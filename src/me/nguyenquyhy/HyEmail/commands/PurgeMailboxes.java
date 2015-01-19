package me.nguyenquyhy.HyEmail.commands;

import java.sql.Connection;
import java.sql.ResultSet;

import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PurgeMailboxes implements CommandExecutor {

	public HyEmail plugin;

	public PurgeMailboxes(HyEmail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();
	ResultSet rs;
	java.sql.Statement stmt;
	Connection con;

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		ResultSet rs;
		java.sql.Statement stmt;
		Connection con;
		try {
			con = service.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery("DELETE FROM HyEmail WHERE expiration < '" + plugin.getCurrentDTG() + "'");
			rs.close();

			sender.sendMessage(plugin.GRAY + "[Email] " + ChatColor.GRAY
					+ "Purged " + ChatColor.GREEN + plugin.expireMail()
					+ ChatColor.GRAY + " expired messages");
		} catch (Exception e) {
			plugin.log.info("[HyEmail] " + "Error: " + e);
			if (e.toString().contains("locked")) {
				sender.sendMessage(plugin.GRAY
						+ "[HyEmail] "
						+ plugin.GOLD
						+ "The database is busy. Please wait a moment before trying again...");
			} else {
				player.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.RED
						+ "Error: " + plugin.WHITE + e);
			}
		}
		return true;

	}
}
