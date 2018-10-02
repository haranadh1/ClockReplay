package com.crp.memmgr.tests;

import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.ObjectFactory;

public class StringObjFactory implements ObjectFactory{
	
	public StringObjFactory() {
		
	}
	public String createObjectInstance() {
		return (new String("vivek"));
	}
	public void resetObject(Object obj) {
		String s = (String)obj;
		s = null;
	}
	public int getObjSize() {
		return 10;
	}
    @Override
    public String getMyName()
    {
        return "string";
    }
    @Override
    public boolean anyVarFields()
    {
        return true;
    }
    @Override
    public boolean anyRoomForObjOfVarLength(int length)
    {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public Object createObjectInstance(MemoryBlockByteBuffer mbb)
    {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public int getVarLenFieldsSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void reset()
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void reset(MemoryBlockByteBuffer inpMBB)
    {
        // TODO Auto-generated method stub
        
    }
}
