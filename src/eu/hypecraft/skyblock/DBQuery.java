package eu.hypecraft.skyblock;

import java.sql.*;

public class DBQuery extends Main {
	
	public static Connection ConnectDB() throws SQLException {
	        String url = "jdbc:mysql://127.0.0.1:3306/SkyBlock?useSSL=false";
	        String username = "root";
	        String password = "CYyK5Y8TfbKpkhYRj9ryqdA3";
	        Connection conn = DriverManager.getConnection(url, username, password);
	        return conn;
    }
	
	
	public static ResultSet getResults(Connection conn, String query) throws SQLException {
		
		ResultSet results = conn.prepareStatement(query).executeQuery();
		return results;
	}
	
	
	public static boolean makeChanges(Connection conn, String query) throws SQLException {
		boolean results = conn.prepareStatement(query).execute();
		return results;
	}
}
