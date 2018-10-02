/**
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved. NOTICE: All
 * information contained herein is, and remains the property of ClockReplay
 * Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from ClockReplay Incorporated.
 */

package com.crp.db;

import java.io.File;
import java.io.FilenameFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.JSON_KEYS;

/**
 * utility functions for db.
 * @author hpoduri
 * @version $Id$
 */
public class DBUtils
{

    /**
     * default constructor.
     */
    public DBUtils()
    {
        
    }
    /**
     * returns all the files in a given dir.
     * @param inputDir input directory to read file names from.
     * @return file names in a json object.
     */
    public static final JSONObject listFilesInDir(
        final String inputDir) throws DBException
    {
        JSONObject jo = new JSONObject();
        
        File dbDir = new File(inputDir);
        
        // filter for only crp_work_load files.
        FilenameFilter filter = new FilenameFilter() 
        {
            public boolean accept(File dir, String name) 
            {
                return name.startsWith("crp_work_load");
            }
        };
        
        File [] fileList = dbDir.listFiles(filter);
        try
        {
            jo.put(
                JSON_KEYS.JsonKeyStrings.JSON_CRP_DB_META_DATA,
                new JSONArray());
            
            for (int i = 0; i < fileList.length; i++)
            {
                
                JSONObject fileDetails = new JSONObject();
                fileDetails.put(
                    JSON_KEYS.JsonKeyStrings.JSON_CRP_DB_FILE_DETAILS,
                    new JSONArray());
                fileDetails.append(
                    JSON_KEYS.JsonKeyStrings.JSON_CRP_DB_FILE_DETAILS,
                    new JSONObject().put(
                    JSON_KEYS.JsonKeyStrings.JSON_CRP_DB_FILE_NAME,
                        fileList[i].getName()));
                fileDetails.append(
                    JSON_KEYS.JsonKeyStrings.JSON_CRP_DB_FILE_DETAILS,
                    new JSONObject().put(
                    JSON_KEYS.JsonKeyStrings.JSON_CRP_DB_NUM_DB_PAGES,
                        fileList[i].length()/ GLOBAL_CONSTANTS.DB_PAGE_SIZE));
                jo.append(
                    JSON_KEYS.JsonKeyStrings.JSON_CRP_DB_META_DATA,
                    fileDetails);
            }
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jo;
        
    }
}
