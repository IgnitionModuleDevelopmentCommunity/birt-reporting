package com.tamakicontrol.modules.designer.beaninfos.editors;

import com.inductiveautomation.factorypmi.designer.property.editors.ConfiguratorEditorSupport;
import com.inductiveautomation.ignition.common.Dataset;
import com.tamakicontrol.modules.client.scripting.ClientReportUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class ReportEditor extends ConfiguratorEditorSupport {

    private JComboBox comboBox;

    @Override
    protected void initComponents() {

        ClientReportUtils reportUtils = new ClientReportUtils();
        Dataset reports = reportUtils.getReports();

        Vector items = new Vector();
        for(int i = 0; i < reports.getRowCount(); i++){
            //, (String)reports.getValueAt(i, "Name")
            items.add((Long)reports.getValueAt(i, "Id"));
        }

        comboBox = new JComboBox(items);
        comboBox.setBorder(null);
        comboBox.setEditable(true);

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox chooser = (JComboBox) e.getSource();
                Long item = (Long)chooser.getSelectedItem();

                if(item != null) {
                    setValue(item);
                }
            }
        });

        this.panel.add(comboBox, BorderLayout.CENTER);
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        comboBox.setSelectedItem(value);
    }

//    private class ComboBoxItem{
//
//        public ComboBoxItem(Long id, String name){
//            this.id = id;
//            this.name = name;
//        }
//
//        public Long getId() {
//            return id;
//        }
//
//        public void setId(Long id) {
//            this.id = id;
//        }
//
//        private Long id;
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        private String name;
//    }

}
