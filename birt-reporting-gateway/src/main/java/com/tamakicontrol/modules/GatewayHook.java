package com.tamakicontrol.modules;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.records.ReportRecord;
import com.tamakicontrol.modules.scripting.GatewayReportUtils;
import com.tamakicontrol.modules.service.ReportEngineService;
import com.tamakicontrol.modules.servlets.ReportServlet;
import org.eclipse.birt.core.exception.BirtException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger("birt-reporting");

    private GatewayContext gatewayContext;

    @Override
    public boolean isFreeModule() {
        return true;
    }

    @Override
    public void setup(GatewayContext gatewayContext){
        this.gatewayContext = gatewayContext;

        BundleUtil.get().addBundle("BIRTReporting", getClass(), "BIRTReporting");

        verifySchemas(gatewayContext);

        try{
            ReportEngineService.initEngineInstance(gatewayContext);
        }catch (BirtException e){
            logger.error("Error while starting BIRT Platform", e);
        }

        gatewayContext.addServlet("birt-reporting", ReportServlet.class);
    }

    @Override
    public void startup(LicenseState licenseState) {
    }

    @Override
    public void shutdown() {
        BundleUtil.get().removeBundle("BIRTReporting");

        try {
            ReportEngineService.destroyEngineInstance();
        }catch(Exception e){
            logger.error("Failed to shutdown BIRT engine", e);
        }

        gatewayContext.removeServlet("birt-reporting");
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {

        super.initializeScriptManager(manager);
        manager.addScriptModule("system.report", new GatewayReportUtils(gatewayContext), new PropertiesFileDocProvider());
    }

    private void verifySchemas(GatewayContext gatewayContext){
        try{
            gatewayContext.getSchemaUpdater().updatePersistentRecords(ReportRecord.META);
        }catch(SQLException e){
            logger.error("Error while creating reporting tables", e);
        }
    }

    @Override
    public Object getRPCHandler(ClientReqSession session, Long projectId) {
        return new GatewayReportUtils(gatewayContext);
    }

}
