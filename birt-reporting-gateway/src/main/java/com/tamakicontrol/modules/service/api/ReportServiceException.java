package com.tamakicontrol.modules.service.api;

public class ReportServiceException extends Exception {

    public ReportServiceException(String message){
        super(message);
    }

    public ReportServiceException(String message, Throwable e){
        super(message, e);
    }

}
