/*********************************************************************************************
 * Copyright (c) 2014-2015  Software Behaviour Analysis Lab, Concordia University, Montreal, Canada
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of Eclipse Public License v1.0 License which
 * accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Syed Shariyar Murtaza -- Initial design and implementation
 **********************************************************************************************/
package org.eclipse.tracecompass.totalads.core;


import java.io.IOException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.tracecompass.totalads.core.Activator;
import org.eclipse.tracecompass.totalads.dbms.DBMSFactory;
import org.eclipse.tracecompass.totalads.exceptions.TotalADSUncaughtExceptionHandler;
import org.eclipse.tracecompass.totalads.algorithms.AlgorithmFactory;
import org.eclipse.tracecompass.totalads.readers.TraceTypeFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import airbrake.AirbrakeAppender;

/**
 * Activator for the plugin, called by Eclipse
 *
 * @author <p>
 *         Syed Shariyar Murtaza justsshary@hotmail.com
 *         </p>
 *
 */
public class Activator implements BundleActivator {

    private static BundleContext context;
    private static Logger log;

    static BundleContext getContext() {
        return context;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        init();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        deInitialize();
    }

    /**
     *
     * Initializes TotalADS
     *
     */

    private static void init() {
        try {

            AlgorithmFactory algFactory = AlgorithmFactory.getInstance();
            algFactory.initialize();

            TraceTypeFactory trcTypeFactory = TraceTypeFactory.getInstance();
            trcTypeFactory.initialize();

            // Initialize the regular java logger
            // Handler handler = null;
            //handler = new java.util.logging.SocketHandler("localhost", 5000); //$NON-NLS-1$
            //handler = new java.util.logging.FileHandler(getCurrentPath() + "totaladslog.xml"); //$NON-NLS-1$
            //Logger.getLogger("").addHandler(handler); //$NON-NLS-1$

            // Initialize the log4j logger
            log = Logger.getLogger(Activator.class.getName());
            initializeLog4j();
            Thread.setDefaultUncaughtExceptionHandler(new TotalADSUncaughtExceptionHandler());

        } catch (Exception ex) { // capture all the exceptions here
            // Regular java logger
            // Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
            // log4j logger
            log.error(ex.getMessage(), ex);
        }


    }

    /**
     * Initializes log4jProperties
     * @throws IOException IOException
     */
    private static void initializeLog4j() throws IOException {

        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "[%d,%p] [%c{1}.%M:%L] %m%n"; //$NON-NLS-1$
        PatternLayout patternLayout=new PatternLayout(PATTERN);
        console.setLayout(patternLayout);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);

        // File appender
        FileAppender fileAppender=new FileAppender(patternLayout, "totalADS-log4j.log"); //$NON-NLS-1$
        fileAppender.activateOptions();
        Logger.getRootLogger().addAppender(fileAppender);


        AirbrakeAppender appender=new AirbrakeAppender();
        appender.setApi_key("4f7d79c04de1d85410279b46efcbb0e2"); //$NON-NLS-1$
        appender.setEnv("production"); //$NON-NLS-1$
        appender.setEnabled(true);
        appender.setUrl("http://api.airbrake.io/notifier_api/v2/notices"); //$NON-NLS-1$
        appender.activateOptions();
        Logger.getRootLogger().addAppender(appender);

    }

    /**
     * DeInitializes TotalADS
     */
    private static void deInitialize() {
        DBMSFactory.INSTANCE.closeConnection();
        // This code deinitializes the Factory instance. It was necessary
        // because
        // if TotalADS plugin is reopened in running Eclipse, the static objects
        // are not
        // deinitialized on previous close of the plugin.
        AlgorithmFactory.destroyInstance();
        TraceTypeFactory.destroyInstance();
    }

}
