package com.tamakicontrol.modules.servlets;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.tamakicontrol.modules.utils.ArgumentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
* Base Servlet
*
* Adds routing and utility methods to our servlet classes.  All servlet methods (doGet, doPost etc.) will be passed
* to a doRequest method which will route the request to the appropriate resource (implemented as a java interface).
*
* */
public abstract class BaseServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    protected static final String METHOD_GET = "GET";
    protected static final String METHOD_POST = "POST";
    protected static final String METHOD_PUT = "PUT";
    protected static final String METHOD_DELETE = "DELETE";

    private HashMap<String, HashMap<String, ServletResource>> router = new HashMap<>();
    private ServletResource defaultResource;

    public abstract String getUriBase();

    private void doRequest(String requestMethod, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logger.debug(String.format("Request: [%s] %s", requestMethod, req.getRequestURI()));

        //TODO remove this when you're done debugging.  It would be great to have a debug switch that enables/disables
        resp.addHeader("Access-Control-Allow-Origin", "*");

        ServletResource resource = null;
        for (String route : router.keySet()) {

            if (req.getRequestURI().matches(getUriBase() + route)) {

                if (router.get(route).containsKey(requestMethod))
                    resource = router.get(route).get(requestMethod);
                else
                    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

                break;
            }
        }
        if (resource != null)
            try {
                resource.doRequest(req, resp);
            } catch (IllegalArgumentException e1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        else if(defaultResource != null)
            defaultResource.doRequest(req, resp);
        else
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);

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

    protected void addResource(String path, String method, ServletResource resource){

        HashMap<String, ServletResource> routerResource = router.get(path);

        if(routerResource == null){
            routerResource = new HashMap<>();
            routerResource.put(method, resource);
            router.put(path, routerResource);
        }else{
            routerResource.put(method, resource);
            router.put(path, routerResource);
        }
    }

    public ServletResource getDefaultResource(){
        return this.defaultResource;
    }

    protected void setDefaultResource(ServletResource resource){
        this.defaultResource = resource;
    }

    protected ArgumentMap getRequestParams(String queryString){
        ArgumentMap parameterMap = new ArgumentMap( );

        if(queryString == null)
            return null;

        try {
            String[] parameters = URLDecoder.decode(queryString, "UTF-8").split("&");
            if(parameters.length > 0){
                for(int i=0; i < parameters.length; i++){

                    String[] keyValuePair = parameters[i].split("=");


                    if(keyValuePair.length > 1) {
                        logger.trace(String.format("Key: %s, Value: %s", keyValuePair[0], keyValuePair[1]));
                        parameterMap.put(keyValuePair[0], keyValuePair[1]);
                    } else {
                        logger.trace(String.format("Key: %s, Value: %s", keyValuePair[0], true));
                        parameterMap.put(keyValuePair[0], true);
                    }
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

    protected String getURIComponent(String regex, String requestURI){
        Pattern pattern = Pattern.compile(getUriBase() + regex);
        Matcher matcher = pattern.matcher(requestURI);

        if(matcher.matches()){
            return matcher.group(1);
        }else{
            return null;
        }
    }

    protected String getRequestURLBase(String regex, String requestURL){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(requestURL);

        if(matcher.matches()){
            return matcher.group(1);
        }else{
            return null;
        }

    }

}
