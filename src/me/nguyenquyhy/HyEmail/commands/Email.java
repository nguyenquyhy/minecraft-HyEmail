package me.nguyenquyhy.HyEmail.commands;

import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Email implements CommandExecutor {

	public HyEmail plugin;

	public Email(HyEmail plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length == 0) {
			plugin.displayHelp(sender, player);
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (player == null || player.hasPermission("HyEmail.admin")) {
				plugin.reloadConfig();
				sender.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.GREEN
						+ "Config Reloaded!");
				return true;
			} else {
				sender.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.RED
						+ "You do not have permission to do that!");
			}
		}

		return true;
	}

}