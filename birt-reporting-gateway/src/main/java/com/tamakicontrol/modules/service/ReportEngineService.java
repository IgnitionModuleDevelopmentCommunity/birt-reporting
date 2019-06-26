package com.tamakicontrol.modules.service;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.service.api.ReportServiceException;
import com.tamakicontrol.modules.utils.ArgumentMap;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformServletContext;
import org.eclipse.birt.report.engine.api.*;
import org.eclipse.core.internal.registry.RegistryProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.OutputStream;

public class ReportEngineService {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    private static ReportEngineService instance;
    private IReportEngine engine;

    /*
    *
    *
    * */
    public ReportEngineService(GatewayContext context) {

        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setPlatformContext(new PlatformServletContext(context.getWebResourceManager().getServletContext()));
        engineConfig.setLogConfig(context.getSystemManager().getLogsDir().getAbsolutePath(), Level.INFO);
        engineConfig.setEngineHome(context.getSystemManager().getDataDir().getAbsolutePath());

        /*
        * Remove default BIRT file logger and redirect it to SLF4J
        * */
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        if (SLF4JBridgeHandler.isInstalled())
            SLF4JBridgeHandler.install();

        try {
            Platform.startup();
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

            if(factory == null){
                logger.error("Factory Object Null");
            }

            engine = factory.createReportEngine(engineConfig);
        } catch (BirtException e) {
            logger.error(e.toString());
        }

    }

    public static ReportEngineService getInstance() {
        return instance;
    }

    /**
     * Get engine instance.
     *
     * @param servletContext
     * @throws BirtException
     */
    public synchronized static void initEngineInstance(
            GatewayContext servletContext) throws BirtException {
        if (ReportEngineService.instance != null)
            return;

        ReportEngineService.instance = new ReportEngineService(servletContext);
    }

    /**
     * Destroy engine instance
     */
    public synchronized static void destroyEngineInstance() {
        ReportEngineService.getInstance().destroyEngine();

        Platform.shutdown();
        RegistryProviderFactory.releaseDefault();

        if (SLF4JBridgeHandler.isInstalled())
            SLF4JBridgeHandler.uninstall();
    }

    public IReportEngine getEngine() {
        return this.engine;
    }

    private void destroyEngine() {
        if (engine != null)
            engine.destroy();

    }

}
