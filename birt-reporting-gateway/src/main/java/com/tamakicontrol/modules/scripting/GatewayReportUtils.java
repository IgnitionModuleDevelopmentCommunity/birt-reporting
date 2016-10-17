package com.tamakicontrol.modules.scripting;

import com.google.gson.Gson;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
import org.eclipse.birt.report.engine.api.*;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simpleorm.dataset.SQuery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

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

        SQuery<ReportRecord> query = new SQuery<ReportRecord>(ReportRecord.META)
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
        SQuery<ReportRecord> query = new SQuery<ReportRecord>(ReportRecord.META);
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

    private List<IParameterDefnBase> getReportParametersFromDesign(byte[] rptDesign){
        try {
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(rptDesign));

            IGetParameterDefinitionTask task = GatewayHook.getReportEngine().createGetParameterDefinitionTask(report);
            ArrayList<IParameterDefnBase> params = new ArrayList<>(task.getParameterDefns(true));

            return params;
        }catch(EngineException e){
            logger.error("Engine exception while opening report", e);
        }
        return null;
    }

    private String serializeParametersAsJSON(List<IParameterDefnBase> params){
        Gson gson = new Gson();
        gson.toJson(params);
        return gson.toJson(params);
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
    protected byte[] runAndRenderReportImpl(PyObject[] objects, String[] keywords) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        HashMap<String, Object> args = new HashMap<>();

        for(int i=0; i < objects.length; i++){
            args.put(keywords[i], (Object)objects[i]);
        }

//        runAndRenderToStream(PyArgumentMap.interpretPyArgs(objects, keywords, this.getClass(),
//                "runAndRenderReport"), outputStream);

        runAndRenderToStream(args, outputStream);

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
    public String runAndRenderToStream(Map args, OutputStream outputStream){

        // Read in report data from either ID or Name
        byte[] reportData = null;
        if(args.get("reportId") != null)
            try {
                reportData = getReport(Long.parseLong((String) args.get("reportId")));
            }catch(NumberFormatException e){
                logger.warn("Parsing reportId from request failed", e);
            }
        else if(args.get("reportName") != null)
            reportData = getReport((String)args.get("reportName"));
        else {
            logger.warn("Request for report did not specify id or name");
            return null;
        }

        try{
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(reportData));

            IRunAndRenderTask task = GatewayHook.getReportEngine().createRunAndRenderTask(report);

            task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                    this.getClass().getClassLoader());

            // if parameters are specified, pass them into the task
            if(args.get("parameters") != null) {
                HashMap<String, Object> reportParams = (HashMap) args.get("parameters");
                reportParams.forEach((key, value) -> {
                    logger.trace(String.format("Report Parameters Key: %s, Value: %s", key, value));
                    task.setParameterValue(key, value);
                });
            }

            RenderOption options = new RenderOption();
            options.setOutputFormat((String)args.getOrDefault("outputFormat", "html"));

            if(options.getOutputFormat().equalsIgnoreCase("html")){
                HTMLRenderOption htmlRenderOption = new HTMLRenderOption(options);
                htmlRenderOption.setEmbeddable(true);
            }else if(options.getOutputFormat().equalsIgnoreCase("pdf")){
                PDFRenderOption pdfRenderOption = new PDFRenderOption(options);
            }else if(options.getOutputFormat().equalsIgnoreCase("xls")){
                EXCELRenderOption excelRenderOption = new EXCELRenderOption(options);
                excelRenderOption.setEnableMultipleSheet(false);
            }else if(options.getOutputFormat().equalsIgnoreCase("doc")){
            }else{
                options.setOutputFormat("html");
            }

            // todo add other relevant options.  Embedible, pdf etc
            // if options other than the defaults are specified, pass them into the task
            if(args.get("options") != null) {
                HashMap<String, Object> reportOptions = (HashMap) args.get("options");
                options.setOutputFormat((String)reportOptions.getOrDefault("outputFormat", "html"));
            }

            options.setOutputStream(outputStream);
            task.setRenderOption(options);

            try {
                task.run();
            }catch (EngineException e){
                logger.error("Engine exception while attempting to run & render report", e);
            }catch(NullPointerException e) {
                logger.error("Null pointer exception while attempting to run & render report", e);
            }finally{
                task.close();

            }

            return options.getOutputFormat();
        }catch(EngineException e){
            logger.error("Exception while creating run & render task", e);
        }

        return null;
    }

    //</editor-fold>

}
