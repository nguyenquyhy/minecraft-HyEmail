package me.nguyenquyhy.HyEmail.commands;

import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ShowInbox implements CommandExecutor {
	public HyEmail plugin;
	public Inbox inbox;
	
	public ShowInbox(HyEmail plugin) {
		this.plugin = plugin;
		this.inbox = new Inbox(plugin);
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		String playerName = args[0]; 
		Player player = Bukkit.getPlayer(playerName);
		
		inbox.readAllEmails(player, player);
		return true;
	}
}
