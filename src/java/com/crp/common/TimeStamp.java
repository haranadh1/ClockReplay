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

/**
 * Implementation of time stamp class
 * we can use System.nanotime() function call in java
 * to get the long value of the time stamp
 * With long value, the additions and finding the
 * difference between two time stamps would be easy.
 */

public class TimeStamp {

	private long ts;
	/**
	 * constructor.
	 * @param inpTS timestamp
	 */
	public TimeStamp(final long inpTS) {
		ts = inpTS;
	}
	/**
	 * returns timestamp in long.
	 * @return timestamp as long
	 */
	public final long getTimeStamp() {
		return ts;
	}
}
