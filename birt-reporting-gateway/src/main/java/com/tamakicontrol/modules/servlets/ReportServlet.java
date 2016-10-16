package com.tamakicontrol.modules.servlets;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.DatasetUtilities;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
import com.tamakicontrol.modules.scripting.GatewayReportUtils;
import org.eclipse.birt.report.engine.api.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");
    private GatewayReportUtils reportUtils;

    @Override
    public void init() throws ServletException {
        super.init();
        reportUtils = new GatewayReportUtils(getContext());
        router.put("/viewer", runAndRenderResource);
        router.put("/api/parameters", getParametersResource);
    }

    private ServletResource runAndRenderResource = new ServletResource() {

        @Override
        public String[] getAllowedMethods() {
            return new String[] {"GET"};
        }

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");

            reportUtils.getRunAndRenderTask(getRequestParams(req.getQueryString()), resp.getOutputStream());
        }

        private IReportRunnable getReportFromDisk(String reportPath){
            try{
                return GatewayHook.getReportEngine().openReportDesign(reportPath);
            }catch(EngineException e){
                logger.error("Failed to open report", e);
            }

            return null;
        }
    };

    private ServletResource getParametersResource = new ServletResource() {

        @Override
        public String[] getAllowedMethods() {
            return new String[] {"GET"};
        }

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            Map<String, Object> requestParams = getRequestParams(req.getQueryString());
            if(requestParams.get("report") == null){
                resp.sendError(404, "null report");
            }

            Dataset reportParams = reportUtils.getReportParameters((Long)requestParams.get("reportid"));

            resp.getWriter().write(DatasetUtilities.toJSONObject(reportParams).toString());
        }
    };

    private IReportRunnable getReportFromDisk(String reportPath){
        try{
            return GatewayHook.getReportEngine().openReportDesign(reportPath);
        }catch(EngineException e){
            logger.error("Failed to open report", e);
        }

        return null;
    }

    private IReportRunnable getReportFromDB(long reportId){
        try{
            byte[] reportData = getContext().getPersistenceInterface().find(ReportRecord.META, reportId).getReportData();
            return GatewayHook.getReportEngine().openReportDesign(new ByteArrayInputStream(reportData));
        }catch (EngineException e){
            logger.error("Failed to open report", e);
        }

        return null;
    }

    private void getRenderReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> requestParams = getRequestParams(req.getQueryString());
        IReportRunnable report = null;
        if(requestParams.get("report") != null){
            report = getReportFromDisk((String)requestParams.get("report"));
        }else if(requestParams.get("reportid") !=  null) {
            report = getReportFromDB(Long.parseLong((String)requestParams.get("reportid")));
        }else{
            resp.sendError(404, "null report");
        }

        if(report == null) {
            resp.sendError(404, report.getReportName());
            return;

        }

        IRunAndRenderTask task = reportUtils.getRunAndRenderTask(requestParams, resp.getOutputStream());


        task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
                this.getClass().getClassLoader());

        requestParams.forEach((key,value) -> {
            if (key != "report") {
                logger.trace(String.format("Key: %s, Value: %s", key, value));
                task.setParameterValue(key, value);
            }
        });

        HTMLRenderOption options = new HTMLRenderOption();
        options.setOutputFormat("html");
        options.setOutputStream(resp.getOutputStream());
        options.setEmbeddable(false);

        task.setRenderOption(options);
        try{
            task.run();
        }
        catch(EngineException e1){
            logger.info("Exception while rendering report", e1);
        }
        finally{
            task.close();
        }
    }

}
