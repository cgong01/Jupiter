package db;

import db.mongodb.MongoDBConnection;
import db.mysql.MySQLConnection;

public class DBConnectionFactory {
	
	public enum DBType {
		MYSQL("mysql"),
		MONGODB("mongodb"),
		DEFAULTDB("mysql");
		
		private String dbname;
		
		DBType(String dbname) {
			this.dbname = dbname;
		}
		
		public String getDBName() {
			return this.dbname;
		}
	}
	
//	private static final String DEFAULT_DB = "mysql";
	private static DBType dbType = DBType.DEFAULTDB;
	
//	public DBType getDBType() {
//		return this.dbType;
//	}
	
//	public static DBConnection getDBConnection(String db) {
//		switch(db) {
//			case "mysql": 
//				return new MySQLConnection();
//			case "mongodb": 
//				return null;	// return new MongoDBConnection();
//			default: 
//				throw new IllegalArgumentException("Invalid db" + db);
//		}
//	}
	
	public static DBConnection getDBConnection(DBType db) {
		if (db.getDBName().equals("mysql")) {
			return new MySQLConnection();
		}
		else if (db.getDBName().equals("mongodb")) {
			return new MongoDBConnection();
		}
		return null;
	}
	
//	public static DBConnection getDBConnection() {
//		return getDBConnection(DEFAULT_DB);
//	}
	
	public static DBConnection getDBConnection() {
		return getDBConnection(dbType);
	}
}
