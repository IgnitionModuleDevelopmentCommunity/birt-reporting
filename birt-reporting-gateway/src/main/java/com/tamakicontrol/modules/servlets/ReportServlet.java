package com.tamakicontrol.modules.servlets;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.records.ReportRecord;
import org.eclipse.birt.report.engine.api.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getRenderReport(req, resp);
    }

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

    protected Map<String, String> getRequestParams(String queryString){
        Map<String, String> parameterMap = new HashMap<>();

        if(queryString == null)
            return null;

        try {
            String[] parameters = URLDecoder.decode(queryString, "UTF-8").split("&");
            if(parameters.length > 0){
                for(int i=0; i < parameters.length; i++){
                    String[] keyValuePair = parameters[i].split("=");

                    logger.trace(String.format("Key: %s, Value: %s", keyValuePair[0], keyValuePair[1]));
                    parameterMap.put(keyValuePair[0], keyValuePair[1]);
                }
            }
        }catch (UnsupportedEncodingException e){
            logger.error("Unsupported Encoding", e);
        }

        return parameterMap;
    }

    private GatewayContext getContext() {
        GatewayContext context = (GatewayContext)getServletContext().getAttribute(GatewayContext.SERVLET_CONTEXT_KEY);
        return context;
    }

    private void getRenderReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String> requestParams = getRequestParams(req.getQueryString());
        IReportRunnable report = null;
        if(requestParams.get("report") != null){
            report = getReportFromDisk(requestParams.get("report"));
        }else if(requestParams.get("reportid") !=  null) {
            report = getReportFromDB(Long.parseLong(requestParams.get("reportid")));
        }else{
            resp.sendError(404, "null report");
        }

        if(report == null) {
            resp.sendError(404, report.getReportName());
            return;

        }

        IRunAndRenderTask task = GatewayHook.getReportEngine().createRunAndRenderTask(report);


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

    private void getReportParameters(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String> requestParams = getRequestParams(req.getQueryString());
        if(requestParams.get("report") == null){
            resp.sendError(404, "null report");
        }

        IReportRunnable report = getReportFromDisk(requestParams.get("report"));

        IGetParameterDefinitionTask task = GatewayHook.getReportEngine().createGetParameterDefinitionTask(report);

        Collection params = task.getParameterDefns(true);

        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject reportParam;

            Iterator iter = params.iterator();
            while (iter.hasNext()) {
                IParameterDefnBase param = (IParameterDefnBase) iter.next();
                logger.info(String.format("Parameter %s", param.toString()));
                reportParam = new JSONObject();
                reportParam.put("displayName", param.getDisplayName());
                reportParam.put("name", param.getName());
                reportParam.put("helpText", param.getHelpText());
                reportParam.put("proptText", param.getPromptText());
                reportParam.put("typeName", param.getTypeName());
                reportParam.put("parameterType", param.getParameterType());
                reportParam.put("userPropertyValues", param.getUserPropertyValues());
                jsonArray.put(reportParam);
            }
            resp.getWriter().write(jsonArray.toString());
        }catch(JSONException e){
            logger.error("JSON Exception when retrieving report parameters", e);
        }
    }


}
