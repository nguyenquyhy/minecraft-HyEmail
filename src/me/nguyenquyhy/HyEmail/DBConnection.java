package me.nguyenquyhy.HyEmail;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import me.nguyenquyhy.HyEmail.HyEmail;

public class DBConnection {
	private static DBConnection instance = new DBConnection();
	public Connection con = null;
	public int Timeout = 30;
	public Statement stmt;

	public HyEmail plugin;

	private DBConnection() {
	}

	public static synchronized DBConnection getInstance() {
		return instance;
	}

	/**
	 * We set the plugin that is to be used for these connections.
	 * 
	 * @param plugin
	 */
	public void setPlugin(HyEmail plugin) {
		this.plugin = plugin;
	}

	public void setConnection() throws Exception {
		Class.forName("org.sqlite.JDBC");
		con = DriverManager.getConnection("jdbc:sqlite:"
				+ plugin.getDataFolder().getAbsolutePath() + File.separator
				+ "HyEmail.db");
	}

	public Connection getConnection() {
		return con;
	}

	public void closeConnection() {
		try {
			con.close();
		} catch (Exception ignore) {
		}
	}

	public void createTable() {
		Statement stmt;
		try {
			stmt = con.createStatement();
			String queryC = "CREATE TABLE IF NOT EXISTS HyEmail (id INTEGER PRIMARY KEY, sender varchar(16) collate nocase, target varchar(16) collate nocase, date timestamp, message varchar(30), read varchar(10), expiration timestamp)";
			stmt.executeUpdate(queryC);
		} catch (Exception e) {
			plugin.log.info("[HyEmail] " + "Error: " + e);
		}
	}

	public void setStatement() throws Exception {
		if (con == null) {
			setConnection();
		}
		Statement stmt = con.createStatement();
		stmt.setQueryTimeout(Timeout); // set timeout to 30 sec.
	}

	public Statement getStatement() {
		return stmt;
	}

	public void executeStmt(String instruction) throws SQLException {
		stmt.executeUpdate(instruction);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Clone is not allowed.");
	}
}