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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.OutputStream;

public class ReportEngineService {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    private static ReportEngineService instance;

    private IReportEngine engine = null;
    private EngineConfig engineConfig = null;

    private GatewayContext context;

    private ConcurrentLinkedQueue<IEngineTask> taskQueue = new ConcurrentLinkedQueue<>();

    public ReportEngineService(GatewayContext context){
        this.context = context;

        engineConfig = new EngineConfig();
        engineConfig.setPlatformContext(new PlatformServletContext(context.getServletContext()));
        engineConfig.setEngineHome("");
        engineConfig.setLogConfig(context.getLogsDir().getAbsolutePath(), Level.ALL);

        try {
            Platform.startup();

            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

            engine = factory.createReportEngine(engineConfig);

        }catch (BirtException e){
            logger.error(e.toString());
        }

    }

    public static ReportEngineService getInstance(){
        return instance;
    }

    /**
     * Get engine instance.
     *
     * @param servletContext
     * @throws BirtException
     *
     */
    public synchronized static void initEngineInstance(
            GatewayContext servletContext ) throws BirtException {
        if ( ReportEngineService.instance != null )
            return;

        ReportEngineService.instance = new ReportEngineService(servletContext);
    }

    public synchronized static void destroyEngineInstance(){
        ReportEngineService.getInstance().destroyEngine();

        Platform.shutdown();
        RegistryProviderFactory.releaseDefault();
    }

    private void destroyEngine(){
        if(engine != null)
            engine.destroy();
    }

    public IReportRunnable openReportDesign(InputStream inputStream){
        IReportRunnable reportDesign = null;

        try{
            reportDesign = engine.openReportDesign(inputStream);
        }catch(EngineException e){
            logger.error(e.toString());
        }

        return reportDesign;
    }

    public IRunAndRenderTask createRunAndRenderTask(IReportRunnable report, String outputFormat, Map options,
                                                    Map parameters, OutputStream outputStream) throws ReportServiceException {

        IRunAndRenderTask task = engine.createRunAndRenderTask(report);

        task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                this.getClass().getClassLoader());

        if(parameters != null)
            task.setParameterValues(parameters);

        RenderOption renderOption = createRenderOption(outputFormat, options);
        renderOption.setOutputStream(outputStream);

        task.setRenderOption(renderOption);

        return task;
    }

    private RenderOption createRenderOption(String outputFormat, Map options){

        RenderOption renderOption;

        String outFormat = outputFormat == null ? "html" : outputFormat;

        if(options == null)
            options = new HashMap<>();

        if(outFormat.equalsIgnoreCase("pdf")){
            renderOption = createPDFRenderOption(options);
        }else if(outFormat.equalsIgnoreCase("xlsx")){
            renderOption =  createExcelRenderOption(options);
        }else if(outFormat.equalsIgnoreCase("doc")){
            renderOption = createWordRenderOptions(options);
        }else{
            renderOption =  createHTMLRenderOption(options);
        }

        return renderOption;
    }

    private HTMLRenderOption createHTMLRenderOption(Map options){
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

        return htmlOptions;
    }

    private PDFRenderOption createPDFRenderOption(Map options){
        PDFRenderOption pdfOptions = new PDFRenderOption();
        ArgumentMap optionMap = new ArgumentMap(options);

        pdfOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        pdfOptions.setEmbededFont(optionMap.getBooleanArg("embeddedFont", true));

        return pdfOptions;
    }

    private EXCELRenderOption createExcelRenderOption(Map options){
        EXCELRenderOption excelOptions = new EXCELRenderOption();
        ArgumentMap optionMap = new ArgumentMap(options);
        excelOptions.setOutputFormat("xlsx");

        excelOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        excelOptions.setEnableMultipleSheet(optionMap.getBooleanArg("multipleSheet", false));
        excelOptions.setWrappingText(optionMap.getBooleanArg("wrapText", false));
        excelOptions.setHideGridlines(optionMap.getBooleanArg("hideGridLines", false));

        return excelOptions;
    }

    private RenderOption createWordRenderOptions(Map options){
        RenderOption renderOption = new RenderOption();
        renderOption.setOutputFormat("doc");
        renderOption.setSupportedImageFormats("PNG;GIF;JPG;BMP");
        return renderOption;
    }

    public void runAndRenderReport(IReportRunnable report, String outputFormat, Map options,
                                   Map parameters, OutputStream outputStream) throws ReportServiceException {

        IRunAndRenderTask task;
        try{
            task = createRunAndRenderTask(report, outputFormat, options, parameters, outputStream);
        }catch (ReportServiceException e){
            throw new ReportServiceException("Exception thrown while creating run and render task", e);
        }

        try{
            task.run();
        }catch (BirtException e){
            task.cancel();
            throw new ReportServiceException("Exception thrown while running report", e);
        }finally{
            task.close();
        }

    }

}
