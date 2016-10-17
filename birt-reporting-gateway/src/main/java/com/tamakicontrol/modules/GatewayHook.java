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
import com.tamakicontrol.modules.servlets.ReportServlet;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformServletContext;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.core.internal.registry.RegistryProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger("birt-reporting");

    private GatewayContext gatewayContext;

    private static IReportEngine engine;
    private EngineConfig engineConfig = new EngineConfig();

    @Override
    public void setup(GatewayContext gatewayContext){
        this.gatewayContext = gatewayContext;

        BundleUtil.get().addBundle("BirtReporting", getClass(), "BirtReporting");

        engineConfig.setPlatformContext(new PlatformServletContext(gatewayContext.getServletContext()));
        engineConfig.setEngineHome("");
        engineConfig.setLogConfig(gatewayContext.getLogsDir().getAbsolutePath(), Level.INFO);

        verifySchemas(gatewayContext);

        try{
            Platform.startup();

            IReportEngineFactory factory = (IReportEngineFactory)Platform
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

            engine = factory.createReportEngine(engineConfig);
        }catch (BirtException e){
            logger.error("Error while starting Birt Platform", e);
        }

        gatewayContext.addServlet("birt-reporting", ReportServlet.class);

    }

    @Override
    public void startup(LicenseState licenseState) {

    }

    @Override
    public void shutdown() {
        BundleUtil.get().removeBundle("TamakiReporting");

        try {
            if(engine != null)
                engine.destroy();

            Platform.shutdown();
            RegistryProviderFactory.releaseDefault();
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

    public static IReportEngine getReportEngine(){
        return engine;
    }

    public static InputStream getStaticResource(String filePath){
        return GatewayHook.class.getResourceAsStream(filePath);
    }

}
