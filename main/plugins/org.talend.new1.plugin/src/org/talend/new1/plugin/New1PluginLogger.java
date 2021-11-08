package org.talend.new1.plugin;

import org.talend.commons.exception.ExceptionHandler;

public class New1PluginLogger {
    public static void log() {
        ExceptionHandler.log("R2022-03 the plugins: " + Activator.PLUGIN_ID + " started -20211108");
    }

}
