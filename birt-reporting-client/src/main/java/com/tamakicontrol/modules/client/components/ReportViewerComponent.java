package com.tamakicontrol.modules.client.components;

import com.inductiveautomation.factorypmi.application.binding.DynamicPropertyDescriptor;
import com.inductiveautomation.factorypmi.application.script.builtin.ClientNetUtilities;
import com.inductiveautomation.factorypmi.application.script.builtin.ClientSystemUtilities;
import com.inductiveautomation.ignition.client.script.ClientFileUtilities;
import com.inductiveautomation.ignition.client.util.gui.LoadingLabel;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.FileUtilities;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;
import com.tamakicontrol.modules.client.scripting.ClientReportUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;



//TODO ReportViewerComponent Extension functions to customize tool bar
//TODO ReportViewerComponent Add Validation Method (there could be a BIRT call that does this)
public class ReportViewerComponent extends PDFViewerComponent {

    //<editor-fold desc="Properties">
    public static final int REPORT_FORMAT_PDF = 0;
    public static final int REPORT_FORMAT_XLS = 1;
    public static final int REPORT_FORMAT_DOCX = 2;
    public static final int REPORT_FORMAT_HTML = 3;
    public static final int REPORT_FORMAT_XLSX = 4;

    /*
     * Enumerated report format field.  Used when saving/generating a report to request specific BIRT output.
     * */
    private int reportFormat = REPORT_FORMAT_PDF;

    public int getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(int reportFormat) {
        if (reportFormat >= 0 && reportFormat <= 4) {
            int old = this.reportFormat;
            this.reportFormat = reportFormat;
            firePropertyChange("reportFormat", old, reportFormat);
            reportFormatDropdown.setSelectedIndex(reportFormat);
        }
    }

    private Dataset renderOptions;

    public Dataset getRenderOptions() {
        return renderOptions;
    }

    public void setRenderOptions(Dataset renderOptions) {
        this.renderOptions = renderOptions;
    }

    //TODO build datasets of render options for different output types
    public Dataset getDefaultPDFRenderOptions() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("pdfRenderOption.fitToPage", "pdfRenderOption.pagebreakPaginationOnly",
                "pdfRenderOption.pageOverflow", "pdfRenderOption.pageLimit");
        builder.colTypes(Boolean.class, Boolean.class, Boolean.class, Integer.class);
        return builder.build();
    }

    /*
     *
     * */
    private Long reportId = null;

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        Long old = this.reportId;
        this.reportId = reportId;
        firePropertyChange("reportId", old, reportId);

        if (reportId != null)
            queryReportParams(reportId);
    }

    private Dataset reportParams;

    public Dataset getReportParams() {
        return this.reportParams;
    }

    public void setReportParams(Dataset reportParams) {
        Dataset old = this.reportParams;
        this.reportParams = reportParams;
        firePropertyChange("reportParams", old, reportParams);

        if (reportParams != null)
            createCustomProperties();
    }

    private void queryReportParams(final Long reportId) {
        logger.debug("Querying Report Parameters");
        if (reportId != null)
            setReportParams(reportUtils.getReportParameters(reportId));
    }


    private boolean loading = false;

    public boolean isLoading() {
        return loading;
    }

    public synchronized void setLoading(boolean loading) {
        this.loading = loading;
        loadingLabel.setVisible(loading);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        boolean old = this.loaded;
        this.loaded = loaded;
        firePropertyChange("loaded", old, loaded);
        reportFormatDropdown.setEnabled(loaded);
    }

    private boolean loaded = false;


    //</editor-fold>

    private final Logger logger = LoggerFactory.getLogger("BIRT Report Viewer Component");
    private final ClientReportUtils reportUtils = new ClientReportUtils();

    // TODO ReportViewerComponent LoadingLabel is public for debugging
    public LoadingLabel loadingLabel = new LoadingLabel();

    public ReportViewerComponent() {
        setPreferredSize(new Dimension(400, 400));

        loadingLabel.setOpaque(true);
        loadingLabel.setFillBackground(true);
        loadingLabel.setIcon(LoadingLabel.LOADING_GIF);
        loadingLabel.setVisible(false);

        add(loadingLabel, BorderLayout.CENTER);
    }

    // TODO remove generic type after JDK 6 support lapses
    /*
     *
     * Defined e
     *
     * */
    JComboBox reportFormatDropdown = new JComboBox(new String[]{"PDF", "Excel", "Word", "Web Page", "XLSX"});

    @Override
    protected void onStartup() {
        super.onStartup();

        setLoaded(false);

        // copy existing print button and remove existing save & print buttons from toolbar
        JToolBar saveToolBar = (JToolBar) getToolBar().getComponentAtIndex(0);
        JButton saveButton = (JButton) saveToolBar.getComponentAtIndex(0);
        JButton printButton = (JButton) saveToolBar.getComponentAtIndex(1);
        saveToolBar.remove(1);
        saveToolBar.remove(0);

        JToolBar reportExportToolbar = new JToolBar();


        reportFormatDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();
                logger.info(String.format("Setting parent to selected index %d", comboBox.getSelectedIndex()));
                setReportFormat(comboBox.getSelectedIndex());
            }
        });
        reportExportToolbar.add(reportFormatDropdown);

        // remove all existing action listeners from save button
        for (ActionListener listener : saveButton.getActionListeners()) {
            saveButton.removeActionListener(listener);
        }

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveReport();
            }
        });

        reportExportToolbar.add(saveButton);
        reportExportToolbar.add(printButton);

        getToolBar().addSeparator();
        getToolBar().add(reportExportToolbar);

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                queryReportParams(reportId);
            }
        });
    }

    private synchronized void createCustomProperties() {

        logger.debug("Creating Custom Properties");
        TreeMap<String, DynamicPropertyDescriptor> propertyMap = getDynamicProps() != null
                ? getDynamicProps() : new TreeMap<String, DynamicPropertyDescriptor>();

        ArrayList<String> propertyNames = new ArrayList<String>();

        // add any missing properties to the dynamic property list
        for (int i = 0; i < reportParams.getRowCount(); i++) {
            String name = (String) reportParams.getValueAt(i, "name");
            String description = "";
            DynamicPropertyDescriptor pd;

            propertyNames.add(name);

            if (!propertyMap.containsKey(name)) {

                switch ((Integer) reportParams.getValueAt(i, "dataType")) {
                    case 0:
                        pd = new DynamicPropertyDescriptor(name, description, Object.class);
                        break;
                    case 1:
                        pd = new DynamicPropertyDescriptor(name, description, String.class);
                        break;
                    case 2:
                        pd = new DynamicPropertyDescriptor(name, description, Float.class);
                        break;
                    case 3:
                        pd = new DynamicPropertyDescriptor(name, description, Double.class);
                        break;
                    case 4:
                        pd = new DynamicPropertyDescriptor(name, description, Date.class);
                        break;
                    case 5:
                        pd = new DynamicPropertyDescriptor(name, description, Boolean.class);
                        break;
                    case 6:
                        pd = new DynamicPropertyDescriptor(name, description, Integer.class);
                        break;
                    case 7:
                        pd = new DynamicPropertyDescriptor(name, description, Date.class);
                        break;
                    case 8:
                        pd = new DynamicPropertyDescriptor(name, description, Date.class);
                        break;
                    default:
                        pd = new DynamicPropertyDescriptor(name, description, Object.class);
                        break;
                }

                pd.setValue("prop.category", "Report Parameters");
                propertyMap.put(name, pd);
                logger.debug(String.format("Adding Custom Property %s", name));
            }

        }

        // remove any existing dynamic props that aren't in the dataset
        ArrayList<String> toRemove = new ArrayList<String>();
        for (String key : propertyMap.keySet()) {
            if (!propertyNames.contains(key)) {
                toRemove.add(key);
                logger.debug(String.format("Removing Custom Property %s", key));
            }
        }

        propertyMap.keySet().removeAll(toRemove);

        setDynamicProps(propertyMap);
        firePropertyChange("actionConfiguration", false, true);

    }

    private Map<String, Object> getParameterValues() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        for (String key : getDynamicProps().keySet()) {
            params.put(key, getPropertyValue(key));
        }

        return params;
    }

    //<editor-fold desc="Behavior">

    private static final String[] REPORT_FORMAT_OPTIONS = {"pdf", "xls", "doc", "html", "xlsx", "pdf"};

    private String getReportFormatStr(int format) {
        if (reportFormat >= 1 && reportFormat <= 4) {
            return REPORT_FORMAT_OPTIONS[format];
        } else {
            return "pdf";
        }
    }


    public void renderReport() {

        setLoading(true);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                byte[] reportBytes = generateReport(REPORT_FORMAT_PDF);
                loadPDFBytes(reportBytes);
            }
        });

    }

    public void saveReport() {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String filePath = getSaveFile();
                byte[] reportBytes = generateReport(reportFormat);
                saveReportFile(filePath, reportBytes);
            }
        });
    }

    private byte[] generateReport(int format) {

        final String reportFormatString = getReportFormatStr(format);
        final Map<String, Object> params = getParameterValues();

        // determine best default render options for client component
        Map<String, Object> renderOptions;
        if (format == REPORT_FORMAT_HTML) {
            renderOptions = getDefaultHTMLRenderOptions();
        } else {
            renderOptions = new HashMap<String, Object>();
        }

        byte[] bytes = reportUtils.runAndRenderReport(reportId, reportFormatString, params, renderOptions);
        setLoading(false);
        setLoaded(true);

        return bytes;
    }

    private String getSaveFile() {
        JFileChooser fileChooser = ClientFileUtilities.getChooser(1, "", "." + getReportFormatStr(reportFormat));

        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            // Automatically add a file extentions
            // TODO definitely some bugs here... Need to match "." @ end of file and replace
            if (!Arrays.asList(REPORT_FORMAT_OPTIONS).contains(FilenameUtils.getExtension(filePath))) {
                filePath = String.format("%s.%s", filePath, getReportFormatStr(reportFormat));
            }

            return filePath;
        }

        return null;
    }

    /*
     * TODO JavaDoc
     * */
    private void saveReportFile(String filePath, byte[] bytes) {
        if (filePath != null) {
            try {
                FileUtilities.writeFile(filePath, bytes);
                ClientNetUtilities.openURL(String.format("file://%s", filePath));
            } catch (IOException e) {
                logger.error(String.format("Error while writing report %s", e.getMessage()));
            }
        }

    }


    // TODO I think these should possibly be globally default render options for HTML because its the only way images will show up
    private HashMap<String, Object> getDefaultHTMLRenderOptions() {
        HashMap<String, Object> renderOptions = new HashMap<String, Object>();
        renderOptions.put("baseImageURL", String.format("%s/system/birt-reporting/api/images/", ClientSystemUtilities.getGatewayAddress()));
        renderOptions.put("image-handler", "server");
        return renderOptions;
    }

    //</editor-fold>

}
