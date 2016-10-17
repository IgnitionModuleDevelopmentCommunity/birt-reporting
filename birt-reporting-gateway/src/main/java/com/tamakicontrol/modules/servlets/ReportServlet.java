package com.tamakicontrol.modules.servlets;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.DatasetUtilities;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.scripting.GatewayReportUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.birt.report.engine.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");
    private GatewayReportUtils reportUtils;

    @Override
    public void init() throws ServletException {
        super.init();
        reportUtils = new GatewayReportUtils(getContext());
        router.put("/web/(.*)", staticResource);
        router.put("/api/reports", getReportsResource);
        router.put("/api/run-and-render", runAndRenderResource);
        router.put("/api/parameters", getParametersResource);
    }

    /*
        *
        * runAndRenderResource
        *
        * Arguments:
        *   reportId (int) - id of the report to render
        *   reportName (String) - name of the report to render
        *   parameters (Dictionary) - key value pairs of parameters for the report
        *   options (Dictionary) - key value pairs of options for the report
        *
        * Returns:
        *   Rendered report output
        *
        * */
    private ServletResource runAndRenderResource = new ServletResource() {

        @Override
        public String[] getAllowedMethods() {
            return new String[] {"GET"};
        }

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");

            String outputFormat = reportUtils.runAndRenderToStream(getRequestParams(req.getQueryString()), resp.getOutputStream());

            if(outputFormat.equalsIgnoreCase("html"))
                resp.setContentType("text/html");
            else if(outputFormat.equalsIgnoreCase("pdf"))
                resp.setContentType("text/pdf");
            else if(outputFormat.equalsIgnoreCase("xls"))
                resp.setContentType("Report.xls");
            else if(outputFormat.equalsIgnoreCase("doc"))
                resp.setContentType("Report.doc");
        }

    };

    /*
    *
    * getParametersResource
    *
    * Arguments:
    *   reportId - (int) Id of the report to get parameters for
    *   reportName - (String) Name of the report to get parameters for
    *
    * Returns:
    *   Report parameter data encoded as JSON
    *
    * */
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
            Dataset reportParams = null;

            if(requestParams.get("reportId") != null) {
                try {
                    Long reportId = Long.parseLong((String) requestParams.get("reportId"));
                    reportParams = reportUtils.getReportParameters(reportId);
                }catch(NumberFormatException e){
                    logger.warn("Parsing reportId from request failed");
                }
            }else if(requestParams.get("reportName") != null) {
                String reportName = (String) requestParams.get("reportName");
                reportParams = reportUtils.getReportParameters(reportName);
            }else{
                resp.sendError(400);
            }

            if(reportParams != null)
                resp.getWriter().print(DatasetUtilities.toJSONObject(reportParams).toString());
            else
                resp.sendError(400);
        }
    };

    /*
    *
    * getReportsResource
    *
    * Arguments:
    *   None
    *
    * Returns:
    *   List of reports in internal db as JSON
    *
    * */
    private ServletResource getReportsResource = new ServletResource() {
        @Override
        public String[] getAllowedMethods() {
            return new String[]{"GET"};
        }

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            Dataset data = reportUtils.getReports();

            resp.getWriter().print(DatasetUtilities.toJSONObject(data).toString());
        }
    };

    /*
    *
    * getStaticResource
    *
    * Arguments:
    *   None
    *
    * Returns:
    *   Text data from a static file
    *
    * */
    private ServletResource staticResource = new ServletResource() {
        @Override
        public String[] getAllowedMethods() {
            return new String[] {"GET"};
        }

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");

            Pattern pattern = Pattern.compile(URI_BASE + "/(.*)");
            Matcher matcher = pattern.matcher(req.getRequestURI());
            String filePath;


            if(matcher.find())
                filePath = matcher.group(1);
            else{
                resp.sendError(404);
                return;
            }

            logger.debug(String.format("Serving static file %s", filePath));

            InputStream fileStream = GatewayHook.class.getResourceAsStream(filePath);
            if(fileStream != null)
                IOUtils.copy(fileStream, resp.getOutputStream());
            else
                logger.warn(String.format("Resource not found %s", filePath));

        }
    };

}
