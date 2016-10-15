package com.tamakicontrol.modules.scripting;

import java.util.List;
import java.util.Map;

public abstract class AbstractReportUtils implements ReportUtilProvider{

    @Override
    public long saveReport(long id, String name, String description, byte[] reportData) {
        return saveReportImpl(id, name, description, reportData);
    }

    protected abstract long saveReportImpl(long id, String name, String description, byte[] reportData);

    @Override
    public byte[] getReport(long id) {
        return getReportImpl(id);
    }

    protected abstract byte[] getReportImpl(long id);

    @Override
    public byte[] getReport(String name) {

        return getReportImpl(name);
    }

    protected abstract byte[] getReportImpl(String name);

    @Override
    public List<Object> getReports() {
        return getReports(false);
    }

    @Override
    public List<Object> getReports(boolean includeData) {
        return getReportsImpl(includeData);
    }

    protected abstract List<Object> getReportsImpl(boolean includeData);

    @Override
    public boolean removeReport(long id) {
        return removeReportImpl(id);
    }

    protected abstract boolean removeReportImpl(long id);

    @Override
    public boolean removeReport(String name) {
        return removeReportImpl(name);
    }

    protected abstract boolean removeReportImpl(String name);

    @Override
    public List<Map<String, Object>> getReportParameters(long id) {
        return getReportParametersImpl(id);
    }

    protected abstract List<Map<String, Object>> getReportParametersImpl(long id);

    @Override
    public List<Map<String, Object>> getReportParameters(String name) {
        return getReportParametersImpl(name);
    }

    protected abstract List<Map<String, Object>>  getReportParametersImpl(String name);

}
