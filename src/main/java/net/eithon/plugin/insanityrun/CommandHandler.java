package net.eithon.plugin.insanityrun;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.insanityrun.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String ADD_COMMAND = "/eir add <name> [<up speed> <forward speed>]";
	private static final String GOTO_COMMAND = "/eir goto <name>";
	private static final String PRICE_COMMAND = "/eir price <name> <amount>";
	private static final String REWARD_COMMAND = "/eir reward <name> <amount>";
	private static final String JOIN_COMMAND = "/eir join_named_arena <name>";
	private static final String LEAVE_COMMAND = "/eir leave";
	private static final String RESET_COMMAND = "/eir reset";
	private static final String LIST_COMMAND = "/eir list";
	private static final String REMOVE_COMMAND = "/eir remove <name>";
	private static final String LINK_COMMAND = "/eir link <name 1> <name 2>";

	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	void disable() {
		this._controller.save();
	}

	@Override
	public boolean onCommand(CommandParser commandParser) {
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return true;

		String command = commandParser.getArgumentCommand();
		if (command == null) return false;
		
		if (command.equals("add")) {
			addCommand(commandParser);
		} else if (command.equals("link")) {
			linkCommand(commandParser);
		} else if (command.equals("remove")) {
			removeCommand(commandParser);
		} else if (command.equals("list")) {
			listCommand(commandParser);
		} else if (command.equals("goto")) {
			gotoCommand(commandParser);
		} else if (command.equals("price")) {
			priceCommand(commandParser);
		} else if (command.equals("reward")) {
			rewardCommand(commandParser);
		} else if (command.equals("join_named_arena")) {
			joinCommand(commandParser);
		} else if (command.equals("leave")) {
			leaveCommand(commandParser);
		} else if (command.equals("reset")) {
			resetCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
		}
		return true;
	}

	void addCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.add")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		String name =commandParser.getArgumentString();
		Player player = commandParser.getPlayer();
		if (!this._controller.verifyArenaNameIsNew(player, name)) return;

		boolean success = this._controller.createOrUpdateArena(player, name);
		if (success) Config.M.arenaAdded.sendMessage(player, name);
		this._controller.delayedSave();
	}

	void removeCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.remove")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		CommandSender sender = commandParser.getSender();
		String name =commandParser.getArgumentStringAsLowercase();
		if (!this._controller.removeArena(sender, name)) return;
		Config.M.arenaRemoved.sendMessage(sender, name);
		this._controller.delayedSave();
	}

	void linkCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.link")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3, 3)) return;

		CommandSender sender = commandParser.getSender();
		String name1 = commandParser.getArgumentStringAsLowercase();
		String name2 = commandParser.getArgumentStringAsLowercase();
		
		if (!this._controller.linkArenas(sender, name1, name2)) return;
		Config.M.arenasAreLinked.sendMessage(sender, name1, name2);
		this._controller.delayedSave();
	}

	void gotoCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.goto")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		Player player = commandParser.getPlayer();
		String name =commandParser.getArgumentStringAsLowercase();

		if (!this._controller.gotoArena(player, name)) return;
		Config.M.gotoArena.sendMessage(player, name);
	}

	void priceCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.price")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 3)) return;

		Player player = commandParser.getPlayer();
		String name =commandParser.getArgumentStringAsLowercase();
		double amount = commandParser.getArgumentDouble(0.0);

		if (!this._controller.priceArena(player, name, amount)) return;
		this._controller.delayedSave();
		Config.M.priceArena.sendMessage(player, name, amount);
	}

	void rewardCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.reward")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 3)) return;

		Player player = commandParser.getPlayer();
		String name =commandParser.getArgumentStringAsLowercase();
		double amount = commandParser.getArgumentDouble(0.0);

		if (!this._controller.rewardArena(player, name, amount)) return;
		this._controller.delayedSave();
		Config.M.rewardArena.sendMessage(player, name, amount);
	}

	void joinCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.join")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		Player player = commandParser.getPlayer();
		String name =commandParser.getArgumentStringAsLowercase();

		if (!this._controller.joinArena(player, name)) return;
		Config.M.joinArena.sendMessage(player, name);
	}

	void leaveCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.leave")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		Player player = commandParser.getPlayer();
		String name =commandParser.getArgumentStringAsLowercase();

		if (!this._controller.leaveGame(player)) return;
		Config.M.leftArena.sendMessage(player, name);
	}

	void resetCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.reset")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		Player player = commandParser.getPlayer();

		if (!this._controller.resetGame(player)) return;
	}

	void listCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eir.list")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		Player player = commandParser.getPlayer();

		player.sendMessage("Arenas:");
		this._controller.listArenas(player);
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {

		if (command.equals("add")) {
			sender.sendMessage(ADD_COMMAND);
		} else if (command.equals("link")) {
			sender.sendMessage(LINK_COMMAND);
		} else if (command.equals("remove")) {
			sender.sendMessage(REMOVE_COMMAND);
		} else if (command.equals("list")) {
			sender.sendMessage(LIST_COMMAND);
		} else if (command.equals("goto")) {
			sender.sendMessage(GOTO_COMMAND);
		} else if (command.equals("join_named_arena")) {
			sender.sendMessage(JOIN_COMMAND);
		} else if (command.equals("leave")) {
			sender.sendMessage(LEAVE_COMMAND);
		} else if (command.equals("reset")) {
			sender.sendMessage(RESET_COMMAND);
		} else if (command.equals("price")) {
			sender.sendMessage(PRICE_COMMAND);
		} else if (command.equals("reward")) {
			sender.sendMessage(REWARD_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}	
	}
}
