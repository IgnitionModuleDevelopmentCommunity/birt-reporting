package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.DatasetUtilities;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
import com.tamakicontrol.modules.scripting.utils.ArgumentMap;
import org.eclipse.birt.report.engine.api.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.core.PyDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simpleorm.dataset.SQuery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import java.security.InvalidParameterException;
import java.util.*;

public class GatewayReportUtils extends AbstractReportUtils{

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    private GatewayContext gatewayContext;

    public GatewayReportUtils(GatewayContext gatewayContext){
        this.gatewayContext = gatewayContext;
    }

    //<editor-fold desc="CRUD">
    @Override
    protected long saveReportImpl(long id, String name, String description, byte[] reportData) {
        if(reportExists(id) || reportExists(name) && id > 0)
            return updateReport(id, name, description, reportData);
        else
            return addReport(name, description, reportData);
    }

    private long addReport(String name, String description, byte[] reportData){

        ReportRecord reportRecord = gatewayContext.getPersistenceInterface().createNew(ReportRecord.META);
        reportRecord.setName(name);
        reportRecord.setDescription(description);
        reportRecord.setReportData(reportData);
        gatewayContext.getPersistenceInterface().save(reportRecord);

        return reportRecord.getId();
    }

    private long updateReport(long id, String name, String description, byte[] reportData){
        ReportRecord reportRecord = gatewayContext.getPersistenceInterface().find(ReportRecord.META, id);

        reportRecord.setId(id);
        reportRecord.setName(name);
        reportRecord.setDescription(description);
        reportRecord.setReportData(reportData);
        gatewayContext.getPersistenceInterface().save(reportRecord);

        return reportRecord.getId();
    }

    @Override
    protected byte[] getReportImpl(long id) {
        try {
            ReportRecord reportRecord = gatewayContext.getPersistenceInterface().find(ReportRecord.META, id);
            if(reportRecord != null)
                return reportRecord.getReportData();
            else
                return null;

        }catch(NullPointerException e1){
            logger.debug(String.format("Report id '%d' not found", id), e1);
        }

        return null;
    }

    @Override
    protected byte[] getReportImpl(String name) {
        SQuery<ReportRecord> query = new SQuery<>(ReportRecord.META)
                .eq(ReportRecord.META.getField("name"), name);

        try {
            ReportRecord reportRecord = gatewayContext.getPersistenceInterface().queryOne(query);
            if(reportRecord != null)
                return reportRecord.getReportData();
            else
                return null;

        }catch(NullPointerException e1){
            logger.debug(String.format("Report '%s' not found", name), e1);
        }

        return null;
    }

    @Override
    protected boolean reportExistsImpl(long id) {
        return getReport(id) !=  null;
    }

    @Override
    protected boolean reportExistsImpl(String name) {
        return getReport(name) != null;
    }

    @Override
    protected boolean removeReportImpl(long id) {

        PersistenceSession session = gatewayContext.getPersistenceInterface().getSession();
        ReportRecord reportRecord = session.find(ReportRecord.META, id);

        if(reportRecord != null) {
            reportRecord.deleteRecord();
            session.commit();
            session.close();
            return true;
        }else
            session.close();
            return false;
    }

    @Override
    protected boolean removeReportImpl(String name) {
        SQuery<ReportRecord> query = new SQuery<>(ReportRecord.META)
                .eq(ReportRecord.META.getField("name"), name);

        PersistenceSession session = gatewayContext.getPersistenceInterface().getSession();
        ReportRecord reportRecord = session.queryOne(query);

        if(reportRecord != null) {
            reportRecord.deleteRecord();
            session.commit();
            session.close();
            return true;
        }else
            session.close();
            return false;

    }

    //</editor-fold>

    //<editor-fold desc="getReports">

    @Override
    @SuppressWarnings("unchecked")
    protected Dataset getReportsImpl() {
        List<ReportRecord> reports = getReportRecords();

        String[] names = {"Id", "Name", "Description"};
        Class[] types = {Long.class, String.class, String.class};
        Object[][] data = new Object[3][reports.size()];
        ReportRecord record;

        for(int i=0; i < reports.size(); i++) {
            record = reports.get(i);
            data[0][i] = record.getId();
            data[1][i] = record.getName();
            data[2][i] = record.getDescription();
        }

        return new BasicDataset(Arrays.asList(names), Arrays.asList(types), data);
    }

    public String getReportsAsJSON() {
        return DatasetUtilities.toJSONObject(getReports()).toString();
    }

    public List<ReportRecord> getReportRecords(){
        SQuery<ReportRecord> query = new SQuery<>(ReportRecord.META);
        return gatewayContext.getPersistenceInterface().query(query);
    }

    //</editor-fold>

    // <editor-fold desc="getReportParameters">

    @Override
    protected String getReportParametersImpl(long id) {
        return serializeReportParameters(getReport(id));
    }

    @Override
    protected String getReportParametersImpl(String name) {
        return serializeReportParameters(getReport(name));
    }

    public String getReportParametersJSON(long id){
        return serializeReportParameters(getReport(id));
    }

    public String getReportParametersJSON(String name){
        return serializeReportParameters(getReport(name));
    }

    @SuppressWarnings("unchecked")
    private String serializeReportParameters(byte[] reportData) {

        ArrayList<IParameterDefnBase> params = null;
        IGetParameterDefinitionTask task = null;
        try {
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(reportData));

            task = GatewayHook.getReportEngine().createGetParameterDefinitionTask(report);
            params = (ArrayList)task.getParameterDefns(true);
        }catch(EngineException e){
            logger.error("Engine exception while opening report", e);
        }

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        try{
            for(IParameterDefnBase param : params){
                jsonObject = new JSONObject();

                //TODO add support for parameter groups
                if(param instanceof IParameterGroupDefn){
                    IParameterGroupDefn group = (IParameterGroupDefn)param;
                    ArrayList<IParameterDefnBase> groupParams = group.getContents();

                    groupParams.forEach(groupParam -> {
                        groupParam.getName();
                        groupParam.getDisplayName();
                    });

                }
                else{
                    IScalarParameterDefn scalar = (IScalarParameterDefn)param;

                    jsonObject.put("name", scalar.getName());
                    jsonObject.put("displayName", scalar.getDisplayName());
                    jsonObject.put("defaultValue", scalar.getDefaultValue());
                    jsonObject.put("required", scalar.isRequired());
                    jsonObject.put("hidden", scalar.isHidden());
                    jsonObject.put("dataType", scalar.getDataType());
                    jsonObject.put("promptText", scalar.getPromptText());
                    jsonObject.put("helpText", scalar.getHelpText());
                    jsonObject.put("parameterType", scalar.getParameterType());
                    jsonObject.put("typeName", scalar.getTypeName());
                    jsonObject.put("userProperties", scalar.getUserPropertyValues());
                    jsonObject.put("controlType", scalar.getControlType());

                    if(scalar.getControlType() == IScalarParameterDefn.LIST_BOX){
                        ArrayList<IParameterSelectionChoice> selectionList = (ArrayList)task.getSelectionList(scalar.getName());
                        HashMap<String, Object>selections = new HashMap<>();

                        if(selectionList != null)
                            selectionList.forEach(selection -> {
                                selections.put(selection.getLabel(), selection.getValue());
                            });

                        jsonObject.put("selectionListType", scalar.getSelectionListType());
                        jsonObject.put("selectionList", selections);
                    }


                }

                jsonArray.put(jsonObject);
            }
        }catch (JSONException e){
            logger.error("Error serializing parameter structure", e);
        }

        return jsonArray.toString();
    }

    //</editor-fold>

    //<editor-fold desc="runAndRenderReport">

    @Override
    @SuppressWarnings("unchecked")
    protected byte[] runAndRenderReportImpl(long reportId, String reportName, String outputFormat, PyDictionary parameters, PyDictionary options) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        runAndRenderToStream(reportId, reportName, outputFormat, parameters, options, outputStream);
        return outputStream.toByteArray();
    }

    /*
        *
        * This method is abstracted so that it can return an output stream to a servlet,
        * or an output stream to the runAndRenderReportImpl method which can then return
        * a binary array to the client which can be rendered or save however they chose.
        *
        *
        * Arguments:
        *   reportId
        *   reportName
        *   parameters
        *   outputFormat
        *   options
        *
        * */
    @SuppressWarnings("unchecked")
    public void runAndRenderToStream(Map args, OutputStream outputStream){

        Long reportId = Long.parseLong((String)args.get("reportId"));
        String reportName = (String)args.get("reportName");
        String outputFormat = (String)args.get("outputFormat");
        Map<String, Object> parameters = (Map)args.get("parameters");
        Map<String, Object> options = (Map)args.get("options");

        runAndRenderToStream(reportId, reportName, outputFormat, parameters, options, outputStream);
    }

    public void runReport(Long reportId, String reportName, String outputFormat, Map<String, Object> parameters,
                          String outputFile){

        byte[] reportData = (reportId == null) ? getReport(reportName) : getReport(reportId);

        if(reportData == null)
            throw new InvalidParameterException("Invalid report id or name");

        try{
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(reportData));

            IRunTask task = GatewayHook.getReportEngine().createRunTask(report);

            task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                    this.getClass().getClassLoader());

            setTaskParameters(task, parameters);

            task.run(outputFile);
            task.close();

        }catch(EngineException e){
            logger.error("Exception while executing run task", e);
        }

    }

    public void renderToStream(Long reportId, String reportName, String outputFormat, Map<String, Object> parameters,
                               Map<String, Object> options, String reportPath, OutputStream outputStream){

        byte[] reportData = (reportId == null) ? getReport(reportName) : getReport(reportId);

        if(reportData == null)
            throw new InvalidParameterException("Invalid report id or name");

        try{
            IReportDocument report = GatewayHook.getReportEngine()
                    .openReportDocument(reportPath);

            IRenderTask task = GatewayHook.getReportEngine().createRenderTask(report);

            task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                    this.getClass().getClassLoader());

            setTaskParameters(task, parameters);

            RenderOption renderOptions = handleRenderOptions(outputFormat, options);
            renderOptions.setOutputStream(outputStream);
            task.setRenderOption(renderOptions);
            //task.setPageRange("1-2");
            task.render();
            task.close();

        }catch(EngineException e){
            logger.error("Exception while creating run and render task", e);
        }
    }



    @SuppressWarnings("unchecked")
    public void runAndRenderToStream(Long reportId, String reportName, String outputFormat,
                                     Map<String, Object> parameters, Map<String, Object> options, OutputStream outputStream){


        byte[] reportData = (reportId == null) ? getReport(reportName) : getReport(reportId);

        if(reportData == null)
            throw new InvalidParameterException("Invalid report id or name");

        try{
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(reportData));

            IRunAndRenderTask task = GatewayHook.getReportEngine().createRunAndRenderTask(report);

            task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                    this.getClass().getClassLoader());

            setTaskParameters(task, parameters);

            RenderOption renderOptions = handleRenderOptions(outputFormat, options);
            renderOptions.setOutputStream(outputStream);
            task.setRenderOption(renderOptions);

            task.run();
            task.close();

        }catch(EngineException e){
            logger.error("Exception while creating run and render task", e);
        }

    }

    private void setTaskParameters(IEngineTask task, Map<String, Object> params){
        // if parameters are specified, pass them into the task
        if(params != null) {
            params.forEach((key, value) -> {
                logger.trace(String.format("Report Parameters Key: %s, Value: %s", key, value));
                task.setParameterValue(key, value);
            });
        }
    }

    private RenderOption handleRenderOptions(String outputFormat, Map<String, Object> options){
        RenderOption renderOptions = new RenderOption();

        if(outputFormat == null)
            outputFormat = "html";

        if(options == null)
            options = new HashMap<>();

        if(outputFormat.equalsIgnoreCase("pdf")){
            handlePDFRenderOptions(renderOptions, options);
        }else if(outputFormat.equalsIgnoreCase("xlsx")){
            handleExcelRenderOptions(renderOptions, options);
        }else if(outputFormat.equalsIgnoreCase("doc")){
            handleWordRenderOptions(renderOptions, options);
        }else{
            renderOptions.setOutputFormat("html");
            handleHTMLRenderOptions(renderOptions, options);
        }

        return renderOptions;
    }

    private void handleHTMLRenderOptions(RenderOption renderOption, Map args){
        HTMLRenderOption htmlOptions = new HTMLRenderOption(renderOption);
        ArgumentMap options = new ArgumentMap(args);
        renderOption.setOutputFormat("html");

        IHTMLImageHandler serverImageHandler = new HTMLServerImageHandler();
        htmlOptions.setImageHandler(serverImageHandler);
        htmlOptions.setImageDirectory(gatewayContext.getTempDir().getPath());
        htmlOptions.setBaseImageURL(options.getStringArg("baseImageURL"));

        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP;SWF;SVG");
        htmlOptions.setEmbeddable(options.getBooleanArg("embeddable", false));
        htmlOptions.setHtmlPagination(options.getBooleanArg("pagination", false));
        htmlOptions.setBaseImageURL(options.getStringArg("baseImageURL"));
    }

    private void handlePDFRenderOptions(RenderOption renderOption, Map args){
        PDFRenderOption pdfOptions = new PDFRenderOption(renderOption);
        ArgumentMap options = new ArgumentMap(args);
        renderOption.setOutputFormat("pdf");

        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        pdfOptions.setEmbededFont(options.getBooleanArg("embeddedFont", true));
    }

    private void handleExcelRenderOptions(RenderOption renderOption, Map args){
        EXCELRenderOption excelRenderOption = new EXCELRenderOption(renderOption);
        ArgumentMap options = new ArgumentMap(args);
        renderOption.setOutputFormat("xlsx");

        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        excelRenderOption.setEnableMultipleSheet(options.getBooleanArg("multipleSheet", false));
        excelRenderOption.setWrappingText(options.getBooleanArg("wrapText", false));
        excelRenderOption.setHideGridlines(options.getBooleanArg("hideGridLines", false));
    }

    private void handleWordRenderOptions(RenderOption renderOption, Map options){
        renderOption.setOutputFormat("doc");
        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
    }

    //</editor-fold>

}
