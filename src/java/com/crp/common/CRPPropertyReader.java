/**
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

/**
 * ClockReplayPropertyReader is a common class in CRP project, for reading
 * properties file, which will be sent as an argument in the constructor
 */

package com.crp.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author gopi
 * @version $Id$
 */
public class CRPPropertyReader {

	/**
	 * String variable to store properties file path
	 */
	private String propertiesFilePath;
	/**
	 * initiate the property file path.
	 * @param propFilePath file path.
	 */
	public CRPPropertyReader(final String propFilePath) {
			this.propertiesFilePath = propFilePath;
	}
	/**
	 * default constructor.
	 */
	public CRPPropertyReader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * loads property file into property object.
	 * @return errorProperty : properties object
	 * @throws IOException throw io exception if
	 * any error while reading.
	 */
	public final Properties loadPropertiesFile()
	    throws IOException {
		/*Creating the object of Properties Class*/
		Properties errorProperty = new Properties();
		try {
			FileInputStream propertyStream = new FileInputStream(
			    propertiesFilePath);
			// Loading the context of property file
			errorProperty.load(propertyStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return errorProperty;
	}
}
