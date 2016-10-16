package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
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

    @Override
    protected byte[] getReportImpl(String name) {
        ReportRecord searchRecord = new ReportRecord();
        searchRecord.setName(name);
        gatewayContext.getPersistenceInterface().find(ReportRecord.META, searchRecord);

        SQuery<ReportRecord> query = new SQuery<ReportRecord>(ReportRecord.META)
                .eq(ReportRecord.META.getField("name"), name);

        return gatewayContext.getPersistenceInterface().queryOne(query).getReportData();
    }

    @Override
    protected Dataset getReportsImpl() {
        SQuery<ReportRecord> query = new SQuery<ReportRecord>(ReportRecord.META);

        List<ReportRecord> results = gatewayContext.getPersistenceInterface().query(query);
        List<Object> resultsObject = new ArrayList<>();

        String[] names = {"Id", "Name", "Description"};
        Class[] types = {Long.class, String.class, String.class};
        Object[][] data = new Object[results.size()][3];
        ReportRecord record;

        for(int i=0; i < results.size(); i++){
            record = results.get(i);
            data[i][0] = record.getId();
            data[i][1] = record.getName();
            data[i][2] = record.getDescription();
        }

        return new BasicDataset(Arrays.asList(names), Arrays.asList(types), data);
    }




    @Override
    protected boolean removeReportImpl(long id) {
        return false;
    }

    @Override
    protected boolean removeReportImpl(String name) {
        return false;
    }





    @Override
    protected Dataset getReportParametersImpl(long id) {
        return getReportParametersFromDesign(getReport(id));
    }

    @Override
    protected Dataset getReportParametersImpl(String name) {
        return getReportParametersFromDesign(getReport(name));
    }

    private Dataset getReportParametersFromDesign(byte[] rptDesign){
        try {
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(rptDesign));

            IGetParameterDefinitionTask task = GatewayHook.getReportEngine().createGetParameterDefinitionTask(report);
            ArrayList<IParameterDefnBase> params = new ArrayList<>(task.getParameterDefns(true));

            String[] names = {"name", "displayName", "helpText", "parameterType", "parameterType",
                    "promptText", "typeName", "userProperties"};

            Class[] types = {String.class, String.class, String.class, Integer.class, String.class, String.class,
                    String.class, String.class, Map.class};

            Object[][] data = new Object[params.size()][6];

            IParameterDefnBase param;
            for(int i=0; i < params.size(); i++){
                param = params.get(i);
                data[i][0] = param.getName();
                data[i][1] = param.getDisplayName();
                data[i][2] = param.getHelpText();
                data[i][3] = param.getParameterType();
                data[i][4] = param.getPromptText();
                data[i][5] = param.getTypeName();
                data[i][6] = param.getUserPropertyValues();
            }

            return new BasicDataset(Arrays.asList(names), Arrays.asList(types), data);
        }catch(EngineException e){
            logger.error("Engine exception while opening report", e);
        }
        return null;
    }




    @Override
    protected byte[] runAndRenderReportImpl(PyObject[] objects, String[] keywords){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IRunAndRenderTask task = getRunAndRenderTask(PyArgumentMap.interpretPyArgs(objects, keywords, this.getClass(),
                "runAndRenderReportImpl"), outputStream);

        try {
            task.run();
        }catch (EngineException e){
            logger.error("Engine exception while attempting to run & render report", e);
        }catch(NullPointerException e) {
            logger.error("Null pointer exception while attempting to run & render report", e);
        }finally{
            task.close();
        }

        return outputStream.toByteArray();
    }

    /*
    *
    * This method is abstracted so that it can return an output stream to a servlet,
    * or an output stream to the runAndRenderReportImpl method which can then return
    * a binary array to the client which can be rendered or save however they chose.
    *
    * */
    public IRunAndRenderTask getRunAndRenderTask(Map args, OutputStream outputStream){

        byte[] reportData = getReport((String)args.get("id"));

        try{
            IReportRunnable report = GatewayHook.getReportEngine().openReportDesign(new ByteArrayInputStream(reportData));
            IRunAndRenderTask task = GatewayHook.getReportEngine().createRunAndRenderTask(report);
            task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                    this.getClass().getClassLoader());


            Map<String, Object> reportParams = (Map)args.get("parameters");
            reportParams.forEach((key, value) -> {
                    logger.trace(String.format("Key: %s, Value: %s", key, value));
                    task.setParameterValue(key, value);
            });


            HTMLRenderOption options = new HTMLRenderOption();
            options.setOutputFormat("html");
            options.setOutputStream(outputStream);
            options.setEmbeddable(false);
            task.setRenderOption(options);

            return task;
        }catch(EngineException e){
            logger.error("Exception while creating run & render task", e);
        }

        return null;
    }

}
