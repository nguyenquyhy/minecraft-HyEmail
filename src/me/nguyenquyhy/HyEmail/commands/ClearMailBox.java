package me.nguyenquyhy.HyEmail.commands;

import java.sql.Connection;
import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearMailBox implements CommandExecutor {
	public HyEmail plugin;

	public ClearMailBox(HyEmail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {

		if (args.length != 1) {
			sender.sendMessage("/clearmailbox <player>");
			return true;
		}
		String playerName = args[0].toLowerCase();

		java.sql.Statement stmt;
		Connection con;
		try {
			con = service.getConnection();
			stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM HyEmail WHERE target='"
					+ playerName + "'");
			sender.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.GREEN
					+ "Mailbox of " + playerName + " Cleared.");
			return true;
		} catch (Exception e) {
			plugin.log.info("[HyEmail] Error: " + e);
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