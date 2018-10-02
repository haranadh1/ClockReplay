/*
 *
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of ClockReplay Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from ClockReplay Incorporated.
 */

/*
 * CRErrorHandling is a common class in CRP project, for displaying
 * error messages from error.properties file
 *
 */
package com.crp.common;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Category;

import com.crp.common.GLOBAL_CONSTANTS;
/**
 * Generic Error Handling Class.
 * @author hpoduri
 * @version $Id$
 */
public class CRErrorHandling
{

    /**
     * Initializing Logger.
     */
    private static Category   log      = CRPLogger
                                           .initializeLogger("com.crp.common.CRErrorHandling");
    /**
     * properties. we only read the properties file once. the Properties class
     * is thread safe; refer to java documentation.
     */
    private static Properties crpProps = null;

    /**
     * disable constructor. we dont want anyone instantiating this class.
     */
    private CRErrorHandling()
    {
    }
	/**
	 * this method loads the property file into Properties obj.
	 * This is not thread safe.
	 * The method should be called only once at the start of the
	 * process. once the class is loaded, the getError from
	 * properties is thread safe.
	 */
	public static void load() {
        try
        {
            String galoreRoot = System.getenv(GLOBAL_CONSTANTS.CRP_ENV);
            String fileSep = System.getProperty("file.separator");
            CRPPropertyReader crpPropertyReader = new CRPPropertyReader(
                galoreRoot + fileSep + "properties" + fileSep
                    + "error.properties");
            crpProps = crpPropertyReader.loadPropertiesFile();
        }
        catch (IOException inOutException)
        {
            log.error("Input Output exception while reading prop file  ");
        }
        catch (NullPointerException nullPointException)
        {
            log.error("Null pointer exception while reading prop file  ");
            nullPointException.printStackTrace();

        }
        catch (Exception e)
        {
            log.error("Unexpected error while reading prop file  ");
        }
    }
	/**
	 * returns formatted error message for a given error code.
	 * This method searches for error message in properties file based on
	 * error code ; It reads the properties from the file for the first
	 * time and hold on to the properties (in memory) for future references.
	 * Throws error if the path to properties file is wrong
	 * or if there is any I/O error
	 * @param  errorCode - This is error code as input.
	 * @param  args String array
	 * @return errorMessage.
	 */
    public static String getError(final String errorCode, final String[] args)
    {
        // Start of try block
        String errorMessage = null;
        /**
         * Open for reading, a resource of the specified name from the search
         * path used to load classes ; only for the first time
         **/
        if (crpProps != null)
        {
            errorMessage = crpProps.getProperty(errorCode);

            /* If There is no error code in the properties file */
            if (errorMessage == "" || errorMessage == null)
            {
                // log in the log file
                log.error("Unable to find the key in prop file:  " + errorCode);
                // set the error message to the original code
                errorMessage = " Error code " + errorCode
                    + " not found in prop file";
            }
            else
            {
                // handle arguments.
                if (args != null && args.length > 0)
                {
                    MessageFormat mf = new MessageFormat(errorMessage);
                    errorMessage = mf.format(args);
                }
            }
        }
        else
        {
            log.error("Not Initialized; should call load() method first");
        }
        return errorMessage;
    }
/**
 * main method.
 * @param s args
 */
    public static void main(final String[] s)
    {
        // Load properties into memory
        CRErrorHandling.load();
        log.info("Properties loaded into memory ");
        log.debug("Error from prop file :"
            + CRErrorHandling.getError("CRP_JAVA_APP_ERR", null));
    }
}
