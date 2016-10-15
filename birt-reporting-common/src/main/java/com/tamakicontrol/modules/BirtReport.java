package com.tamakicontrol.modules;

public class BirtReport {

    private long id = -1;
    private String name;
    private String description;
    private byte[] reportData;

    public BirtReport(long id, String name, String description, byte[] reportData){
        this.id = id;
        this.name = name;
        this.description = description;
        this.reportData = reportData;
    }

    public BirtReport(long id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
        this.reportData = null;
    }

    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public byte[] getReportData(){
        return this.reportData;
    }

    public void setReportData(byte[] reportData){
        this.reportData = reportData;
    }

}
