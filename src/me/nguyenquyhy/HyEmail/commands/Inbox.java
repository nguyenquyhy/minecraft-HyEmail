package me.nguyenquyhy.HyEmail.commands;

import java.sql.Connection;
import java.sql.ResultSet;

import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Inbox implements CommandExecutor {
	public HyEmail plugin;

	public Inbox(HyEmail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length != 0 && args.length != 1 && args.length != 2) {
			printHelp(sender);
		} else if (args.length == 0) {
			// Read all email
			readAllEmails(sender, player);
		} else if (args.length == 1) {
			readSingleEmail(sender, player, args[0]);
		} else if (args.length == 2) {
			String subcommand = args[0];

			if (subcommand.equalsIgnoreCase("read")) {
				readSingleEmail(sender, player, args[1]);
			} else if (subcommand.equalsIgnoreCase("delete")) {
				deleteSingleEmail(sender, player, args[1]);
			} else {
				sender.sendMessage(plugin.RED + "Invalid command!");
				printHelp(sender);
			}
		}
		return true;
	}

	private void readSingleEmail(CommandSender sender, Player player,
			String emailId) {
		ResultSet rs;
		java.sql.Statement stmt;
		Connection con;
		try {
			con = service.getConnection();
			stmt = con.createStatement();
			String playerName = player.getName().toLowerCase();

			rs = stmt.executeQuery("SELECT * FROM HyEmail WHERE id='" + emailId
					+ "' AND target='" + playerName + "'");
			String date = rs.getString("date");
			String id = rs.getString("id");
			String expiration = rs.getString("expiration");
			Boolean updateExpiration = false;
			if (expiration.equalsIgnoreCase("NONE")) {
				updateExpiration = true;
				expiration = plugin.getExpiration(date);
			}
			sender.sendMessage(plugin.GOLD + "Message Open: " + plugin.WHITE
					+ id);
			while (rs.next()) {
				sender.sendMessage(plugin.GRAY + " From: " + plugin.GREEN
						+ rs.getString("sender"));
				sender.sendMessage(plugin.GRAY + " Date: " + plugin.WHITE
						+ date);
				sender.sendMessage(plugin.GRAY + " Expires: " + plugin.WHITE
						+ expiration);
				sender.sendMessage(plugin.GRAY + " Message: " + plugin.GREEN
						+ rs.getString("message"));

			}
			if (updateExpiration) {
				stmt.executeUpdate("UPDATE HyEmail SET read='YES', expiration='"
						+ expiration
						+ "' WHERE id='"
						+ emailId
						+ "' AND target='" + playerName + "'");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			if (e.toString().contains("ResultSet closed")) {
				sender.sendMessage(plugin.GRAY
						+ "[HyEmail] "
						+ plugin.RED
						+ "This is not your message to read or it does not exist.");
			} else if (e.toString().contains(
					"java.lang.ArrayIndexOutOfBoundsException")) {
				sender.sendMessage("Please specify a valid email ID");
				printHelp(sender);
			} else {
				player.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.RED
						+ "Error: " + plugin.WHITE + e);
			}
		}
	}

	private void deleteSingleEmail(CommandSender sender, Player player,
			String emailId) {
		ResultSet rs;
		java.sql.Statement stmt;
		Connection con;
		try {
			String Playername = player.getName().toLowerCase();
			con = service.getConnection();
			stmt = con.createStatement();

			rs = stmt.executeQuery("SELECT * FROM HyEmail WHERE id='" + emailId
					+ "'");

			if (!rs.getString("target").equalsIgnoreCase(Playername)) {
				sender.sendMessage(plugin.GRAY
						+ "[Email] "
						+ plugin.RED
						+ "This is not your message to delete or it does not exist. ");
			} else {
				stmt.executeUpdate("DELETE FROM HyEmail WHERE id='" + emailId
						+ "' AND target='" + Playername + "'");
				sender.sendMessage(plugin.GRAY + "[Email] " + plugin.GREEN
						+ "Message Deleted.");
			}
			rs.close();
		} catch (Exception e) {
			if (e.toString().contains("ResultSet closed")) {
				sender.sendMessage(plugin.GRAY
						+ "[Email] "
						+ plugin.RED
						+ "This is not your message to delete or it does not exist.");
			} else if (e.toString().contains(
					"java.lang.ArrayIndexOutOfBoundsException")) {
				sender.sendMessage("Please specify a valid email ID");
				printHelp(sender);
			} else {
				plugin.log.info("[HyEmail] " + "Error: " + e);
				player.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.RED
						+ "Error: " + plugin.WHITE + e);
			}
		}
	}

	public void readAllEmails(CommandSender sender, Player player) {
		ResultSet rs;
		java.sql.Statement stmt;
		Connection con;
		try {
			con = service.getConnection();
			stmt = con.createStatement();
			String playerName = player.getName().toLowerCase();

			rs = stmt
					.executeQuery("SELECT COUNT(target) AS inboxtotal FROM HyEmail WHERE target='"
							+ playerName + "'");
			final int emailCount = rs.getInt("inboxtotal");
			rs.close();

			if (emailCount == 0) {
				sender.sendMessage(plugin.GREEN
						+ "You have no email in your inbox.");
			} else {
				rs = stmt.executeQuery("SELECT * FROM HyEmail WHERE target='"
						+ playerName + "'");
				sender.sendMessage(plugin.GOLD
						+ "- ID ----- FROM ----------- DATE ------");
				while (rs.next()) {
					String isread = rs.getString("read");
					if (isread.contains("NO")) {
						// Unread messages
						sender.sendMessage(plugin.GRAY + "  [" + plugin.GREEN
								+ rs.getInt("id") + plugin.GRAY + "]"
								+ "         " + rs.getString("sender")
								+ "        " + rs.getString("date"));
					} else {
						// Read messages
						sender.sendMessage(plugin.GRAY + "  ["
								+ rs.getInt("id") + plugin.GRAY + "]"
								+ "         " + rs.getString("sender")
								+ "        " + rs.getString("date"));
					}
				}
				rs.close();

				sender.sendMessage(plugin.GRAY
						+ "(type /inbox <id> to read each email)");
			}
		} catch (Exception e) {
			plugin.log.info("[HyEmail] Error: " + e);
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
	}

	private void printHelp(CommandSender sender) {
		sender.sendMessage(plugin.GREEN + "/inbox" + plugin.WHITE
				+ "- check your inbox");
		sender.sendMessage(plugin.GREEN + "/inbox <ID>" + plugin.WHITE + " or "
				+ plugin.GREEN + "/inbox read <ID>" + plugin.WHITE
				+ "- read one email");
		sender.sendMessage(plugin.GREEN + "/inbox delete <ID>" + plugin.WHITE
				+ "- delete one email");
	}
}