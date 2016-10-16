package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.Dataset;
import org.python.core.PyObject;

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
    public Dataset getReports() {
        return getReportsImpl();
    }

    protected abstract Dataset getReportsImpl();

    @Override
    public byte[] runAndRenderReport(PyObject[] objects, String[] keywords) {
        return runAndRenderReportImpl(objects, keywords);
    }

    protected abstract byte[] runAndRenderReportImpl(PyObject[] objects, String[] keywords);

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
    public Dataset getReportParameters(long id) {
        return getReportParametersImpl(id);
    }

    protected abstract Dataset getReportParametersImpl(long id);

    @Override
    public Dataset getReportParameters(String name) {
        return getReportParametersImpl(name);
    }

    protected abstract Dataset  getReportParametersImpl(String name);

}
