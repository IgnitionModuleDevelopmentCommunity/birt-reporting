package com.tamakicontrol.modules.client.components;

import com.inductiveautomation.ignition.client.util.gui.ErrorUtil;
import com.inductiveautomation.vision.api.client.components.model.AbstractVisionPanel;
import org.apache.commons.lang3.StringUtils;
import org.icepdf.core.pobjects.Catalog;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.MyAnnotationCallback;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.util.PropertiesManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class PDFViewerComponent extends AbstractVisionPanel {

    //<editor-fold desc="Properties">
    private SwingController controller;
    public SwingController getController(){
        return this.controller;
    }

    private String filePath = "";
    public String getFilePath(){
        return filePath;
    }

    public void setFilePath(String filePath){
        String old = this.filePath;
        this.filePath = filePath;

        boolean unchanged = StringUtils.equals(old, filePath);
        if(controller != null && !unchanged) {
            boolean success = false;
            if (StringUtils.isNoneBlank(filePath))
                success = tryOpen(filePath);

            if(!success)
                controller.closeDocument();

        }

        firePropertyChange("filePath", old, filePath);
    }

    public static final int PAGE_FIT_MODE_NONE = 1;
    public static final int PAGE_FIT_MODE_ACTUAL = 2;
    public static final int PAGE_FIT_MODE_FITHEIGHT = 3;
    public static final int PAGE_FIT_MODE_FITWIDTH = 4;
    private int pageFitMode = PAGE_FIT_MODE_NONE;
    public int getPageFitMode(){
        return this.pageFitMode;
    }
    public void setPageFitMode(int pageFitMode){
        this.pageFitMode = pageFitMode;

        if(controller != null)
            controller.setPageFitMode(pageFitMode, true);

    }

    public static final int PAGE_VIEW_MODE_ONE_PAGE = 1;
    public static final int PAGE_VIEW_MODE_TWO_COLUMN_LEFT = 2;
    public static final int PAGE_VIEW_MODE_TWO_PAGE_LEFT = 3;
    public static final int PAGE_VIEW_MODE_TWO_COLUMN_RIGHT = 4;
    public static final int PAGE_VIEW_MODE_TWO_PAGE_RIGHT = 5;
    private int pageViewMode = PAGE_VIEW_MODE_ONE_PAGE;
    public int getPageViewMode(){
        return this.pageViewMode;
    }
    public void setPageViewMode(int pageViewMode){
        this.pageViewMode = pageViewMode;

        if(controller != null)
            controller.setPageViewMode(pageViewMode, true);

    }

    private boolean toolbarVisible = true;
    public boolean getToolbarVisible(){
        return toolbarVisible;
    }
    public void setToolbarVisible(boolean toolbarVisible){
        this.toolbarVisible = toolbarVisible;

        if(controller != null)
            controller.setToolBarVisible(toolbarVisible);

    }

    private boolean utilityVisible = false;
    public boolean getUtilityVisible(){
        return this.utilityVisible;
    }
    public void setUtilityVisible(boolean utilityVisible){
        this.utilityVisible = utilityVisible;

        if(controller != null)
            controller.setUtilityPaneVisible(utilityVisible);

    }

    public boolean footerVisible = true;
    public boolean getFooterVisible(){
        return this.footerVisible;
    }
    public void setFooterVisible(boolean footerVisible){
        this.footerVisible = footerVisible;

        if(footer != null)
            footer.setVisible(footerVisible);

    }

    private float zoomFactor = 0.0F;
    public float getZoomFactor(){
        return this.zoomFactor;
    }
    public void setZoomFactor(float zoomFactor){
        this.zoomFactor = zoomFactor;

        if(controller != null)
            controller.setZoom(zoomFactor);

    }
    //</editor-fold>

    private final Logger logger = LoggerFactory.getLogger("pdf-vewer");

    // GUI
    private static final Dimension PREFERRED_SIZE = new Dimension(300, 500);
    private JPanel viewerComponentPanel;
    private JPanel footer;

    public JToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(JToolBar toolBar) {
        this.toolBar = toolBar;
    }

    protected JToolBar toolBar;



    public PDFViewerComponent(){
        super(new BorderLayout());
    }

    @Override
    protected void onStartup() {

        controller = new SwingController(){
            @Override
            protected void applyViewerPreferences(Catalog catalog, PropertiesManager propertiesManager) {
                super.applyViewerPreferences(catalog, propertiesManager);

                setToolbarVisible(toolbarVisible);
                setUtilityVisible(utilityVisible);
                setPageFitMode(pageFitMode, true);
                setPageViewMode(pageViewMode, true);
            }
        };

        controller.setPageFitMode(pageFitMode, true);
        controller.setPageViewMode(pageViewMode, true);

        controller.getDocumentViewController().setAnnotationCallback(
                new MyAnnotationCallback(controller.getDocumentViewController()));

        SwingViewBuilder factory = new SwingViewBuilder(controller) {

            @Override
            public JPanel buildStatusPanel(){
                footer = super.buildStatusPanel();
                return footer;
            }

            @Override
            public JToolBar buildCompleteToolBar(boolean embeddableComponent) {
                toolBar = super.buildCompleteToolBar(embeddableComponent);

                if(toolBar != null)
                    toolBar.setVisible(toolbarVisible);

                return toolBar;
            }

            @Override
            public JTabbedPane buildUtilityTabbedPane() {
                JTabbedPane utilityPane = super.buildUtilityTabbedPane();

                if(utilityPane != null)
                    utilityPane.setVisible(utilityVisible);

                return utilityPane;
            }
        };

        viewerComponentPanel = factory.buildViewerPanel();
        ComponentKeyBinding.install(controller, viewerComponentPanel);

        setPreferredSize(PREFERRED_SIZE);
        add(viewerComponentPanel, BorderLayout.CENTER);

        /*
         * Open the given file in a new thread to keep the page loading quickly
         * */
        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run() {
                footer.setVisible(footerVisible);
                if(StringUtils.isNotBlank(filePath))
                    tryOpen(filePath);
            }
        });

    }

    @Override
    protected void onShutdown() {
        controller.dispose();
    }

    private boolean tryOpen(String path){
        File f = new File(path);
        boolean success = false;
        boolean isFile = f.isFile() && f.canRead();

        if(controller != null){
            if(isFile) {
                controller.openDocument(path);
                success = true;
            }
        }else{
            try{
                URL url = new URL(path);
                controller.openDocument(url);
                success = true;
            }catch (MalformedURLException e){
                logger.error("Error opening url", e);
            }
        }

        return success;
    }

    public void loadPDFBytes(byte[] pdfBytes){
        loadPDFBytes(pdfBytes, "");
    }

    public void loadPDFBytes(byte[] pdfBytes, String description){
        controller.openDocument(new ByteArrayInputStream(pdfBytes), description, null);
    }

    public void print(){
        print(true);
    }

    public void print(boolean withDialog){
        if(controller != null){
            if(controller.getDocument() !=  null){
                controller.print(withDialog);
            }else{
                ErrorUtil.showError("No PDF Document Loaded");
            }
        }else{
            ErrorUtil.showError("No PDF Controller Loaded");
        }
    }



}
