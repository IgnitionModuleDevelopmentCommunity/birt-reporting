package com.tamakicontrol.modules.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Created by cwarren on 10/19/16.
 */
public class EngineLogger extends java.util.logging.Logger {

    private final Logger logger = LoggerFactory.getLogger("birt-reporting-engine");

    protected EngineLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    @Override
    public void log(LogRecord record) {
        super.log(record);
    }

    @Override
    public void log(Level level, String msg) {
        super.log(level, msg);
    }

    @Override
    public void log(Level level, Supplier<String> msgSupplier) {
        super.log(level, msgSupplier);
    }

    @Override
    public void log(Level level, String msg, Object param1) {
        super.log(level, msg, param1);
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        super.log(level, msg, params);
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        super.log(level, msg, thrown);
    }

    @Override
    public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        super.log(level, thrown, msgSupplier);
    }

    @Override
    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        super.logp(level, sourceClass, sourceMethod, msg);
    }

    @Override
    public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
        super.logp(level, sourceClass, sourceMethod, msgSupplier);
    }

    @Override
    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
        super.logp(level, sourceClass, sourceMethod, msg, param1);
    }

    @Override
    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
        super.logp(level, sourceClass, sourceMethod, msg, params);
    }

    @Override
    public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
        super.logp(level, sourceClass, sourceMethod, msg, thrown);
    }

    @Override
    public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
        super.logp(level, sourceClass, sourceMethod, thrown, msgSupplier);
    }

    @Override
    public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
        super.logrb(level, sourceClass, sourceMethod, bundle, msg, thrown);
    }


}
