package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.Dataset;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

public interface ReportUtilProvider {

    public Dataset getReports();

    public String getReportsAsJSON();

    public byte[] getReport(long id);

    public byte[] getReport(String name);

    public byte[] runAndRenderReport(PyObject[] objects, String[] keywords);

    public byte[] runAndRenderReport(long reportId, String reportName, String outputFormat,
                                     PyDictionary parameters, PyDictionary options);

    public long saveReport(long id, String name, String description, byte[] reportData);

    public boolean removeReport(long id);

    public boolean removeReport(String name);

    public Dataset getReportParameters(long id);

    public Dataset getReportParameters(String name);

    public String getReportParametersAsJSON(long id);

    public String getReportParametersAsJSON(String name);

}