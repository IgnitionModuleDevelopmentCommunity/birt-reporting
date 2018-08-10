package com.tamakicontrol.modules.designer.beaninfos.editors;

import com.inductiveautomation.factorypmi.designer.property.editors.ConfiguratorEditorSupport;
import com.inductiveautomation.ignition.common.Dataset;
import com.tamakicontrol.modules.client.scripting.ClientReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class ReportEditor extends ConfiguratorEditorSupport {

    private static final Logger logger = LoggerFactory.getLogger("birt-reporting");

    private JComboBox comboBox;

    // TODO dynamicaly update report items
    private Vector<ReportEditorComboBoxItem> items;

    @Override
    protected void initComponents() {

        ClientReportUtils reportUtils = new ClientReportUtils();
        Dataset reports = reportUtils.getReports();

        items = new Vector<ReportEditorComboBoxItem>();
        for(int i = 0; i < reports.getRowCount(); i++){
            items.add(new ReportEditorComboBoxItem((Long)reports.getValueAt(i, "Id"), (String)reports.getValueAt(i, "Name")));
        }

        comboBox = new JComboBox(items);
        comboBox.setBorder(null);
        comboBox.setEditable(true);
        comboBox.setRenderer(new ReportEditorComboBoxItemRenderer());
        // TODO there has to be a better way to force a combo box to take up the entire container width
        comboBox.setPreferredSize(new Dimension(1200, 17));
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox chooser = (JComboBox) e.getSource();
                ReportEditorComboBoxItem item = (ReportEditorComboBoxItem) chooser.getSelectedItem();

                if(item != null) {
                    setValue(item);
                }
            }
        });

        this.panel.add(comboBox);
    }

    @Override
    public void setValue(Object value) {

        if(value instanceof Long){
            Long _value = (Long)value;
            ReportEditorComboBoxItem _item = null;
            for(ReportEditorComboBoxItem item : items){
                if(item.getId().equals(_value)){
                    _item = item;
                    break;
                }
            }

            super.setValue(_item);
            comboBox.setSelectedItem(_item);
        }else if(value instanceof ReportEditorComboBoxItem){
            super.setValue(value);
            comboBox.setSelectedItem(value);
        }else{
            super.setValue(null);
        }
    }

    @Override
    public Object getValue() {
        Object value = super.getValue();
        if(value != null){
            return ((ReportEditorComboBoxItem)value).getId();
        }

        return null;
    }

    private class ReportEditorComboBoxItemRenderer extends BasicComboBoxRenderer{
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if(value != null){
                ReportEditorComboBoxItem item = (ReportEditorComboBoxItem)value;
                setText(item.getName());
            }else{
                setText("CHOOSE A REPORT");
            }

            if(index < 0){
                setText("CHOOSE A REPORT");
            }

            return this;
        }
    }

    private class ReportEditorComboBoxItem {

        public ReportEditorComboBoxItem(Long id, String name){
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        private Long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private String name;


        @Override
        public String toString() {
            return name;
        }

    }

}
