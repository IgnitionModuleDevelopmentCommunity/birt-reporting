package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.BirtReport;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simpleorm.dataset.SQuery;

import java.io.ByteArrayInputStream;
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
        return searchRecord.getReportData();
    }

    @Override
    protected List<Object> getReportsImpl(boolean includeData) {
        SQuery<ReportRecord> query = new SQuery<ReportRecord>(ReportRecord.META);

        List<ReportRecord> results = gatewayContext.getPersistenceInterface().query(query);
        List<Object> resultsObject = new ArrayList<>();

        results.forEach(record -> {
            BirtReport report = new BirtReport(record.getId(), record.getName(), record.getDescription());

            if(includeData)
                report.setReportData(record.getReportData());

            resultsObject.add(report);
        });

        return resultsObject;
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
    protected List<Map<String, Object>> getReportParametersImpl(long id) {

        try {
            IReportRunnable report = GatewayHook.getReportEngine()
                    .openReportDesign(new ByteArrayInputStream(getReport(id)));

            IGetParameterDefinitionTask task = GatewayHook.getReportEngine().createGetParameterDefinitionTask(report);
            Collection params = task.getParameterDefns(true);
            List<Map<String, Object>> reportParams = new ArrayList<>();

            // TODO is there a simpler way of doing this?
            reportParams.forEach(param -> {
                IParameterDefnBase paramImpl = (IParameterDefnBase)param;
                Map<String, Object> reportParam = new HashMap<String, Object>();

                reportParam.put("displayName", paramImpl.getDisplayName());
                reportParam.put("name", paramImpl.getName());
                reportParam.put("helpText", paramImpl.getHelpText());
                reportParam.put("proptText", paramImpl.getPromptText());
                reportParam.put("typeName", paramImpl.getTypeName());
                reportParam.put("parameterType", paramImpl.getParameterType());
                reportParam.put("userPropertyValues", paramImpl.getUserPropertyValues());

                reportParams.add(reportParam);
            });

            return reportParams;

        }catch(EngineException e){
            logger.error(String.format("Engine exception while opening report with id %d", id), e);
        }
        return null;
    }

    @Override
    protected List<Map<String, Object>> getReportParametersImpl(String name) {
        return null;
    }

}
