package com.crp.sqldb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.crp.common.CRPConfig;

/*
 * Test class is used to demonstrate DBConnect class methods
 */
public class Test {
	
	public void openConnection(){
		DBStore dbc=new DBStore();
		dbc.connect();
		System.out.println("JSON DOC: "+dbc.getJsonDoc());
		dbc.disconnect();
	}
	
	public void getNodes(){
		DBStore dbc=new DBStore();
		dbc.connect();
		ResultSet rs=dbc.select("nodes");
		try {
			while(rs.next()){
			      String t=rs.getString(1);
				if(t.length()>10){
				t=t.substring(0,10)+"...";
				}
				System.out.print("<div class=\"host_div\"><div class=\"img_div\" align=\"center\"><li><a class=\"opener\" id=\""+t+"\"><img src=\"images/host.gif\" alt=\""+t+"\" /></a></li></div><div class=\"text_div\" align=\"center\"><b>"+t+"</b></div></div>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbc.disconnect();
	}
	
	public void getHostData(){		
		DBStore db=new DBStore();
		db.connect();
		String doc =db.getJsonDoc();
		db.disconnect();
		CRPConfig cc=new CRPConfig();
		String host="ClockReplay";
		
		HashMap<String,String> hm1=cc.getHostCaptureData(doc, host);
		HashMap<String,String> hm2=cc.getHostReplayData(doc, host);
		
		System.out.print("<table class=\"tab\" width=300><caption><h2>Capture Services</h2></caption>");
		System.out.print("<tr><th>Port<th>Capture Service  </tr>");
		Iterator<String> key1=hm1.keySet().iterator();
		while(key1.hasNext()){
		String s=(String)key1.next();
		if(hm1.get(s)!=null){
		System.out.print("<tr>");
		System.out.print("<td> "+s+" <td> "+hm1.get(s));
		System.out.print("</tr>");	
		}	
		}
		
		System.out.print("</table>");
		System.out.print("<hr>");
		System.out.print("<table class=\"tab\" width=300><caption><h2>Replay Services</h2></caption>");
		System.out.print("<tr><th>Port<th>Replay Service  </tr>");
		Iterator<String> key2=hm2.keySet().iterator();
		while(key2.hasNext()){
		String s=(String)key2.next();
		if(hm2.get(s)!=null){
		System.out.print("<tr>");
		System.out.print("<td> "+s+" <td> "+hm2.get(s));
		System.out.print("</tr>");	
		}	
		}
		System.out.print("</table>");
	}
	
	public void addNode(){
		String host="Satyam";
		DBStore db=new DBStore();
		db.connect();
		ResultSet rs=db.select("nodes");
		try {
			boolean t=true;
			while(rs.next()){
				if(rs.getString(1).equals(host)){
					t=false;
					System.out.println("Already exists");
				}
			}
			if(t){
				ArrayList<String> a=new ArrayList<String>();
				a.add("node");
				ArrayList<String> b=new ArrayList<String>();
				b.add(host);
				db.insert("nodes", a, b);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
	}
	
	public void deleteNode(){
		String host="satya2";
		DBStore db=new DBStore();
		db.connect();
		db.delete("nodes","node='"+host+"'");
		db.disconnect();
	}
	
	public void getTabCont(){
		DBStore db=new DBStore();
		db.connect();
		db.update("nodes", "node='SatyamSoftSols'", "node = 'SatyamSofts'");
		String doc = db.getJsonDoc();
		CRPConfig cc=new CRPConfig();
		String str1=cc.getCapTabContent(doc);
		System.out.println(str1);
		String str2=cc.getRepTabContent(doc);
		System.out.println(str2);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*DBStore db=new DBStore();
		db.connect();
		System.out.println(db.getColumns("sales"));
		db.test();*/
		Test t=new Test();
//		t.openConnection();
//		t.getNodes();
//		t.getHostData();
//		t.addNode();
//		t.deleteNode();
		t.getTabCont();
	}

}
