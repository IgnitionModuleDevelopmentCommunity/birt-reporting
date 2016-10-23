package com.tamakicontrol.modules.servlets;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/*
* Base Servlet
*
* Adds routing and utility methods to our servlet classes.  All servlet methods (doGet, doPost etc.) will be passed
* to a doRequest method which will route the request to the appropriate resource (implemented as a java interface).
*
* */
public class BaseServlet extends HttpServlet {

    public static final String URI_BASE = "/main/system/birt-reporting";

    protected static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    protected HashMap<String, ServletResource> router = new HashMap<>();

    protected void doRequest(String requestType, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{
        logger.debug(String.format("Request: [%s] %s", requestType, req.getRequestURI()));

        //TODO remove this when you're done debugging
        resp.addHeader("Access-Control-Allow-Origin", "*");

        for(String route : router.keySet()){

            if(req.getRequestURI().matches(URI_BASE + route)){
                if(Arrays.asList(router.get(route).getAllowedMethods()).contains(requestType)) {
                    router.get(route).doRequest(req, resp);
                    return;
                }else{
                    resp.sendError(405);
                    return;
                }
            }
        }

        resp.sendError(404);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest("GET", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest("POST", req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest("POST", req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest("DELETE", req, resp);
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

    protected GatewayContext getContext() {
        GatewayContext context = (GatewayContext)getServletContext().getAttribute(GatewayContext.SERVLET_CONTEXT_KEY);
        return context;
    }

}
