package dk.spirit55555.chatmute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class ChatMuteTabCompleter implements TabCompleter {
	private ChatMute plugin;

	//The command options
	private String[] options = {
		"version",
		"reload",
		"permissions",
		"status",
		"enable",
		"disable",
		"mute-time",
		"filters"
	};

	//The filter options
	private String[] filterOptions = {
		"enable",
		"disable",
		"mute-time"
	};

	public ChatMuteTabCompleter(ChatMute instance) {
		plugin = instance;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		//Complete the commands
		if (args.length == 1) {
			return getSuggestions(args[0], options);
		}

		//Complete the current mute-time
		else if (args.length == 2 && (args[0].equalsIgnoreCase("mute-time") || args[0].equalsIgnoreCase("m")) && args[1].isEmpty()) {
			List<String> suggestion = new ArrayList<String>();
			Integer muteTime = plugin.getConfig().getInt("mute-time");
			suggestion.add(muteTime.toString());
			return suggestion;
		}

		//Complete the filter commands
		else if (args.length > 1 && args.length < 5 && (args[0].equalsIgnoreCase("filters") || args[0].equalsIgnoreCase("f"))) {
			if (args.length == 2) {
				Set<String> filters = plugin.filters.getConfig().getKeys(false);
				return getSuggestions(args[1], filters.toArray(new String[filters.size()]));
			}

			else if (args.length == 3)
				return getSuggestions(args[2], filterOptions);

			else if (args.length == 4 && (args[2].equalsIgnoreCase("mute-time") || args[2].equalsIgnoreCase("m")) && args[3].isEmpty()) {
				List<String> suggestion = new ArrayList<String>();
				Integer muteTime = plugin.filters.getConfig().getInt(args[1] + ".mute-time");

				//Only show the suggestion if the mute time has been set.
				if (muteTime != 0) {
					suggestion.add(muteTime.toString());
					return suggestion;
				}
			}
		}

		return null;
	}

	/**
	 * Helper function for getting the right suggestions.
	 * @param option String of what the user has already written.
	 * @param suggestions Array of suggestions that should be checked.
	 * @return The suggestions that match.
	 */
	private List<String> getSuggestions(String option, String[] suggestions) {
		List<String> newSuggestions = new ArrayList<String>();

		for (String suggestion : suggestions) {
			//Only show suggestions that could match what the user has already written.
			if (suggestion.matches("^" + option + ".+")) {
				newSuggestions.add(suggestion);
			}
		}

		return newSuggestions;
	}
}