package com.tamakicontrol.modules.designer;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.vision.api.designer.VisionDesignerInterface;
import com.inductiveautomation.vision.api.designer.palette.JavaBeanPaletteItem;
import com.inductiveautomation.vision.api.designer.palette.Palette;
import com.inductiveautomation.vision.api.designer.palette.PaletteItemGroup;
import com.tamakicontrol.modules.client.components.PDFViewerComponent;
import com.tamakicontrol.modules.client.components.ReportViewerComponent;
import com.tamakicontrol.modules.client.scripting.ClientReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesignerHook extends AbstractDesignerModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        super.startup(context, activationState);

        logger.info("Starting up BIRT Reporting Module");

        BundleUtil.get().addBundle("AbstractReportUtils", ClientReportUtils.class, "AbstractReportUtils");

        // Add the BeanInfo package to the search path
        context.addBeanInfoSearchPath("com.tamakicontrol.modules.designer.beaninfos");

        // Add my component to its own palette
        VisionDesignerInterface vdi = (VisionDesignerInterface) context
                .getModule(VisionDesignerInterface.VISION_MODULE_ID);

        if (vdi != null) {
            Palette palette = vdi.getPalette();

            PaletteItemGroup group = palette.addGroup("BIRT Reporting");
            group.addPaletteItem(new JavaBeanPaletteItem(PDFViewerComponent.class));
            group.addPaletteItem(new JavaBeanPaletteItem(ReportViewerComponent.class));
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);
        manager.addScriptModule("system.report", new ClientReportUtils(), new PropertiesFileDocProvider());
    }
}
