package com.tamakicontrol.modules.client;

import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook;
import com.tamakicontrol.modules.client.scripting.ClientReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHook extends AbstractClientModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void startup(ClientContext context, LicenseState activationState) throws Exception {
        super.startup(context, activationState);
        logger.info("Starting up BIRT Reporting Module");
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
