package com.tamakicontrol.modules.records;

import com.inductiveautomation.ignition.gateway.localdb.persistence.*;
import simpleorm.dataset.SFieldFlags;

public class ReportRecord extends PersistentRecord {

    public static final RecordMeta<ReportRecord> META = new RecordMeta<ReportRecord>(
            ReportRecord.class, "ReportsRecord").setNounKey("ReportsRecord.Noun").setNounPluralKey(
            "ReportsRecord.Noun.Plural");

    public static final IdentityField Id = new IdentityField(META);

    public static final StringField Name = new StringField(META, "Name", SFieldFlags.SMANDATORY);
    public static final StringField Description = new StringField(META, "Description");
    public static final BlobField ReportData = new BlobField(META, "ReportData");


    public long getId(){
        return getLong(Id);
    }
    public void setId(long id){
        setLong(Id, id);
    }

    public String getName(){
        return getString(Name);
    }
    public void setName(String name){
        setString(Name, name);
    }

    public String getDescription(){
        return getString(Description);
    }
    public void setDescription(String description){
        setString(Description, description);
    }

    public byte[] getReportData(){
        return getBytes(ReportData);
    }
    public void setReportData(byte[] reportData){
        setBytes(ReportData, reportData);
    }

    @Override
    public RecordMeta<?> getMeta() {
        return META;
    }

}
