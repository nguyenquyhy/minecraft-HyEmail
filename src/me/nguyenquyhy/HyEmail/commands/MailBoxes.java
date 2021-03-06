package me.nguyenquyhy.HyEmail.commands;

import java.sql.Connection;
import java.sql.ResultSet;

import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MailBoxes implements CommandExecutor {

	public HyEmail plugin;

	public MailBoxes(HyEmail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		ResultSet rs;
		java.sql.Statement stmt;
		Connection con;
		try {
			con = service.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT DISTINCT target FROM HyEmail");
			sender.sendMessage(plugin.GOLD + "Active Inboxes: ");
			while (rs.next()) {
				sender.sendMessage(plugin.GRAY + " Mailbox: " + plugin.GREEN
						+ rs.getString("target"));
			}
			rs.close();
		} catch (Exception e) {
			plugin.log.info("[HyEmail] " + "Error: " + e);
			if (e.toString().contains("locked")) {
				sender.sendMessage(plugin.GRAY
						+ "[HyEmail] "
						+ plugin.GOLD
						+ "The database is busy. Please wait a moment before trying again...");
			} else {
				sender.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.RED
						+ "Error: " + plugin.WHITE + e);
			}
		}

		return true;
	}

}