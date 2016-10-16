package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import org.python.core.PyObject;

import java.util.List;
import java.util.Map;

public interface ReportUtilProvider {

    public Dataset getReports();

    public byte[] getReport(long id);

    public byte[] getReport(String name);

    public byte[] runAndRenderReport(PyObject[] objects, String[] keywords);

    public long saveReport(long id, String name, String description, byte[] reportData);

    public boolean removeReport(long id);

    public boolean removeReport(String name);

    public Dataset getReportParameters(long id);

    public Dataset getReportParameters(String name);

}