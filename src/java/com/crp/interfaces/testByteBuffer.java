package com.crp.interfaces;

import java.nio.ByteBuffer;

public class testByteBuffer
{
    
    public static void main(String [] args)
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        byte [] barray = bb.array();
        
        String str = "hello";
        System.arraycopy(barray, 0, str.getBytes(), 0, str.length());
        
        System.out.println("offset: " + String.valueOf(bb.position()+ String.valueOf(bb.limit())));
    }

}
