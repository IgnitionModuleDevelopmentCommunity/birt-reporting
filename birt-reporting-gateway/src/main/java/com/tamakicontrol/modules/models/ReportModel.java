package com.tamakicontrol.modules.models;

import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.db.schema.DBTableSchema;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ReportModel {

    private static final Logger logger = LoggerFactory.getLogger(ReportModel.class);

    private long Id;

    private String name;

    private String description;

    private byte[] reportData;

    private Date createdDate;
    private String createdBy;
    private Date modifiedDate;
    private String modifiedBy;


    public void initializeTable(GatewayContext context){
        SRConnection con;
        DBTableSchema table;
        try {
            con = context.getDatasourceManager().getConnection("");
            table = new DBTableSchema("", con.getParentDatasource().getTranslator());
            table.addRequiredColumn("id", DataType.Int8);
            table.addRequiredColumn("name", DataType.String);
            table.addRequiredColumn("description", DataType.String);
            table.addRequiredColumn("reportData", DataType.BooleanArray);
            table.addRequiredColumn("createdDate", DataType.DateTime);
            table.addRequiredColumn("createdBy", DataType.String);
            table.addRequiredColumn("modifiedDate", DataType.DateTime);
            table.addRequiredColumn("modifiedBy", DataType.String);
            table.verifyAndUpdate(con);
            con.close();
        }catch (Exception e){
            logger.error(String.format("Error while creating reports table: %s", e.getMessage()));
        }

    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getReportData() {
        return reportData;
    }

    public void setReportData(byte[] reportData) {
        this.reportData = reportData;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }


}
