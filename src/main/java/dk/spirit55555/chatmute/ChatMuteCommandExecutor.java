package dk.spirit55555.chatmute;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class ChatMuteCommandExecutor implements CommandExecutor {
	private ChatMute plugin;

	//Get the plugin from the main class, needed for the configuration file
	public ChatMuteCommandExecutor(ChatMute instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length == 1) {
			//Want to show the plugin version?
			if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
				String versionMessage = plugin.getMessage("version");
				versionMessage = String.format(versionMessage, plugin.getDescription().getVersion());

				plugin.showMessage(sender, versionMessage);
				return true;
			}

			//Want to reload the configuration file?
			else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
				plugin.reloadConfig();
				plugin.messages.reloadConfig();
				plugin.filters.reloadConfig();

				String reloadMessage = plugin.getMessage("reload");

				plugin.showMessage(sender, reloadMessage);

				//If the reload was done by a player, show it in the console too
				if (sender instanceof Player)
					plugin.showMessage(Bukkit.getConsoleSender(), reloadMessage);

				return true;
			}

			//Want to show the permissions?
			else if (args[0].equalsIgnoreCase("permissions") || args[0].equalsIgnoreCase("p")) {
				List<Permission> permissions = plugin.getDescription().getPermissions();
				String permissionsList = "";

				for (Permission permission : permissions)
					permissionsList += "\n&c" + permission.getName() + "\n&a- " + permission.getDescription();

				plugin.showMessage(sender, plugin.getMessage("permissions") + permissionsList);

				return true;
			}

			//Want to show the status?
			else if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("s")) {
				boolean enabled = plugin.getConfig().getBoolean("enabled", true);

				if (enabled) {
					String enabledMessage = plugin.getMessage("status.enabled");
					plugin.showMessage(sender, enabledMessage);
				}

				else {
					String disabledMessage = plugin.getMessage("status.disabled");
					plugin.showMessage(sender, disabledMessage);
				}

				return true;
			}

			//Want to enable the plugin?
			else if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("e")) {
				boolean enabled = plugin.getConfig().getBoolean("enabled", true);

				if (!enabled) {
					String enableMessage = plugin.getMessage("enable");

					plugin.getConfig().set("enabled", true);
					plugin.saveConfig();

					plugin.showMessage(sender, enableMessage);
				}

				else {
					String enabledMessage = plugin.getMessage("already.enabled");

					plugin.showMessage(sender, enabledMessage);
				}

				return true;
			}

			//Or disable the plugin?
			else if (args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("d")) {
				boolean enabled = plugin.getConfig().getBoolean("enabled", true);

				if (enabled) {
					String disableMessage = plugin.getMessage("disable");

					plugin.getConfig().set("enabled", false);
					plugin.saveConfig();

					plugin.showMessage(sender, disableMessage);
				}

				else {
					String disabledMessage = plugin.getMessage("already.disabled");

					plugin.showMessage(sender, disabledMessage);
				}

				return true;
			}

			//Want to see all filters?
			else if (args[0].equalsIgnoreCase("filters") || args[0].equalsIgnoreCase("f")) {
				FileConfiguration filtersConfig = plugin.filters.getConfig();
				List<String> filters = new ArrayList<String>();

				for (String filter : filtersConfig.getKeys(false)) {
					ChatColor color;

					if (filtersConfig.getBoolean(filter + ".enabled", true))
						color = ChatColor.GREEN;
					else
						color = ChatColor.RED;

					filters.add(color +  filter);
				}

				String message = String.format(plugin.getMessage("filters.current"), filters.size(), StringUtils.join(filters, ChatColor.RESET + ", "));
				plugin.showMessage(sender, message);
				return true;
			}
		}

		//Want to see the mute time?
		else if (args.length > 0 && args.length < 3 && (args[0].equalsIgnoreCase("mute-time") || args[0].equalsIgnoreCase("m"))) {
			//Maybe even change it?
			if (args.length == 2) {
				//Only save integers
				try {
					Integer muteTime = Integer.parseInt(args[1]);

					plugin.getConfig().set("mute-time", muteTime);
					plugin.saveConfig();
				}

				//Just return false if the value was wrong
				catch (NumberFormatException e) {
					return false;
				}

				String message = String.format(plugin.getMessage("mute-time.set"), plugin.getConfig().getInt("mute-time"));
				plugin.showMessage(sender, message);
				return true;
			}

			//Just see it then...
			String message = String.format(plugin.getMessage("mute-time.status"), plugin.getConfig().getInt("mute-time"));
			plugin.showMessage(sender, message);
			return true;
		}

		//Want to see or change the filters?
		else if (args.length > 1 && args.length < 5 && (args[0].equalsIgnoreCase("filters") || args[0].equalsIgnoreCase("f"))) {
			FileConfiguration filtersConfig = plugin.filters.getConfig();

			//Make sure the filter exists.
			if (!filtersConfig.contains(args[1])) {
				String message = String.format(plugin.getMessage("filters.notexist"), args[1]);
				plugin.showMessage(sender, message);
				return true;
			}

			//Want the filter status?
			else if (args.length == 2) {
				if (filtersConfig.getBoolean(args[1] + ".enabled", true)) {
					String message = String.format(plugin.getMessage("filters.status.enabled"), args[1]);
					plugin.showMessage(sender, message);
				}

				else {
					String message = String.format(plugin.getMessage("filters.status.disabled"), args[1]);
					plugin.showMessage(sender, message);
				}

				return true;
			}

			else if (args.length == 3) {
				//Want to enabled it?
				if (args[2].equalsIgnoreCase("enable") || args[2].equalsIgnoreCase("e")) {
					//Is it already enabled? It is by default.
					if (filtersConfig.getBoolean(args[1] + ".enabled", true)) {
						String message = String.format(plugin.getMessage("filters.already.enabled"), args[1]);
						plugin.showMessage(sender, message);
					}

					else {
						filtersConfig.set(args[1] + ".enabled", true);
						plugin.filters.saveConfig();

						String message = String.format(plugin.getMessage("filters.enable"), args[1]);
						plugin.showMessage(sender, message);
					}

					return true;
				}

				//Want to disabled it?
				else if (args[2].equalsIgnoreCase("disable") || args[2].equalsIgnoreCase("d")) {
					//Is it already disabled?
					if (!filtersConfig.getBoolean(args[1] + ".enabled", true)) {
						String message = String.format(plugin.getMessage("filters.already.disabled"), args[1]);
						plugin.showMessage(sender, message);
					}

					else {
						filtersConfig.set(args[1] + ".enabled", false);
						plugin.filters.saveConfig();

						String message = String.format(plugin.getMessage("filters.disable"), args[1]);
						plugin.showMessage(sender, message);
					}

					return true;
				}

				//Want to see the mute time?
				else if (args[2].equalsIgnoreCase("mute-time") || args[2].equalsIgnoreCase("m")) {
					Integer muteTime = plugin.filters.getConfig().getInt(args[1] + ".mute-time");

					//Has the mute time been set?
					if (muteTime == 0) {
						String message = String.format(plugin.getMessage("filters.mute-time.notset"), args[1], plugin.getConfig().getInt("mute-time"));
						plugin.showMessage(sender, message);
					}

					else {
						String message = String.format(plugin.getMessage("filters.mute-time.status"), args[1], muteTime);
						plugin.showMessage(sender, message);
					}

					return true;
				}
			}

			//Want to change the mute time on a filter?
			else if (args.length == 4 && (args[2].equalsIgnoreCase("mute-time") || args[2].equalsIgnoreCase("m"))) {
				//Only save integers
				try {
					Integer muteTime = Integer.parseInt(args[3]);

					plugin.filters.getConfig().set(args[1] + ".mute-time", muteTime);
					plugin.filters.saveConfig();
				}

				//Just return false if the value was wrong
				catch (NumberFormatException e) {
					return false;
				}

				String message = String.format(plugin.getMessage("filters.mute-time.set"), args[1], plugin.filters.getConfig().getInt(args[1] + ".mute-time"));
				plugin.showMessage(sender, message);
				return true;
			}

		}

		return false;
	}
}