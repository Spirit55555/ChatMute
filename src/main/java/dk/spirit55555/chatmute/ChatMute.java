package dk.spirit55555.chatmute;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatMute extends JavaPlugin {
	protected final ConfigAccessor filters                = new ConfigAccessor(this, "filters.yml");
	protected final ConfigAccessor messages               = new ConfigAccessor(this, "messages.yml");
	private final ChatMutePlayerListener playerListener   = new ChatMutePlayerListener(this);
	private final ChatMuteCommandExecutor commandExecutor = new ChatMuteCommandExecutor(this);
	private final ChatMuteTabCompleter tabCompleter       = new ChatMuteTabCompleter(this);

	public void onEnable() {
		//Save configuration files, if they do not exist
	    saveDefaultConfig();
	    messages.saveDefaultConfig();
	    filters.saveDefaultConfig();

	    //Get the plugin manager
		PluginManager pm = getServer().getPluginManager();

		//And register our listener
		pm.registerEvents(playerListener, this);

		//Register our command executor and tab completer
		getCommand("chatmute").setExecutor(commandExecutor);
		getCommand("chatmute").setTabCompleter(tabCompleter);
	}

	/**
	 * Show a message to a user with the configured prefix.
	 * @param sender Who to send the message to.
	 * @param message The message to send.
	 */
	protected void showMessage(CommandSender sender, String message) {
		//Get the prefix
		String prefix = getConfig().getString("prefix");

		//Translate the colors
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		message = ChatColor.translateAlternateColorCodes('&', message);

		//Send the message
		sender.sendMessage(prefix + message);
	}

	/**
	 * Get a language message from the messages.yml file
	 * @param message
	 * @return
	 */
	protected String getMessage(String message) {
		return messages.getConfig().getString(message);
	}
}
