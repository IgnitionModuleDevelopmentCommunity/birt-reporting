package com.tamakicontrol.modules.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public interface ServletResource {

    public String[] getAllowedMethods();

    public void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

}
