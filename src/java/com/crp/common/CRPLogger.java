/*
 * ClockReplayLogger is a common class in CRP project, for application logging
 *
 */
package com.crp.common;

import org.apache.log4j.Category;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Java Log4j class for ClockReplay.
 * @author gopi
 * @version $Id$
 */
public class CRPLogger
{
    /** To create an instance of logger for the class */
    static final String LOG_PROPERTIES_FILE = System
                                                .getenv(GLOBAL_CONSTANTS.CRP_ENV)
                                                + System
                                                    .getProperty("file.separator")
                                                + "xml"
                                                + System
                                                    .getProperty("file.separator")
                                                + "log4j.xml";
/**
 * disable constructor.
 */
    private CRPLogger()
    {
    }
/**
 * instantiate logger object for a given class.
 * @param className class name to be used for logging.
 * @return logger object.
 */

    public static Category initializeLogger(final String className)
    {
        Category log = null;
        boolean anyError = false;
        int counter = 0;
        do
        {
            try
            {
                counter++;
                DOMConfigurator.configure(LOG_PROPERTIES_FILE);
                log = Category.getInstance(className);
                anyError = false;
            }
            catch (Exception e)
            {
                anyError = true;
                System.out.println(e.getMessage());
                //throw new RuntimeException("Unable to load logging property "
                //    + LOG_PROPERTIES_FILE);
            }
        }
        while (anyError && counter < 2);
        return log;
    }
/**
 * test main method.
 * @param args arguments.
 */
    public static void main(final String[] args)
    {
        Category log = CRPLogger.initializeLogger("com.crp.");
        log.info("Error from prop file :"
            + CRErrorHandling.getError("CRP_JAVA_APP_ERR", null));
        // Log4J is now loaded;
        log.info("leaving the main method of Log4JDemo");
    }
}
