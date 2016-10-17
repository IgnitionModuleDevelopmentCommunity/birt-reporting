package com.tamakicontrol.modules.scripting;

import com.google.gson.Gson;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
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
        try {
            ReportRecord reportRecord = gatewayContext.getPersistenceInterface().createNew(ReportRecord.META);

            reportRecord.setId(id);
            reportRecord.setName(name);
            reportRecord.setDescription(description);
            reportRecord.setReportData(reportData);

            gatewayContext.getSchemaUpdater().ensureRecordExists(reportRecord);

            return reportRecord.getId();
        }catch (Exception e){
            logger.error("Exception while saving report", e);
            return -1L;
        }
    }

    @Override
    protected byte[] getReportImpl(long id) {
        try {
            return gatewayContext.getPersistenceInterface().find(ReportRecord.META, id).getReportData();
        }catch(NullPointerException e){
            logger.error(String.format("Application threw null pointer when searching for report id %d", id), e);
            return null;
        }
    }

    //TODO getReportImpl(String) doesn't work
    @Override
    protected byte[] getReportImpl(String name) {
        ReportRecord searchRecord = new ReportRecord();
        searchRecord.setName(name);
        gatewayContext.getPersistenceInterface().find(ReportRecord.META, searchRecord);

        SQuery<ReportRecord> query = new SQuery<>(ReportRecord.META)
                .eq(ReportRecord.META.getField("name"), name);

        return gatewayContext.getPersistenceInterface().queryOne(query).getReportData();
    }

    //TODO Impement removeReport
    @Override
    protected boolean removeReportImpl(long id) {
        return false;
    }

    @Override
    protected boolean removeReportImpl(String name) {
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
        Object[][] data = new Object[reports.size()][3];
        ReportRecord record;

        for(int i=0; i < reports.size(); i++){
            record = reports.get(i);
            data[i][0] = record.getId();
            data[i][1] = record.getName();
            data[i][2] = record.getDescription();
        }

        return new BasicDataset(Arrays.asList(names), Arrays.asList(types), data);
    }

    @Override
    protected String getReportsAsJSONImpl() {
        List<ReportRecord> reports = getReportRecords();
        Gson gson = new Gson();
        return gson.toJson(reports);
    }

    private List<ReportRecord> getReportRecords(){
        SQuery<ReportRecord> query = new SQuery<>(ReportRecord.META);
        return gatewayContext.getPersistenceInterface().query(query);
    }

    //</editor-fold>

    // <editor-fold desc="getReportParameters">

    @Override
    protected Dataset getReportParametersImpl(long id) {
        return serializeParametersAsDataset(getReportParametersFromDesign(getReport(id)));
    }

    @Override
    protected Dataset getReportParametersImpl(String name) {
        return serializeParametersAsDataset(getReportParametersFromDesign(getReport(name)));
    }

    @Override
    protected String getReportParametersAsJSONImpl(long id) {
        return serializeParametersAsJSON(getReportParametersFromDesign(getReport(id)));
    }

    @Override
    protected String getReportParametersAsJSONImpl(String name) {
        return serializeParametersAsJSON(getReportParametersFromDesign(getReport(name)));
    }

    @SuppressWarnings("unchecked")
    private List<IParameterDefnBase> getReportParametersFromDesign(byte[] rptDesign){
        try {
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(rptDesign));

            IGetParameterDefinitionTask task = GatewayHook.getReportEngine().createGetParameterDefinitionTask(report);

            return new ArrayList<>(task.getParameterDefns(true));
        }catch(EngineException e){
            logger.error("Engine exception while opening report", e);
        }
        return null;
    }

    private String serializeParametersAsJSON(List<IParameterDefnBase> params){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        try{
            for(IParameterDefnBase param : params){

                IScalarParameterDefn scalar = (IScalarParameterDefn)param;
                jsonObject = new JSONObject();

                jsonObject.put("name", param.getName());
                jsonObject.put("displayName", param.getDisplayName());
                jsonObject.put("defaultValue", scalar.getDefaultValue());
                jsonObject.put("required", scalar);
                jsonObject.put("dataType", scalar.getDataType());
                jsonObject.put("promptText", param.getPromptText());
                jsonObject.put("helpText", param.getHelpText());
                jsonObject.put("parameterType", param.getParameterType());
                jsonObject.put("typeName", param.getTypeName());
                jsonObject.put("userProperties", param.getUserPropertyValues());
                jsonObject.put("controlType", scalar.getControlType());
                jsonObject.put("selectionListType", scalar.getSelectionListType());
                jsonObject.put("selectionList", scalar.getSelectionList());

                jsonArray.put(jsonObject);
            }
        }catch (JSONException e){
            logger.error("Error serializing parameter structure", e);
        }

        return jsonArray.toString();
    }

    private Dataset serializeParametersAsDataset(List<IParameterDefnBase> params){
        String[] names = {"name", "displayName", "helpText", "parameterType",
                "promptText", "typeName"};

        Class[] types = {String.class, String.class, String.class, Integer.class,
                String.class, String.class};

        Object[][] data = new Object[params.size()][7];

        IParameterDefnBase param;
        for(int i=0; i < params.size(); i++){
            param = params.get(i);
            data[i][0] = param.getName();
            data[i][1] = param.getDisplayName();
            data[i][2] = param.getHelpText();
            data[i][3] = param.getParameterType();
            data[i][4] = param.getPromptText();
            data[i][5] = param.getTypeName();
        }

        return new BasicDataset(names, types, data);
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

    @SuppressWarnings("unchecked")
    public void runAndRenderToStream(Long reportId, String reportName, String outputFormat,
                                     Map<String, Object> parameters, Map<String, Object> options, OutputStream outputStream){

        byte[] reportData;
        if(reportId != null){
            reportData = getReport(reportId);
        }else if(reportName != null){
            reportData = getReport(reportName);
        }else{
            throw new InvalidParameterException("Must specify either report id or name");
        }

        try{
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(reportData));

            IRunAndRenderTask task = GatewayHook.getReportEngine().createRunAndRenderTask(report);

            task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                    this.getClass().getClassLoader());

            // if parameters are specified, pass them into the task
            if(parameters != null) {
                parameters.forEach((key, value) -> {
                    logger.trace(String.format("Report Parameters Key: %s, Value: %s", key, value));
                    task.setParameterValue(key, value);
                });
            }

            RenderOption renderOptions = new RenderOption();

            if(outputFormat == null)
                outputFormat = "html";
            renderOptions.setOutputFormat(outputFormat);


            /*
            *
            * Build rendering options for report
            *
            * */
            if(options == null)
                options = new HashMap<>();

            if(renderOptions.getOutputFormat().equalsIgnoreCase("html")){
                HTMLRenderOption htmlRenderOption = new HTMLRenderOption(renderOptions);
                htmlRenderOption.setEmbeddable((boolean)options.getOrDefault("embeddable", false));
            }else if(renderOptions.getOutputFormat().equalsIgnoreCase("pdf")){
                PDFRenderOption pdfRenderOption = new PDFRenderOption(renderOptions);
                pdfRenderOption.setEmbededFont((boolean)options.getOrDefault("embedFont", true));
            }else if(renderOptions.getOutputFormat().equalsIgnoreCase("xls")){
                EXCELRenderOption excelRenderOption = new EXCELRenderOption(renderOptions);
                excelRenderOption.setEnableMultipleSheet((boolean)options.getOrDefault("enableMultipleSheets", false));
                excelRenderOption.setHideGridlines((boolean)options.getOrDefault("disableGridLines", false));
                excelRenderOption.setWrappingText((boolean)options.getOrDefault("wrapText", false));

                //TODO what strings are available for office versions?
                //excelRenderOption.setOfficeVersion();
            }else if(renderOptions.getOutputFormat().equalsIgnoreCase("doc")){
                //TODO find word rendering options
                assert true;
            }else{
                renderOptions.setOutputFormat("html");
            }

            renderOptions.setOutputStream(outputStream);
            task.setRenderOption(renderOptions);

            try {
                task.run();
            }catch (EngineException e){
                logger.error("Engine exception while attempting to run & render report", e);
            }catch(NullPointerException e) {
                logger.error("Null pointer exception while attempting to run & render report", e);
            }finally{
                task.close();
            }

        }catch(EngineException e){
            logger.error("Exception while creating run & render task", e);
        }

    }

    //</editor-fold>

}
