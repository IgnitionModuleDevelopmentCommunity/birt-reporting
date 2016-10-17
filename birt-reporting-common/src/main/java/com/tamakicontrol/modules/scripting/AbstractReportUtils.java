package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

public abstract class AbstractReportUtils implements ReportUtilProvider{

    static {
        BundleUtil.get().addBundle(
                AbstractReportUtils.class.getSimpleName(),
                AbstractReportUtils.class.getClassLoader(),
                AbstractReportUtils.class.getName().replace('.', '/')
        );
    }

    public static final int PARAMETER_CONTROL_TEXT_BOX = 0;
    public static final int PARAMETER_CONTROL_LIST_BOX = 1;
    public static final int PARAMETER_CONTROL_RADIO_BUTTON = 2;
    public static final int PARAMETER_CONTROL_CHECK_BOX = 3;

    public static final int SELECTION_LIST_NONE = 0;
    public static final int SELECTION_LIST_DYNAMIC = 1;
    public static final int SELECTION_LIST_STATIC = 2;

    public static final int PARAMETER_DATATYPE_ANY = 0;
    public static final int PARAMETER_DATATYPE_STRING = 1;
    public static final int PARAMETER_DATATYPE_FLOAT = 2;
    public static final int PARAMETER_DATATYPE_DECIMAL = 3;
    public static final int PARAMETER_DATATYPE_DATE_TIME = 4;
    public static final int PARAMETER_DATATYPE_BOOLEAN = 5;
    public static final int PARAMETER_DATATYPE_INTEGER = 6;
    public static final int PARAMETER_DATATYPE_DATE = 7;
    public static final int PARAMETER_DATATYPE_TIME = 8;


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
    @KeywordArgs(
            names={"reportId", "reportName", "outputFormat", "parameters", "options"},
            types={Long.class, String.class, String.class, PyDictionary.class, PyDictionary.class}
    )

    public byte[] runAndRenderReport(PyObject[] objects, String[] keywords) {
        PyArgumentMap pyArgs = PyArgumentMap.interpretPyArgs(objects, keywords, this.getClass(), "runAndRenderReport");

        Long reportId = pyArgs.getLongArg("reportId");
        String reportName = pyArgs.getStringArg("reportName");
        String outputFormat = pyArgs.getStringArg("outputFormat");
        PyDictionary parameters = (PyDictionary)pyArgs.get("parameters");
        PyDictionary options = (PyDictionary)pyArgs.get("options");

        return runAndRenderReport(reportId, reportName, outputFormat, parameters, options);
    }

    @Override
    public byte[] runAndRenderReport(long reportId, String reportName, String outputFormat,
                                     PyDictionary parameters, PyDictionary options) {
        return runAndRenderReportImpl(reportId, reportName, outputFormat, parameters, options);
    }

    protected abstract byte[] runAndRenderReportImpl(long reportId, String reportName, String outputFormat,
                                                     PyDictionary parameters, PyDictionary options);

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
