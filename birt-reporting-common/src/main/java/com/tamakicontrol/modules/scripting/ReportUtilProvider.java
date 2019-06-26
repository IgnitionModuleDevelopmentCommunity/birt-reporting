package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.Dataset;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

import java.util.Map;

public interface ReportUtilProvider {

    public Dataset getReports();

    public boolean reportExists(long id);

    public boolean reportExists(String name);

    public byte[] getReport(long id);

    public byte[] getReport(String name);

    public long saveReport(long id, String name, String description, byte[] reportData);

    public long saveReport(String name, String description, byte[] reportData);

    public boolean removeReport(long id);

    // TODO remove reportName
    public boolean removeReport(String name);

    public byte[] runAndRenderReport(PyObject[] objects, String[] keywords);

    //TODO remove reportName or create separate function
    public byte[] runAndRenderReport(long reportId, String reportName, String outputFormat,
                                     PyDictionary parameters, PyDictionary options);

    public byte[] runAndRenderReport(long reportId, String outputFormat, Map<String, Object>parameters,
                                     Map<String, Object>options);

    public Dataset getReportParameters(long id);

}