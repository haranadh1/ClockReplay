package com.crp.memmgr.tests;

import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.ObjectFactory;

public class myclassObjFactory implements ObjectFactory {
	public Object createObjectInstance() {
		return new myclass();
	}
	public void resetObject(Object obj) {
		myclass m = (myclass)obj;
		m.a = 0;
		m.b = 0;
		m.f = 0;
	}
	public int getObjSize() {
		return 16;
	}
    @Override
    public String getMyName()
    {
        return "MyClass";
    }
    @Override
    public boolean anyVarFields()
    {
        return false;
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
