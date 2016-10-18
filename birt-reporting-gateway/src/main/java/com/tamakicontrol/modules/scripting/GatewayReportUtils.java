package com.tamakicontrol.modules.scripting;

import com.google.gson.Gson;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistenceSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
import org.eclipse.birt.report.engine.api.*;
import org.eclipse.birt.report.engine.ir.Report;
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

    @Override
    protected String getReportsAsJSONImpl() {
        List<ReportRecord> reports = getReportRecords();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        try {
            for (ReportRecord report : reports) {
                jsonObject = new JSONObject();
                jsonObject.put("reportId", report.getId());
                jsonObject.put("reportName", report.getName());
                jsonObject.put("description", report.getDescription());

                jsonArray.put(jsonObject);
            }
        }catch (JSONException e){
            logger.error("JSON exception while serializing reports", e);
        }

        return jsonArray.toString();
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

    //TODO Should we have a dataset return option?
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

        if(reportData == null)
            throw new InvalidParameterException("Invalid report id or name");


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
