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


package com.crp.common;

import org.apache.log4j.Category;

/**
 * Logger for common package.
 * @author hpoduri
 * @version $Id$
 */
public final class CommonLogger {
    /**
     * Logger for all the common classes.
     */
    public static final Category CMN_LOG = CRPLogger.initializeLogger(
        "com.crp.common");
	/**
	 * make constructor private.
	 */
	private CommonLogger() {
	}
}
