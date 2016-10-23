package com.tamakicontrol.modules.designer;

import com.inductiveautomation.factorypmi.designer.palette.model.PaletteLoader;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.vision.api.designer.VisionDesignerInterface;
import com.inductiveautomation.vision.api.designer.palette.JavaBeanPaletteItem;
import com.inductiveautomation.vision.api.designer.palette.Palette;
import com.inductiveautomation.vision.api.designer.palette.PaletteItemGroup;
import com.tamakicontrol.modules.client.components.PDFViewerComponent;
import com.tamakicontrol.modules.client.scripting.ClientReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesignerHook extends AbstractDesignerModuleHook {

    private final Logger logger = LoggerFactory.getLogger("birt-reporting");

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        super.startup(context, activationState);

        context.addBeanInfoSearchPath("com.tamakicontrol.modules.designer.beaninfos");

        VisionDesignerInterface vdi = (VisionDesignerInterface) context
                .getModule(VisionDesignerInterface.VISION_MODULE_ID);

        if(vdi != null) {
            Palette pallete = vdi.getPalette();

            PaletteItemGroup group = pallete.addGroup("BIRT Reporting");
            group.addPaletteItem(new JavaBeanPaletteItem(PDFViewerComponent.class));
        }

        BundleUtil.get().addBundle("AbstractReportUtils", ClientReportUtils.class, "AbstractReportUtils");

    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        manager.addScriptModule("system.report", new ClientReportUtils(), new PropertiesFileDocProvider());
    }

}
