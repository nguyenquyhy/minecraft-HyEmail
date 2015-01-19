package me.nguyenquyhy.HyEmail.listeners;

import java.sql.Connection;
import java.sql.ResultSet;

import me.nguyenquyhy.HyEmail.DBConnection;
import me.nguyenquyhy.HyEmail.HyEmail;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PListener implements Listener {

	public HyEmail plugin;

	public PListener(HyEmail plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	DBConnection service = DBConnection.getInstance();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.getConfig().getBoolean("OnPlayerJoin.ShowNewMessages")) {
			final Player player = event.getPlayer();
			String targetnick = player.getName().toLowerCase();
			Connection con;
			java.sql.Statement stmt;
			ResultSet rs;
			try {
				con = service.getConnection();
				stmt = con.createStatement();
				rs = stmt
						.executeQuery("SELECT COUNT(target) AS inboxtotal FROM HyEmail WHERE target='"
								+ targetnick + "' AND read='NO'");
				final int unreadCount = rs.getInt("inboxtotal");
				rs.close();
				if (player.hasPermission("HyEmail.inbox") && unreadCount != 0) {
					int tempDelay = plugin.getConfig().getInt(
							"OnPlayerJoin.DelayInSeconds");
					int Delay = 20 * tempDelay;

					Bukkit.getServer()
							.getScheduler()
							.scheduleSyncDelayedTask(
									Bukkit.getServer().getPluginManager()
											.getPlugin("HyEmail"),
									new Runnable() {
										public void run() {
											player.sendMessage(plugin.GRAY
													+ "[Mail] "
													+ plugin.GREEN
													+ "You have "
													+ plugin.GOLD
													+ unreadCount
													+ plugin.GREEN
													+ " new messages. Type /inbox to check or /email for help.");
										}
									}, Delay);

				}
			} catch (Exception e) {
				player.sendMessage(plugin.GRAY + "[HyEmail] " + plugin.RED
						+ "Error: " + plugin.WHITE + e);
			}
		}
	}
}