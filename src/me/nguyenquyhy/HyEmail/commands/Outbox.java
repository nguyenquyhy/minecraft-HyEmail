package me.nguyenquyhy.HyEmail.commands;

import java.sql.Connection;
import java.sql.ResultSet;

import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Outbox implements CommandExecutor {

	public HyEmail plugin;

	public Outbox(HyEmail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

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
			String ownernick = player.getName().toLowerCase();

			rs = stmt.executeQuery("SELECT * FROM HyEmail WHERE sender='"
					+ ownernick + "'");

			sender.sendMessage(plugin.GOLD
					+ "- ID ----- TO ----------- DATE ------");
			while (rs.next()) {
				Boolean isread = rs.getTimestamp("read") != null;
				if (!isread) {
					sender.sendMessage(plugin.GRAY + "  [" + plugin.GREEN
							+ rs.getInt("id") + plugin.GRAY + "]" + "         "
							+ rs.getString("target") + "          "
							+ rs.getString("date"));
				} else {
					sender.sendMessage(plugin.GRAY + "  [" + rs.getInt("id")
							+ plugin.GRAY + "]" + "         "
							+ rs.getString("target") + "          "
							+ rs.getString("date"));
				}
			}
			sender.sendMessage(plugin.GRAY
					+ "(deleted/expired messages will not be displayed)");
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