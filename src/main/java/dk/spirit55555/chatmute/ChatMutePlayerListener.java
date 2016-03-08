package dk.spirit55555.chatmute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ChatMutePlayerListener implements Listener {
	private ChatMute plugin;
	private String filterMessage;
	private int filterMuteTime;
	private String filterCommandsMute;
	private String filterCommandsUnmute;
	private String filterAdminMessage;
	private boolean showAdminMessage;

	//Get the plugin from the main class, needed for the configuration file
	public ChatMutePlayerListener(ChatMute instance) {
		plugin = instance;
	}

	//NOTE: EssentialsChat will remove dots in the chat, if priority is not set to EventPriority.LOWEST
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		//Get the player and the player name
		Player thePlayer  = event.getPlayer();
		String playerName = thePlayer.getName();

		boolean enabled = plugin.getConfig().getBoolean("enabled");

		//Is the plugin enabled?
		if (enabled) {
			//Did the player chat something bad?
			if (this.checkFilters(event)) {
				//Cancel the message
				event.setCancelled(true);
				
				//Mute the player
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), filterCommandsMute);
				
				//Send message to the player
				plugin.showMessage(thePlayer, filterMessage);
				
				//Do we want to show the admin message?
				if (showAdminMessage) {
					//Format the admin message
					String adminMessage = String.format(filterAdminMessage, playerName, filterMuteTime, event.getMessage(), '/' + filterCommandsUnmute);
	
					//Send a message to the console
					plugin.showMessage(Bukkit.getConsoleSender(), adminMessage);
	
					//Send an admin message to all with the chatmute.notify permission
					for (Player player: Bukkit.getServer().getOnlinePlayers()) {
						if (player.hasPermission("chatmute.notify")) {
							plugin.showMessage(player, adminMessage);
						}
					}
				}
			}
		}
	}

	/**
	 * Check if a chat message matches one of the filters.
	 * @param event The chat event.
	 * @return True if there is a match, false if no filters match.
	 */
	private boolean checkFilters(AsyncPlayerChatEvent event) {
		//Get the filters
		ConfigurationSection filters = plugin.filters.getConfig();

		//Run through the filters
		for (String filter: filters.getKeys(false)) {
			 //Is the player allowed to bypass it? Then continue with the next filter.
			 if (event.getPlayer().hasPermission("chatmute.bypass." + filter)) {
				 continue;
			 }
			
			ConfigurationSection config = filters.getConfigurationSection(filter);

			//Only check if the filter is enabled
			if (!config.getBoolean("enabled", true)) {
				return false;
			}

			//Get the filter regex
			Pattern p = Pattern.compile(config.getString("regex"));
			Matcher m = p.matcher(event.getMessage());

			//Did we match something?
			if (m.find()) {
				filterMessage  = config.getString("message");
				filterMuteTime = config.getInt("mute-time", plugin.getConfig().getInt("mute-time"));
				
				//Only show the admin message when both commands have been defined
				if (config.getString("commands.mute") != null && config.getString("commands.unmute") == null)
					showAdminMessage = false;
				
				else {
					showAdminMessage = true;
					filterCommandsUnmute = config.getString("commands.unmute", plugin.getConfig().getString("commands.unmute"));
					filterCommandsUnmute = formatCommand(filterCommandsUnmute, event.getPlayer().getName());
					filterAdminMessage   = config.getString("notify", plugin.getMessage("notify"));
				}

				filterCommandsMute   = config.getString("commands.mute", plugin.getConfig().getString("commands.mute"));
				filterCommandsMute   = formatCommand(filterCommandsMute, event.getPlayer().getName());
				
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Format a command with variables, so it's ready to run.
	 * @param command The command that has to be formated
	 * @param username The username of the player the command should run on
	 * @return A ready command
	 */
	private String formatCommand(String command, String username) {
		command = command.replace("{PLAYER}", username);
		command = command.replace("{TIME}", Integer.toString(filterMuteTime));
		command = command.replace("{MESSAGE}", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', filterMessage)));
		
		return command;
	}
}
