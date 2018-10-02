package com.crp.memmgr.tests;

import com.crp.common.*;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.MemoryManager.MemoryBlockType;


public class ObjectPoolTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			MemoryManager.registerThread((short)0);
			MemoryManager.init(100*GLOBAL_CONSTANTS.KB);
			MemoryBlockObjectPool<String> mb = (MemoryBlockObjectPool<String>)MemoryManager.createMemoryBlock((short)0, 
											        1*GLOBAL_CONSTANTS.KB,
													MemoryManager.MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
													new StringObjFactory(),
													MemoryManager.MemoryAllocationType.NOT_VALID, null);
			for(int i=0;i<1000;i++) {
				String s1 = mb.getObject();
				if(s1 == null)
					break;
				System.out.println("got "+s1+" "+i);
			}
			
			mb.resetPool();
			for(int i=0;i<1000;i++) {
				String s1 = mb.getObject();
				if(s1 == null)
					break;
				System.out.println("got "+s1+" "+i);
			}
			
			
			MemoryBlockObjectPool<myclass> mb1 = (MemoryBlockObjectPool<myclass>)MemoryManager.createMemoryBlock((short)0, 
			        							1*GLOBAL_CONSTANTS.KB,
			        							MemoryManager.MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
			        							new myclassObjFactory(),
			        							MemoryManager.MemoryAllocationType.NOT_VALID, null);
			for(int i=0;i<1000;i++) {
				myclass m1 = mb1.getObject();
				if(m1 == null)
					break;
				System.out.println("got "+m1.a+" "+i);
			}
		}
		catch(MemoryManagerException e) {
			
		}
	}

}
