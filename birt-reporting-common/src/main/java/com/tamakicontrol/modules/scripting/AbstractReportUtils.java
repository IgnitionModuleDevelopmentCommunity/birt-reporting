package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;
import org.python.core.PyObject;

public abstract class AbstractReportUtils implements ReportUtilProvider{

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public long saveReport(long id, String name, String description, byte[] reportData) {
        return saveReportImpl(id, name, description, reportData);
    }

    protected abstract long saveReportImpl(long id, String name, String description, byte[] reportData);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public byte[] getReport(@ScriptArg("id") long id) {
        return getReportImpl(id);
    }

    protected abstract byte[] getReportImpl(long id);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public byte[] getReport(@ScriptArg("name") String name) {

        return getReportImpl(name);
    }

    protected abstract byte[] getReportImpl(String name);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public Dataset getReports() {
        return getReportsImpl();
    }


    protected abstract Dataset getReportsImpl();

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public String getReportsAsJSON() {
        return getReportsAsJSONImpl();
    }

    protected abstract String getReportsAsJSONImpl();

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public byte[] runAndRenderReport(PyObject[] objects, String[] keywords) {
        PyArgumentMap.interpretPyArgs(objects, keywords, this.getClass(), "runAndRenderReport");
        return null;
    }

    protected abstract byte[] runAndRenderReportImpl();

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public boolean removeReport(@ScriptArg("id") long id) {
        return removeReportImpl(id);
    }

    protected abstract boolean removeReportImpl(long id);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public boolean removeReport(@ScriptArg("name") String name) {
        return removeReportImpl(name);
    }

    protected abstract boolean removeReportImpl(String name);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public Dataset getReportParameters(@ScriptArg("id") long id) {
        return getReportParametersImpl(id);
    }

    protected abstract Dataset getReportParametersImpl(long id);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public Dataset getReportParameters(@ScriptArg("name") String name) {
        return getReportParametersImpl(name);
    }

    protected abstract Dataset  getReportParametersImpl(String name);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public String getReportParametersAsJSON(@ScriptArg("id") long id) {
        return getReportParametersAsJSONImpl(id);
    }

    protected abstract String getReportParametersAsJSONImpl(@ScriptArg("id") long id);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public String getReportParametersAsJSON(@ScriptArg("name") String name) {
        return getReportParametersAsJSONImpl(name);
    }

    protected abstract String getReportParametersAsJSONImpl(String name);
}
