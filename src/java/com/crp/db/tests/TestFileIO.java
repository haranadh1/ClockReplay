package com.crp.db.tests;


import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Before;

import com.crp.common.CRErrorHandling;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.db.DBException;
import com.crp.db.FileIO;
import com.crp.db.FileIO.FileOpenFlag;

public class TestFileIO extends TestCase
{

    @Before
    public void setUp() throws Exception
    {
        
     // Load the error messages.
        CRErrorHandling.load();
        
    }
    /**
     * a simple test with memory mapped file interface.
     * @throws DBException
     */
    public final void testMemoryMapped() throws DBException
    {
        FileIO fio = new FileIO("c:\\downloads\\test_data.data",
            FileIO.IOType.MEMORY_MAPPED, GLOBAL_CONSTANTS.DB_PAGE_SIZE, 
            GLOBAL_CONSTANTS.DB_MAX_FILE_SIZE, FileOpenFlag.OPEN_FOR_WRITE);
        fio.setMemoryMappedBufferSize(GLOBAL_CONSTANTS.DB_PAGE_SIZE);
        
        
        for (int i = 0; i < GLOBAL_CONSTANTS.DB_MAX_FILE_SIZE/GLOBAL_CONSTANTS.DB_PAGE_SIZE; i++)
        {
            byte [] pageBuffer = createRandomDBPage();
            fio.write(pageBuffer);
        }
        fio.close();
    }
    public final void testDirectBuffer() throws DBException
    {
        FileIO fio = new FileIO("c:\\downloads\\test_direct_buffer.data",
            FileIO.IOType.DIRECT_BUFFER,
            GLOBAL_CONSTANTS.DB_PAGE_SIZE,
            GLOBAL_CONSTANTS.DB_MAX_FILE_SIZE, FileOpenFlag.OPEN_FOR_WRITE);
        
        for (int i = 0;
        i < GLOBAL_CONSTANTS.DB_MAX_FILE_SIZE/GLOBAL_CONSTANTS.DB_PAGE_SIZE;
        i++)
        {
            byte [] pageBuffer = createRandomDBPage();
            fio.write(pageBuffer);
        }
        fio.close();
    }
    private static byte [] createRandomDBPage()
    {
        byte [] pageBuffer = new byte[(int)GLOBAL_CONSTANTS.DB_PAGE_SIZE];
        int offset = 0;
        String uuid;
        do
        {
            //create a db page buffer.
            uuid = UUID.randomUUID().toString();
            int length = 0;
            if(offset + uuid.length() > GLOBAL_CONSTANTS.DB_PAGE_SIZE)
            {
                length = (int)GLOBAL_CONSTANTS.DB_PAGE_SIZE - offset;
            }
            else
            {
                length = uuid.length();
            }
            System.arraycopy(uuid.getBytes(), 0, pageBuffer, offset, length);
            offset += length;
            
        } while(offset <= (GLOBAL_CONSTANTS.DB_PAGE_SIZE -1));
        return pageBuffer;
    }
    public static void main(String[] args) 
    {
        junit.textui.TestRunner.run(TestFileIO.class);
    }

}
