package com.tamakicontrol.modules.client.scripting;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.tamakicontrol.modules.scripting.AbstractReportUtils;
import com.tamakicontrol.modules.scripting.ReportUtilProvider;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

public class ClientReportUtils extends AbstractReportUtils{

    private final ReportUtilProvider rpc;

    public ClientReportUtils(){
        rpc = ModuleRPCFactory.create(
                "com.tamakicontrol.modules.birt-reporting",
                ReportUtilProvider.class
        );
    }

    @Override
    protected long saveReportImpl(long id, String name, String description, byte[] reportData) {
        return rpc.saveReport(id, name, description, reportData);
    }

    @Override
    protected byte[] getReportImpl(long id){
        return rpc.getReport(id);
    }

    @Override
    protected byte[] getReportImpl(String name){
        return rpc.getReport(name);
    }

    @Override
    protected Dataset getReportsImpl(){
        return rpc.getReports();
    }

    @Override
    protected String getReportsAsJSONImpl() {
        return rpc.getReportsAsJSON();
    }

    @Override
    protected byte[] runAndRenderReportImpl(long reportId, String reportName, String outputFormat, PyDictionary parameters, PyDictionary options) {
        return rpc.runAndRenderReport(reportId, reportName, outputFormat, parameters, options);
    }

    @Override
    protected boolean removeReportImpl(long id){
        return rpc.removeReport(id);
    }

    @Override
    protected boolean removeReportImpl(String name){
        return rpc.removeReport(name);
    }

    @Override
    protected Dataset getReportParametersImpl(long id){
        return rpc.getReportParameters(id);
    }

    @Override
    protected Dataset getReportParametersImpl(String name){
        return rpc.getReportParameters(name);
    }

    @Override
    protected String getReportParametersAsJSONImpl(long id) {
        return rpc.getReportParametersAsJSON(id);
    }

    @Override
    protected String getReportParametersAsJSONImpl(String name) {
        return rpc.getReportParametersAsJSON(name);
    }

}
