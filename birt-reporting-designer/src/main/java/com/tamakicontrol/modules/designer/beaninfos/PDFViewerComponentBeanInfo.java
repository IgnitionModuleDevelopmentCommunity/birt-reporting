package com.tamakicontrol.modules.designer.beaninfos;

import com.inductiveautomation.vision.api.designer.beans.CommonBeanInfo;
import com.inductiveautomation.vision.api.designer.beans.VisionBeanDescriptor;
import com.tamakicontrol.modules.client.components.PDFViewerComponent;

import javax.swing.*;
import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;


public class PDFViewerComponentBeanInfo extends CommonBeanInfo {

    public PDFViewerComponentBeanInfo(){
        super(PDFViewerComponent.class);
    }

    @Override
    protected void initProperties() throws IntrospectionException {
        // Adds common properties
        super.initProperties();

        addProp("filePath", "File Path", "File Path or URL", CAT_DATA, PREFERRED_MASK | BOUND_MASK);

        addProp("zoomFactor", "Zoom", "Zoom Factor", CAT_BEHAVIOR, PREFERRED_MASK | BOUND_MASK);

        String[] pageFitModes = {"None", "Actual", "Fit Height", "Fit Width"};
        int[] pageFitModeValues = {PDFViewerComponent.PAGE_FIT_MODE_NONE, PDFViewerComponent.PAGE_FIT_MODE_ACTUAL,
                PDFViewerComponent.PAGE_FIT_MODE_FITHEIGHT, PDFViewerComponent.PAGE_FIT_MODE_FITWIDTH};
        addEnumProp("pageFitMode", "Page Fit Mode", "Page Fit Mode", CAT_APPEARANCE,
                pageFitModeValues, pageFitModes, PREFERRED_MASK | BOUND_MASK);

        String[] pageViewModes = {"One Page", "Two Column Left", "Two Page Left", "Two Column Right", "Two Page Right"};
        int[] pageViewModeValues = {PDFViewerComponent.PAGE_VIEW_MODE_ONE_PAGE,
                PDFViewerComponent.PAGE_VIEW_MODE_TWO_COLUMN_LEFT, PDFViewerComponent.PAGE_VIEW_MODE_TWO_PAGE_LEFT,
                PDFViewerComponent.PAGE_VIEW_MODE_TWO_COLUMN_RIGHT, PDFViewerComponent.PAGE_VIEW_MODE_TWO_PAGE_RIGHT};

        addEnumProp("pageViewMode", "Page View Mode", "Page View Mode", CAT_APPEARANCE,
                pageViewModeValues, pageViewModes, PREFERRED_MASK | BOUND_MASK);
        addProp("toolbarVisible", "Toolbar Visible", "Toolbar Visible", CAT_APPEARANCE, PREFERRED_MASK | BOUND_MASK);
        addProp("utilityVisible", "Utility Visible", "Utility Pane Visible", CAT_APPEARANCE);
        addProp("footerVisible", "Footer Visible", "Footer Visible", CAT_APPEARANCE);
    }

    @Override
    public Image getIcon(int kind) {
        switch (kind) {
            case BeanInfo.ICON_COLOR_16x16:
                return new ImageIcon(getClass().getResource("/images/pdf-viewer_16x16.png")).getImage();
            case BeanInfo.ICON_MONO_16x16:
                return new ImageIcon(getClass().getResource("/images/pdf-viewer-mono_16x16.png")).getImage();
            case SimpleBeanInfo.ICON_COLOR_32x32:
                return new ImageIcon((getClass().getResource("/images/pdf-viewer_32x32.png"))).getImage();
            case SimpleBeanInfo.ICON_MONO_32x32:
                return new ImageIcon(getClass().getResource("/images/pdf-viewer-mono_32x32.png")).getImage();
        }
        return null;
    }

    @Override
    protected void initDesc() {
        VisionBeanDescriptor bean = getBeanDescriptor();
        bean.setName("PDF Viewer");
        bean.setDisplayName("PDF Viewer");
        bean.setShortDescription("Tamaki PDF Viewer Component");
    }

}
