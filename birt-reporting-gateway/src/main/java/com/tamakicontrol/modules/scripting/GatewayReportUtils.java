package com.tamakicontrol.modules.scripting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.DatasetUtilities;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
import com.tamakicontrol.modules.service.ReportEngineService;
import com.tamakicontrol.modules.service.api.ReportServiceException;
import com.tamakicontrol.modules.utils.ArgumentMap;
import org.eclipse.birt.report.engine.api.*;
import org.eclipse.jetty.http.DateParser;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        return removeReportImpl(reportRecord.getId());
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
    protected Dataset getReportParametersImpl(long id) {
        return reportParametersToDataset(id);
    }

    public String getReportParametersJSON(long id){
        return serializeReportParameters(id);
    }

    private ArrayList<IParameterDefnBase> getBIRTParameters(long id){
        byte[] reportData = getReport(id);
        ArrayList<IParameterDefnBase> params = new ArrayList<>();

        // if null, return an empty set of parameters
        if(reportData == null)
                return params;

        IGetParameterDefinitionTask task = null;
        try {
            IReportRunnable report = ReportEngineService.getInstance().getEngine()
                    .openReportDesign(new ByteArrayInputStream(reportData));

            task = ReportEngineService.getInstance().getEngine().createGetParameterDefinitionTask(report);
            params = (ArrayList)task.getParameterDefns(true);

        }catch(EngineException e){
            logger.error("Engine exception while opening report", e);
        }

        return params;
    }

    private Dataset reportParametersToDataset(long id){

        DatasetBuilder builder = DatasetBuilder.newBuilder();

        builder.colNames("name", "displayName", "defaultValue", "required", "hidden", "dataType", "promptText",
                "helpText", "parameterType", "typeName", "userPropertyValues", "controlType");

        builder.colTypes(String.class, String.class, Object.class, Boolean.class, Boolean.class, Integer.class,
                String.class, String.class, Integer.class, String.class, Object.class, Integer.class);


        ArrayList<IParameterDefnBase> birtParams = getBIRTParameters(id);
        for(IParameterDefnBase param: birtParams) {
            IScalarParameterDefn scalar = (IScalarParameterDefn) param;
            builder.addRow(scalar.getName(), scalar.getDisplayName(), scalar.getDefaultValue(), scalar.isRequired(),
                    scalar.isHidden(), scalar.getDataType(), scalar.getPromptText(), scalar.getHelpText(), scalar.getParameterType(),
                    scalar.getTypeName(), scalar.getUserPropertyValues(), scalar.getControlType());

            // TODO add support for combo boxes
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private String serializeReportParameters(long id) {

        ArrayList<IParameterDefnBase> params = getBIRTParameters(id);

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


                    //TODO this is broken
//                    if(scalar.getControlType() == IScalarParameterDefn.LIST_BOX){
//                        List<IParameterSelectionChoice> selectionList = (List)task.getSelectionList(scalar.getName());
//                        TreeMap<String, Object>selections = new TreeMap<>();
//                        logger.trace("Serializing parameter selections");
//                        if(selectionList != null)
//                            selectionList.forEach(selection -> {
//                                //TODO JSON Serialization doesn't like null labels or values. Handle this more gracefully.
//                                if(selection.getLabel() != null) {
//                                    logger.trace(String.format("Label: %s, Value: %s", selection.getLabel(),
//                                            selection.getValue()));
//                                    selections.put(selection.getLabel(), selection.getValue().toString());
//                                }else{
//                                    logger.trace("Nullified Label");
//                                }
//                            });
//
//                        jsonObject.put("selectionListType", scalar.getSelectionListType());
//                        jsonObject.put("selectionList", selections);
//                    }


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
    protected byte[] runAndRenderReportImpl(long reportId, String reportName, String outputFormat,
                                            PyDictionary parameters, PyDictionary options) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        runAndRenderToStream(reportId, reportName, outputFormat, parameters, options, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public byte[] runAndRenderReportImpl(long reportId, String outputFormat, Map<String, Object> parameters, Map<String, Object> options) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        runAndRenderToStream(reportId, null, outputFormat, parameters, options, outputStream);
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
                                     Map<String, Object> parameters, Map<String, Object> options,
                                     OutputStream outputStream){

        byte[] reportData = (reportId == null) ? getReport(reportName) : getReport(reportId);

        if(reportData == null)
            throw new InvalidParameterException("Invalid report id or name");

        try{
            IReportRunnable report = ReportEngineService.getInstance().getEngine()
                    .openReportDesign(new ByteArrayInputStream(reportData));

            IRunAndRenderTask task = ReportEngineService.getInstance().getEngine().createRunAndRenderTask(report);

            task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                    this.getClass().getClassLoader());

            parameters = typeCastReportParameters(reportData, parameters);
            task.setParameterValues(parameters);

            RenderOption renderOptions = handleRenderOptions(outputFormat, options);
            renderOptions.setOutputStream(outputStream);
            task.setRenderOption(renderOptions);

            Gson gson = new GsonBuilder().create();
            logger.debug(String.format("Rendering with options %s", gson.toJson(options)));

            task.run();
            task.close();

        }catch(EngineException e){
            logger.error("Exception while creating run and render task", e);
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
        }else if(outputFormat.equalsIgnoreCase("xls")){
            handleSpudsoftRenderOptions(renderOptions, options);
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
        htmlOptions.setOutputFormat("html");

        IHTMLImageHandler imageHandler;
        String imageHandlerType = options.getStringArg("image-handler", "server");

        /*
        *
        * Image Handler Types:
        *
        * complete - Built for a "complete" web page.  Links in HTML are shown as file://
        *
        * server - Build for a web application.  Links in HTML shown as href="/images/..."
        *
        * */
        if(imageHandlerType.equalsIgnoreCase("complete"))
            imageHandler = new HTMLCompleteImageHandler();
        else if (imageHandlerType.equalsIgnoreCase("standalone"))
            imageHandler = new HTMLImageHandler();
        else
            imageHandler = new HTMLServerImageHandler();

        htmlOptions.setImageHandler(imageHandler);
        htmlOptions.setImageDirectory(gatewayContext.getTempDir().getPath());
        htmlOptions.setBaseImageURL(options.getStringArg("baseImageURL"));

        htmlOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP;SWF;SVG");
        htmlOptions.setEmbeddable(options.getBooleanArg("embeddable", false));
        htmlOptions.setHtmlPagination(options.getBooleanArg("pagination", false));
        htmlOptions.setBaseImageURL(options.getStringArg("baseImageURL"));
    }

    private void handlePDFRenderOptions(RenderOption renderOption, Map args){
        PDFRenderOption pdfOptions = new PDFRenderOption(renderOption);
        ArgumentMap options = new ArgumentMap(args);
        pdfOptions.setOutputFormat("pdf");

        pdfOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        pdfOptions.setEmbededFont(options.getBooleanArg("embeddedFont", true));
    }

    private void handleExcelRenderOptions(RenderOption renderOption, Map args){
        EXCELRenderOption excelRenderOption = new EXCELRenderOption(renderOption);
        ArgumentMap options = new ArgumentMap(args);
        excelRenderOption.setOutputFormat("xlsx");

        excelRenderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        excelRenderOption.setEnableMultipleSheet(options.getBooleanArg("multipleSheet", false));
        excelRenderOption.setWrappingText(options.getBooleanArg("wrapText", false));
        excelRenderOption.setHideGridlines(options.getBooleanArg("hideGridLines", false));
        excelRenderOption.setEmitterID(options.getStringArg("emitterId"));
        logger.debug(String.format("Using emitter with ID: %s", excelRenderOption.getEmitterID()));
        //  excelRenderOption.setEmitterID("org.eclipse.birt.report.engine.emitter.prototype.excel");
        //	uk.co.spudsoft.birt.emitters.excel.XlsxEmitter
        //  uk.co.spudsoft.birt.emitters.excel.XlsEmitter
    }

    private void handleSpudsoftRenderOptions(RenderOption renderOption, Map args){
        ArgumentMap options = new ArgumentMap(args);

        renderOption.setOutputFormat("xls");
        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        renderOption.setEmitterID(options.getStringArg("emitterId"));
    }

    private void handleWordRenderOptions(RenderOption renderOption, Map options){
        renderOption.setOutputFormat("doc");
        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
    }


    /**
     *
     * Read in parameters from report definition and type cast method parameters to match.
     * If you try to send BIRT a string argument (from a web call) to a report expecting an integer,
     * it throws an exception.
     *
     * */
    private Map<String, Object> typeCastReportParameters(byte[] reportData, Map<String, Object> parameters){

        ArrayList<IParameterDefnBase> reportParameters = ReportEngineService.getInstance().getReportParameters(reportData);

        for(IParameterDefnBase _param : reportParameters) {
            IScalarParameterDefn param = (IScalarParameterDefn)_param;
            Object _value = parameters.get(param.getName());
            Object value = null;

            if(_value != null) {
                logger.debug(String.format("Casting parameter %s to type %s", param.getName(), param.getDataType()));
                try{
                    switch(param.getDataType()){
                        case PARAMETER_DATATYPE_BOOLEAN:
                            //value = Boolean.parseBoolean(_value);
                            value = _value;
                            break;
                        case PARAMETER_DATATYPE_DATE:
                            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            //value = formatter.parse(_value);
                            Date uDate = (Date)_value;
                            value = new java.sql.Date(uDate.getTime());
                            break;
                        case PARAMETER_DATATYPE_INTEGER:
                            value = _value;
                            //value = Integer.parseInt(_value);
                            break;
                        case PARAMETER_DATATYPE_FLOAT:
                            value = _value;
                            //value = Float.parseFloat(_value);
                            break;
                        default:
                            value = _value;
                    }

                    parameters.replace(param.getName(), value);
                }catch(ClassCastException e){
                    logger.error("Error parsing date for report parameter", e);
                }
            }
        }

        return parameters;
    }

    //</editor-fold>

}
