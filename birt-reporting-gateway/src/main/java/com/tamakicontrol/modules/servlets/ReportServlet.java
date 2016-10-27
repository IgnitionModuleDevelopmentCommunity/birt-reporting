package com.tamakicontrol.modules.servlets;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.DatasetUtilities;
import com.tamakicontrol.modules.GatewayHook;
import com.tamakicontrol.modules.scripting.GatewayReportUtils;
import com.tamakicontrol.modules.scripting.utils.ArgumentMap;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");
    private GatewayReportUtils reportUtils;

    @Override
    public String getUriBase() {
        return "/main/system/birt-reporting";
    }

    @Override
    public void init() throws ServletException {
        super.init();
        reportUtils = new GatewayReportUtils(getContext());
        addResource("/web/(.*)", METHOD_GET, getStaticResource);
        addResource("/api/reports", METHOD_GET, getReportsResource);
        addResource("/api/run-and-render", METHOD_GET, getRunAndRenderResource);
        addResource("/api/parameters", METHOD_GET, getParametersResource);
        addResource("/api/images/([^\\s]+(\\.(?i)(jpg|png|bmp|svg))$)", METHOD_GET, getImageResource);
    }

    /*
        *
        * runAndRenderResource
        *
        * Arguments:
        *   reportId (int) - id of the report to render
        *   reportName (String) - name of the report to render
        *   parameters (Dictionary) - key value pairs of parameters for the report.  We expect these parameters to come
        *   as a member of a "parameters" key.  For instance, parameters.RunID=8 parameters.BatchID=4 etc.
        *   options (Dictionary) - key value pairs of options for the report.  Expects the same format as parameters.
        *   (options.embeddible=1, options.hideGridlines=1)
        *
        * Returns:
        *   Rendered report output
        *
        * */
    private ServletResource getRunAndRenderResource = new ServletResource() {

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            ArgumentMap args = getRequestParams(req.getQueryString());

            Long reportId = args.getLongArg("reportId");
            String reportName = args.getStringArg("reportName");
            String outputFormat = args.getStringArg("outputFormat");
            HashMap<String, Object> parameters = new HashMap<>();
            HashMap<String, Object> options = new HashMap<>();

            args.keySet().forEach(key -> {
                if (key.matches("parameters.")) {
                    parameters.put(key.split("\\.")[1], args.get(key));
                } else if (key.matches("options.")) {
                    options.put(key.split("\\.")[1], args.get(key));
                }
            });

            String baseImageURL = getURIComponent("(http.*)/run-and-render", req.getRequestURL().toString()) + "/images";

            options.put("baseImageURL", baseImageURL);

            // Actually generate the report and get the content length
            ByteArrayOutputStream reportStream = new ByteArrayOutputStream();
            reportUtils.runAndRenderToStream(reportId, reportName, outputFormat,
                    parameters, options, reportStream);
            resp.setContentLength(reportStream.toByteArray().length);

            resp.setContentType("UTF-8");
            if (outputFormat == null) {
                resp.setContentType("tex/html");
            } else {
                if (outputFormat.equalsIgnoreCase("html"))
                    resp.setContentType("text/html");
                else if (outputFormat.equalsIgnoreCase("pdf"))
                    resp.setContentType("text/pdf");
                else if (outputFormat.equalsIgnoreCase("xlsx")) {
                    resp.setContentType("application/vnd-msexcel");
                    resp.setHeader("Content-Disposition", "attachment; filename=Report.xlsx");
                } else if (outputFormat.equalsIgnoreCase("docx")) {
                    resp.setContentType("application/vnd.ms-excel");
                    resp.setHeader("Content-Disposition", "attachment; filename=Report.doc");
                }
            }

            reportStream.writeTo(resp.getOutputStream());
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
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            ArgumentMap requestParams = getRequestParams(req.getQueryString());

            try{
                if(requestParams.getLongArg("reportId") != null)
                    resp.getWriter().print(reportUtils.getReportParameters(requestParams.getLongArg("reportId")));
                else if(requestParams.getStringArg("reportName") != null)
                    resp.getWriter().print(reportUtils.getReportParameters(requestParams.getStringArg("reportName")));
                else
                    resp.sendError(404);
            }catch (Exception e){
                logger.error("Exception throws while requesting report parameters", e);
            }
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
    private ServletResource getStaticResource = new ServletResource() {

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            String filePath = getURIComponent("/api/images/(.*)", req.getRequestURI());

            if(filePath == null)
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

            if(filePath.substring(filePath.length() - 1).equals("/"))
                filePath += "index.html";

            logger.debug(String.format("Serving static file %s", filePath));

            try(
                InputStream fileStream = GatewayHook.class.getResourceAsStream(filePath);
            ){
                IOUtils.copy(fileStream, resp.getOutputStream());
            }catch (IOException e){
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);

            }

        }
    };

    /*
    *
    * getImageResource
    *
    * Arguments:
    *   None
    *
    *
    * Returns:
    *   Image
    *
    * */
    private ServletResource getImageResource = new ServletResource() {

        @Override
        public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            String filePath = getURIComponent("/api/images/(.*)", req.getRequestURI());

            if(filePath == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String mime = getServletContext().getMimeType(filePath);
            if(mime == null){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            resp.setContentType(mime);

            File file = new File(getContext().getTempDir().getPath() + "/" + filePath);
            if(!file.exists()){
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            resp.setContentLength((int)file.length());

            logger.trace(String.format("Serving image from %s", filePath));
            InputStream fileStream = new FileInputStream(file);
            IOUtils.copy(fileStream, resp.getOutputStream());
            fileStream.close();
        }
    };


}
