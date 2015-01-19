package me.nguyenquyhy.HyEmail.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Send implements CommandExecutor {
	public HyEmail plugin;

	public Send(HyEmail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length < 2) {
			sender.sendMessage(plugin.GOLD
					+ "/send <ExactPlayerName> <Message>");
			return true;
		}
		Connection con;
		java.sql.Statement stmt;
		try {
			con = service.getConnection();
			stmt = con.createStatement();

			StringBuilder sb = new StringBuilder();
			for (String arg : args)
				sb.append(arg + " ");
			String[] temp = sb.toString().split(" ");
			String[] temp2 = Arrays.copyOfRange(temp, 1, temp.length);
			sb.delete(0, sb.length());
			for (String details : temp2) {
				sb.append(details);
				sb.append(" ");
			}
			String details = sb.toString();

			String rightNow = plugin.getCurrentDTG();
			String target = plugin.myGetPlayerName(args[0]).toLowerCase();

			ResultSet rs2 = stmt
					.executeQuery("SELECT COUNT(target) AS inboxtotal FROM HyEmail WHERE target='"
							+ target + "'");
			int targetInboxTotal = rs2.getInt("inboxtotal");
			rs2.close();

			int maxMailboxSize = plugin.getConfig().getInt("MaxMailboxSize");
			if (targetInboxTotal >= maxMailboxSize) {
				sender.sendMessage(plugin.GRAY + "[Email] " + plugin.RED
						+ target + "'s Inbox is full");
				return true;
			}

			PreparedStatement statement = con
					.prepareStatement("INSERT INTO HyEmail VALUES (?,?,?,?,?,?,?);");
			if (player == null) {
				// Send from console
				statement.setString(2, "console");

				statement.setString(3, target);

				statement.setString(4, rightNow);
				statement.setString(5, details);

				statement.setString(6, "NO");
				statement.setString(7, "NONE");
				statement.executeUpdate();
				statement.close();

				sender.sendMessage(plugin.GRAY + "[Email] " + ChatColor.GREEN
						+ "Message Sent to: " + ChatColor.WHITE + target);
				if (Bukkit.getPlayer(args[0]) != null
						&& Bukkit.getPlayer(args[0]).hasPermission(
								"HyEmail.inbox")) {
					Bukkit.getPlayer(args[0]).sendMessage(
							plugin.GRAY + "[Email] " + plugin.GREEN
									+ "You've Got Email from Server! "
									+ plugin.GOLD + "[/email]");
				}
				return true;

			} else {
				statement.setString(2, player.getName());

				statement.setString(3, target);

				statement.setString(4, rightNow);
				statement.setString(5, details);

				statement.setString(6, "NO");
				statement.setString(7, "NONE");
				statement.executeUpdate();
				statement.close();

				sender.sendMessage(plugin.GRAY + "[Email] " + ChatColor.GREEN
						+ "Message Sent to: " + ChatColor.WHITE + target);
				if (Bukkit.getPlayer(args[0]) != null
						&& Bukkit.getPlayer(args[0]).hasPermission(
								"HyEmail.inbox")) {
					Bukkit.getPlayer(args[0]).sendMessage(
							plugin.GRAY + "[Email] " + plugin.GREEN
									+ "You've Got Email from "
									+ player.getName() + "! " + plugin.GOLD
									+ "[/email]");
				}
				return true;
			}
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