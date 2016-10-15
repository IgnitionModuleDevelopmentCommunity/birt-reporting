package com.tamakicontrol.modules.client.scripting;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.tamakicontrol.modules.scripting.AbstractReportUtils;
import com.tamakicontrol.modules.scripting.ReportUtilProvider;

import java.util.List;
import java.util.Map;

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
    protected List<Object> getReportsImpl(boolean includeData){
        return rpc.getReports(includeData);
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
    protected List<Map<String, Object>> getReportParametersImpl(long id){
        return rpc.getReportParameters(id);
    }

    @Override
    protected List<Map<String, Object>>  getReportParametersImpl(String name){
        return rpc.getReportParameters(name);
    }

}
