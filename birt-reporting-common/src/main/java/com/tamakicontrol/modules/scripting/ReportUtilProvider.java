package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;

import java.util.List;
import java.util.Map;

public interface ReportUtilProvider {

    public List<Object> getReports();

    public List<Object> getReports(boolean includeData);

    public byte[] getReport(long id);

    public byte[] getReport(String name);

    public long saveReport(long id, String name, String description, byte[] reportData);

    public boolean removeReport(long id);

    public boolean removeReport(String name);

    public List<Map<String, Object>> getReportParameters(long id);

    public List<Map<String, Object>> getReportParameters(String name);
}