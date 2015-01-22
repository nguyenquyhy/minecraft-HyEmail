package me.nguyenquyhy.HyEmail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import me.nguyenquyhy.HyEmail.commands.*;
import me.nguyenquyhy.HyEmail.listeners.PListener;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HyEmail extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");

	public ChatColor GREEN = ChatColor.GREEN;
	public ChatColor RED = ChatColor.RED;
	public ChatColor GOLD = ChatColor.GOLD;
	public ChatColor GRAY = ChatColor.GRAY;
	public ChatColor WHITE = ChatColor.WHITE;
	public ChatColor AQUA = ChatColor.AQUA;

	DBConnection service = DBConnection.getInstance();

	public void onEnable() {
		log.info("[" + getDescription().getName() + "] "
				+ getDescription().getVersion() + " enabled.");
		// Load Config.yml
		FileConfiguration cfg = getConfig();
		FileConfigurationOptions cfgOptions = cfg.options();
		cfgOptions.copyDefaults(true);
		cfgOptions.copyHeader(true);
		saveConfig();
		// declare new listener
		new PListener(this);
		this.getCommand("email").setExecutor(new Email(this));
		this.getCommand("inbox").setExecutor(new Inbox(this));
		this.getCommand("outbox").setExecutor(new Outbox(this));
		this.getCommand("send").setExecutor(new Send(this));
		this.getCommand("showinbox").setExecutor(new ShowInbox(this));
		this.getCommand("clearmailbox").setExecutor(new ClearMailBox(this));
		this.getCommand("mailboxes").setExecutor(new MailBoxes(this));
		this.getCommand("purgemailboxes").setExecutor(new PurgeMailboxes(this));
		// Create connection & table
		try {
			service.setPlugin(this);
			service.setConnection();
			service.createTable();
		} catch (Exception e) {
			log.info("[HyEmail] " + "Error: " + e);
		}
		// Check for and delete any expired tickets, display progress.
		log.info("[HyEmail] " + expireMail() + " Expired Messages Cleared");
	}

	public void onDisable() {
		// Check for and delete any expired tickets, display progress.
		log.info("[HyEmail] " + expireMail() + " Expired Messages Cleared");
		// Close DB connection
		service.closeConnection();
		log.info("[" + getDescription().getName() + "] "
				+ getDescription().getVersion() + " disabled.");
	}

	public String myGetPlayerName(String name) {
		Player caddPlayer = getServer().getPlayerExact(name);
		String pName;
		if (caddPlayer == null) {
			caddPlayer = getServer().getPlayer(name);
			if (caddPlayer == null) {
				pName = name;
			} else {
				pName = caddPlayer.getName();
			}
		} else {
			pName = caddPlayer.getName();
		}
		return pName;
	}

	public Date getCurrentDTG() {
		Calendar currentDate = Calendar.getInstance();
		return currentDate.getTime();
	}

	public Date getExpiration() {
		String mailExpiration = getConfig().getString("MailExpiration");
		for (char c : mailExpiration.toCharArray()) {
			if (!Character.isDigit(c)) {
				mailExpiration = "14";
			}
		}
		int expire = Integer.parseInt(mailExpiration);
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		cal.add(Calendar.DATE, expire);
		Date expirationDate = cal.getTime();
		return expirationDate;
	}
	
	public String formatDate(Date date) {
		SimpleDateFormat dtgFormat = new SimpleDateFormat("dd/MMM/yy HH:mm");
		return dtgFormat.format(date);
	}

	public int expireMail() {
		ResultSet rs;
		java.sql.Statement stmt;
		Connection con;
		int expirations = 0;
		try {
			con = service.getConnection();
			stmt = con.createStatement();
			Statement stmt2 = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM HyEmail");
			while (rs.next()) {
				Timestamp expiration = rs.getTimestamp("expiration");
				String id = rs.getString("id");

				if (expiration != null) {
					// IF AN EXPIRATION HAS BEEN APPLIED
					if (expiration.compareTo(Calendar.getInstance().getTime()) < 0) {
						stmt2.executeUpdate("DELETE FROM HyEmail WHERE id='"
								+ id + "'");
						expirations++;
					}
				}
			}
			return expirations;
		} catch (Exception e) {
			log.info("[HyEmail] " + "Error: " + e);
		}
		return expirations;
	}

	public void displayHelp(CommandSender sender, Player player) {
		if (sender != null) {
			sender.sendMessage(GOLD + "[ HyEmail "
					+ getDescription().getVersion() + " ]");
			sender.sendMessage(GREEN + " /send <player> <msg>" + WHITE
					+ " - Send a message");
			sender.sendMessage(GREEN + " /inbox" + WHITE
					+ " - Check your inbox");
			sender.sendMessage(GREEN + " /inbox <id>" + WHITE + " or " + GREEN
					+ " /inbox read <id>" + WHITE + " - Read a message");
			sender.sendMessage(GREEN + " /inbox delete <id>" + WHITE
					+ " - Delete a message");
			sender.sendMessage(GREEN + " /outbox" + WHITE
					+ " - Display your outbox");

			if (player == null || sender.hasPermission("HyEmail.admin")) {
				sender.sendMessage(GOLD + "[Admin Commands]");
				sender.sendMessage(AQUA + " /mailboxes" + WHITE
						+ " - List active mailboxes");
				sender.sendMessage(AQUA + " /clearmailbox <playername>" + WHITE
						+ " - Clear an active mailbox");
				sender.sendMessage(AQUA + " /purgemail" + WHITE
						+ " - Purge expired messages from DB");
				sender.sendMessage(AQUA + " /showinbox <playername>" + WHITE
						+ " - Show the inbox for a particular player (designed for command block)");
			}
		}
	}

	public String createSpacePadding(String data, int requiredLength) {
		StringBuilder sb = new StringBuilder();
		int spaceLength = requiredLength - data.length();
		for (int i = 0; i < spaceLength; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
}