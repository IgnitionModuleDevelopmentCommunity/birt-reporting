package com.tamakicontrol.modules.designer.beaninfos;

import com.inductiveautomation.vision.api.designer.beans.CommonBeanInfo;
import com.inductiveautomation.vision.api.designer.beans.VisionBeanDescriptor;
import com.tamakicontrol.modules.client.components.ReportViewerComponent;
import com.tamakicontrol.modules.designer.beaninfos.editors.ReportEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;

public class ReportViewerComponentBeanInfo extends CommonBeanInfo {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    public ReportViewerComponentBeanInfo(){
        super(ReportViewerComponent.class);
    }

    @Override
    protected void initProperties() throws IntrospectionException {
        // Adds common properties
        super.initProperties();

        addProp("reportParams", "Report Parameters", "Report Parameter Dataset", CAT_DATA, PREFERRED_MASK | BOUND_MASK);
        addProp("reportId", "Report", "Report", CAT_DATA, PREFERRED_MASK | BOUND_MASK, ReportEditor.class);

        String[] pageFitModes = {"None", "Actual", "Fit Height", "Fit Width"};
        int[] pageFitModeValues = {ReportViewerComponent.PAGE_FIT_MODE_NONE, ReportViewerComponent.PAGE_FIT_MODE_ACTUAL,
                ReportViewerComponent.PAGE_FIT_MODE_FITHEIGHT, ReportViewerComponent.PAGE_FIT_MODE_FITWIDTH};
        addEnumProp("pageFitMode", "Page Fit Mode", "Page Fit Mode", CAT_APPEARANCE,
                pageFitModeValues, pageFitModes, PREFERRED_MASK | BOUND_MASK);

        String[] pageViewModes = {"One Page", "Two Column Left", "Two Page Left", "Two Column Right", "Two Page Right"};
        int[] pageViewModeValues = {ReportViewerComponent.PAGE_VIEW_MODE_ONE_PAGE,
                ReportViewerComponent.PAGE_VIEW_MODE_TWO_COLUMN_LEFT, ReportViewerComponent.PAGE_VIEW_MODE_TWO_PAGE_LEFT,
                ReportViewerComponent.PAGE_VIEW_MODE_TWO_COLUMN_RIGHT, ReportViewerComponent.PAGE_VIEW_MODE_TWO_PAGE_RIGHT};
        addEnumProp("pageViewMode", "Page View Mode", "Page View Mode", CAT_APPEARANCE,
                pageViewModeValues, pageViewModes, PREFERRED_MASK | BOUND_MASK);

        String[] formats = {"PDF", "Excel", "Word", "HTML", "Excel+"};
        int[] formatValues = {ReportViewerComponent.REPORT_FORMAT_PDF, ReportViewerComponent.REPORT_FORMAT_XLS,
                ReportViewerComponent.REPORT_FORMAT_DOCX, ReportViewerComponent.REPORT_FORMAT_HTML,
                ReportViewerComponent.REPORT_FORMAT_XLSX};
        addEnumProp("reportFormat", "Report Format", "Report Format", CAT_BEHAVIOR,
                formatValues, formats, PREFERRED_MASK | BOUND_MASK);

        addProp("zoomFactor", "Zoom", "Zoom Factor", CAT_BEHAVIOR, PREFERRED_MASK | BOUND_MASK);
        addProp("toolbarVisible", "Toolbar Visible", "Toolbar Visible", CAT_APPEARANCE, PREFERRED_MASK | BOUND_MASK);
        addProp("utilityVisible", "Utility Visible", "Utility Pane Visible", CAT_APPEARANCE);
        addProp("footerVisible", "Footer Visible", "Footer Visible", CAT_APPEARANCE);
    }

    @Override
    public Image getIcon(int kind) {
        switch (kind) {
            case BeanInfo.ICON_COLOR_16x16:
                return new ImageIcon(getClass().getResource("/images/report-viewer_16x16.jpg")).getImage();
            case BeanInfo.ICON_MONO_16x16:
                return new ImageIcon(getClass().getResource("/images/report-viewer_16x16.jpg")).getImage();
            case SimpleBeanInfo.ICON_COLOR_32x32:
                return new ImageIcon(getClass().getResource("/images/report-viewer_32x32.jpg")).getImage();
            case SimpleBeanInfo.ICON_MONO_32x32:
                return new ImageIcon(getClass().getResource("/images/report-viewer_32x32.jpg")).getImage();
        }
        return null;
    }

    @Override
    protected void initDesc() {
        VisionBeanDescriptor bean = getBeanDescriptor();
        bean.setName("Report Viewer");
        bean.setDisplayName("Report Viewer");
        bean.setShortDescription("BIRT Report Viewer Component");
    }

}
