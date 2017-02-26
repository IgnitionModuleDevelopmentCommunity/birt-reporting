package com.tamakicontrol.modules.scripting;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

import java.util.Arrays;

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
    public long saveReport(long id, String name, String description, byte[] reportData){
        return saveReportImpl(id, name, description, reportData);
    }

    @Override
    public long saveReport(String name, String description, byte[] reportData){
        return saveReportImpl(-1L, name, description, reportData);
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
    public boolean reportExists(long id) {
        return reportExistsImpl(id);
    }

    protected abstract boolean reportExistsImpl(long id);

    @Override
    public boolean reportExists(String name) {
        return reportExistsImpl(name);
    }

    protected abstract boolean reportExistsImpl(String name);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public Dataset getReports(){
        return getReportsImpl();
    }

    protected abstract Dataset getReportsImpl();

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    @KeywordArgs(
            names={"reportId", "reportName", "outputFormat", "parameters", "options"},
            types={Long.class, String.class, String.class, PyDictionary.class, PyDictionary.class}
    )
    public byte[] runAndRenderReport(PyObject[] objects, String[] keywords) throws IllegalArgumentException{
        PyArgumentMap pyArgs = PyArgumentMap.interpretPyArgs(objects, keywords, this.getClass(), "runAndRenderReport");

        Long reportId = pyArgs.getLongArg("reportId");
        String reportName = pyArgs.getStringArg("reportName");
        String outputFormat = pyArgs.getStringArg("outputFormat");
        PyDictionary parameters = (PyDictionary)pyArgs.get("parameters");
        PyDictionary options = (PyDictionary)pyArgs.get("options");

        validateRunAndRenderParameters(reportId, reportName, outputFormat, parameters, options);

        return runAndRenderReport(reportId, reportName, outputFormat, parameters, options);
    }

    @Override
    public byte[] runAndRenderReport(long reportId, String reportName, String outputFormat,
                                     PyDictionary parameters, PyDictionary options) throws IllegalArgumentException{

        validateRunAndRenderParameters(reportId, reportName, outputFormat, parameters, options);
        return runAndRenderReportImpl(reportId, reportName, outputFormat, parameters, options);
    }

    private void validateRunAndRenderParameters(Long reportId, String reportName, String outputFormat,
                                                  PyDictionary parameters, PyDictionary options
    ) throws IllegalArgumentException {
        if(reportId == null && reportName == null)
            throw new IllegalArgumentException("Must specify either a report id or name");
        else if(reportId == null && reportName.equals(""))
            throw new IllegalArgumentException("Report name cannot be blank");

        if(!Arrays.asList(new String[] {"html", "pdf", "xls", "xlsx", "doc", "docx"}).contains(outputFormat.toLowerCase()))
            throw new IllegalArgumentException("Invalid output format specified");
    }

    protected abstract byte[] runAndRenderReportImpl(long reportId, String reportName, String outputFormat,
                                                     PyDictionary parameters, PyDictionary options);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public boolean removeReport(@ScriptArg("id") long id){
        return removeReportImpl(id);
    }

    protected abstract boolean removeReportImpl(long id);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public boolean removeReport(@ScriptArg("name") String name){
        return removeReportImpl(name);
    }

    protected abstract boolean removeReportImpl(String name);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public String getReportParameters(@ScriptArg("id") long id){
        return getReportParametersImpl(id);
    }

    protected abstract String getReportParametersImpl(long id);

    @Override
    @ScriptFunction(docBundlePrefix = "ReportUtils")
    public String getReportParameters(@ScriptArg("name") String name){
        return getReportParametersImpl(name);
    }

    protected abstract String  getReportParametersImpl(String name);

}
