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
    private GatewayContext context;

    /*
    *
    *
    * */
    public ReportEngineService(GatewayContext context) {
        this.context = context;

        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setPlatformContext(new PlatformServletContext(context.getServletContext()));
        engineConfig.setLogConfig(context.getLogsDir().getAbsolutePath(), Level.INFO);

        /*
        * Remove default BIRT file logger and redirect it to SLF4J
        * */
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        if (SLF4JBridgeHandler.isInstalled())
            SLF4JBridgeHandler.install();

        try {
            Platform.startup();
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
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

    public IReportRunnable openReportDesign(InputStream inputStream) {
        IReportRunnable reportDesign = null;

        try {
            reportDesign = engine.openReportDesign(inputStream);
        } catch (EngineException e) {
            logger.error(e.toString());
        }

        return reportDesign;
    }

    public ArrayList<IParameterDefnBase> getReportParameters(byte[] reportData) {
        ArrayList<IParameterDefnBase> params = null;

        try {
            IReportRunnable report = engine.openReportDesign(new ByteArrayInputStream(reportData));

            IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask(report);
            params = (ArrayList) task.getParameterDefns(true);
        } catch (EngineException e) {
            logger.error("Engine exception while opening report", e);
        }

        return params;
    }

    /*
     *
     * createRunAndRenderTask
     *
     *
     * */
    public IRunAndRenderTask createRunAndRenderTask(IReportRunnable report, String outputFormat, Map options,
                                                    Map parameters, OutputStream outputStream) throws ReportServiceException {

        IRunAndRenderTask task = engine.createRunAndRenderTask(report);

        //task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
        //        this.getClass().getClassLoader());

        //if (parameters != null) {
        //    task.setParameterValues(parameters);
        //}

        //RenderOption renderOption = createRenderOption(outputFormat, options);
        //renderOption.setOutputStream(outputStream);
        //task.setRenderOption(renderOption);

        return task;
    }

    public IRunTask createRunTask(IReportRunnable report, Map parameters) throws ReportServiceException {
        IRunTask task = engine.createRunTask(report);

        task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                this.getClass().getClassLoader());

        if (parameters != null)
            task.setParameterValues(parameters);

        return task;
    }

    public IRenderTask createRenderTask(IReportDocument document, String outputFormat, Map options,
                                        OutputStream outputStream) {
        IRenderTask task = engine.createRenderTask(document);

        RenderOption renderOption = createRenderOption(outputFormat, options);
        renderOption.setOutputStream(outputStream);

        task.setRenderOption(renderOption);

        return task;
    }

    private RenderOption createRenderOption(String outputFormat, Map options) {

        RenderOption renderOption;

        String outFormat = outputFormat == null ? "html" : outputFormat;

        if (options == null)
            options = new HashMap<>();

        if (outFormat.equalsIgnoreCase("pdf")) {
            renderOption = createPDFRenderOption(options);
        } else if (outFormat.equalsIgnoreCase("xlsx")) {
            renderOption = createExcelRenderOption(options);
        } else if (outFormat.equalsIgnoreCase("doc")) {
            renderOption = createWordRenderOptions(options);
        } else {
            renderOption = createHTMLRenderOption(options);
        }

        return renderOption;
    }

    private HTMLRenderOption createHTMLRenderOption(Map options) {
        HTMLRenderOption htmlOptions = new HTMLRenderOption();
        ArgumentMap optionMap = new ArgumentMap(options);

        htmlOptions.setOutputFormat("html");

        IHTMLImageHandler serverImageHandler = new HTMLServerImageHandler();
        htmlOptions.setImageHandler(serverImageHandler);
        htmlOptions.setImageDirectory(context.getTempDir().getPath());
        htmlOptions.setBaseImageURL(optionMap.getStringArg("baseImageURL"));

        htmlOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP;SWF;SVG");
        htmlOptions.setEmbeddable(optionMap.getBooleanArg("embeddable", false));
        htmlOptions.setHtmlPagination(optionMap.getBooleanArg("pagination", false));
        htmlOptions.setBaseImageURL(optionMap.getStringArg("baseImageURL"));

        //context.getProjectManager().getProject().getResource().getData()

        return htmlOptions;
    }

    private PDFRenderOption createPDFRenderOption(Map options) {
        PDFRenderOption pdfOptions = new PDFRenderOption();
        ArgumentMap optionMap = new ArgumentMap(options);

        pdfOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        pdfOptions.setEmbededFont(optionMap.getBooleanArg("embeddedFont", true));

        return pdfOptions;
    }

    private EXCELRenderOption createExcelRenderOption(Map options) {
        EXCELRenderOption excelOptions = new EXCELRenderOption();
        ArgumentMap optionMap = new ArgumentMap(options);
        excelOptions.setOutputFormat("xlsx");

        excelOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        excelOptions.setEnableMultipleSheet(optionMap.getBooleanArg("multipleSheet", false));
        excelOptions.setWrappingText(optionMap.getBooleanArg("wrapText", false));
        excelOptions.setHideGridlines(optionMap.getBooleanArg("hideGridLines", false));

        return excelOptions;
    }

    private RenderOption createWordRenderOptions(Map options) {
        RenderOption renderOption = new RenderOption();
        renderOption.setOutputFormat("doc");
        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        return renderOption;
    }

    public void runAndRenderReport(IReportRunnable report, String outputFormat, Map options,
                                   Map parameters, OutputStream outputStream) throws ReportServiceException {

        IRunAndRenderTask task;
        try {
            task = createRunAndRenderTask(report, outputFormat, options, parameters, outputStream);
        } catch (ReportServiceException e) {
            throw new ReportServiceException("Exception thrown while creating run and render task", e);
        }

        try {
            task.run();
        } catch (BirtException e) {
            task.cancel();
            throw new ReportServiceException("Exception thrown while running report", e);
        } finally {
            task.close();
        }

    }

}
