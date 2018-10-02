package com.crp.sqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DBConnect deals with postgresql database. It performs all db operations i.e
 * connecting to/closing db, creating/dropping table,insertion into
 * table,selecting from table, deleting table records,updating table..etc.
 */
public class DBStore {

	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private PreparedStatement pstmt = null;

	/**
	 * Default constructor
	 */
	public DBStore() {
	}

	/**
	 * connect to db;
	 */
	public void connect() {
//		String dvrcls = "";
//		String url = "";
//		String db = "";
//		String uname = "";
//		String pwd = "";

		// default values
		
		  String dvrcls = "org.postgresql.Driver"; 
		  String url = "jdbc:postgresql"; 
		  String db = "mobilestore"; 
		  String uname = "ms";
		  String pwd = "sat4vk";
		 

		/*// create an instance of properties class

		Properties props = new Properties();

		// try retrieve data from file

		try {
			// load properties file
			props.load(new FileInputStream("config.properties"));

			// assign values to local variable

			dvrcls = props.getProperty("dvrcls");
			url = props.getProperty("url");
			db = props.getProperty("db");
			uname = props.getProperty("uname");
			pwd = props.getProperty("pwd");

			System.out.println(dvrcls + ":" + url + ":" + db + ":" + uname
					+ ":" + pwd);
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		try {

			// Loading postgreSQL JDBC Driver
			Class.forName(dvrcls);

			// Connect to database
			con = DriverManager.getConnection(url + ":" + db, uname, pwd);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * disconnects the db and closes the connection
	 */
	public void disconnect() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates the table
	 * @param query-pass the query string to create the table
	 */
	public void createTable(String query) {
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			stmt.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * drops the table
	 * @param query-query string to drop the table
	 */
	public void droptable(String query) {
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			stmt.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * used to update json string in the db table
	 * whenever it gets updated
	 * @param json-updated json string
	 */
	public void updateJsonDoc(String json){
		String set="doc='"+json+"'";
		String cond="id=1";
		update("config_tab1", set, cond);
	}

	/**
	 * used to get json string from the db
	 * 
	 * @returns json -json string 
	 */
	public String getJsonDoc() {
		String json = "";
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("select doc from config_tab1");
			if (rs.next()) {
				json = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * selects all records from table by passing tablle name 
	 * @param tname
	 *            -name of the table to be extracted
	 * @return -resulset object containing all records
	 */
	public ResultSet select(String tname) {
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("select * from " + tname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * selects records from table on specified condition
	 * 
	 * @param tname
	 *            -name of the table to be extracted
	 * @param cond
	 *            -condition of the record
	 * @return -resultset containing all records
	 */
	public ResultSet select(String tname, String cond) {
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("select * from " + tname + "where " + cond);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * selects records from specified columns from table
	 * 
	 * @param tname
	 * @param cols
	 * @return
	 */
	public ResultSet select(String tname, ArrayList<String> cols) {
		String colspart = "";
		for (int i = 0; i < cols.size(); i++) {
			if (i != (cols.size() - 1))
				colspart += cols.get(i) + ",";
			else
				colspart += cols.get(i);
		}

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("select " + colspart + " from " + tname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;

	}

	/**
	 * selects records from specified columns from table on condition.
	 * 
	 * @param tname
	 * @param cols
	 * @param cond
	 * @return
	 */
	public ResultSet select(String tname, ArrayList<String> cols, String cond) {
		String colspart = "";
		for (int i = 0; i < cols.size(); i++) {
			if (i != (cols.size() - 1))
				colspart += cols.get(i) + ",";
			else
				colspart += cols.get(i);
		}

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("select " + colspart + " from " + tname
					+ " where " + cond);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;

	}

	/**
	 * deletes all records from table
	 * 
	 * @param tname
	 */
	public void delete(String tname) {
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("delete from " + tname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * deletes the records on specified condition
	 * @param tname
	 * @param cond
	 */
	public void delete(String tname, String cond) {
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("delete from " + tname + " where " + cond);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * inserts the data into specified table
	 * 
	 * @param tname
	 *            name of the table in which we want to insert the data
	 * @param cols
	 *            array containing list of columns to be inserted
	 * @param vals
	 *            array containing list of values of corresponding columns
	 * @return success/failure
	 */
	public void insert(String tname, ArrayList<String> cols,
			ArrayList<String> vals) {

		// make query
		String query = "insert into " + tname + " ";
		String col = getCols(cols);
		String val = getVals(vals);
		query += col + val;
		try {
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		} catch (SQLException se) {
			se.printStackTrace();
		}

	}
	/**
	 * updates the table by setting new schema on condition
	 * @param tname -table name
	 * @param set -what you want to update
	 * @param cond -where you want to update
	 */
	public void update(String tname,String set,String cond){
		String query = "update " + tname + " SET " + set + " WHERE "+ cond ;
		//System.out.println("Query: "+query);
		try {
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}
	/**
	 * check wether the string is numeric 
	 * @param str
	 * @return -returns positive number it is numeric else returns -1.
	 */
	private int getNumber(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * gets value's part of the query for insertion
	 * 
	 * @param al -array of values
	 * @return -returns value part of the insert query string
	 */
	private String getVals(ArrayList<String> al) {
		String str = " values (";
		for (String s : al) {
			if (!s.equals((String) al.get(al.size() - 1))) {
				if (getNumber(s) != -1) {
					str += getNumber(s) + ",";
				} else {
					str += "'" + s + "',";
				}
			} else {
				if (getNumber(s) != -1) {
					str += getNumber(s);
				} else {
					str += "'" + s + "'";
				}
			}
		}
		str += ")";
		return str;
	}

	/**
	 * gets column's part of query for insertion
	 * 
	 * @param al -array of columns
	 * @return -returns column part of the insert query string
	 */
	private String getCols(ArrayList<String> al) {
		String str = "(";
		for (String s : al) {
			if (!s.equals(al.get(al.size() - 1))) {
				str += s + ",";
			} else {
				str += s;
			}
		}
		str += ")";
		return str;
	}

	/**
	 * gets the column names from the table
	 * 
	 * @param tname
	 * @return -arraylist of column names
	 */
	public ArrayList<String> getColumns(String tname) {
		ArrayList<String> cls = new ArrayList<String>();
		try {
			stmt = con.createStatement();
			ResultSetMetaData rsmd = stmt
					.executeQuery("select * from " + tname).getMetaData();
			for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
				cls.add(rsmd.getColumnName(i));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cls;
	}

}
