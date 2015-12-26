package net.eithon.plugin.insanityrun.logic;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.VaultFacade;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.insanityrun.Config;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Controller {
	private PlayerCollection<Runner> _runners = new PlayerCollection<Runner>();
	private HashMap<String, Arena> _arenas = new HashMap<String, Arena>();
	private EithonPlugin _eithonPlugin;
	private BukkitTask _idleTask;
	private VaultFacade _vaultFacade;

	public Controller(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._runners = new PlayerCollection<Runner>();
		this._vaultFacade = new VaultFacade(eithonPlugin);
		ScoreDisplay.initialize();
		TemporaryEffects.initialize(eithonPlugin);
		SoundMap.initialize(eithonPlugin);
		PotionEffectMap.initialize(eithonPlugin);
		BlockUnderFeet.initialize(eithonPlugin);
		this._idleTask = null;
		load();
	}

	public void delayedSave() {
		delayedSave(1);
	}

	private void delayedSave(double seconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				save();
			}
		}, TimeMisc.secondsToTicks(seconds));		
	}

	public void delayedLoad(JavaPlugin plugin, double seconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				load();
			}
		}, TimeMisc.secondsToTicks(seconds));		
	}

	@SuppressWarnings("unchecked")
	public void save() {
		JSONArray arenas = new JSONArray();
		for (Arena arena : this._arenas.values()) {
			arenas.add(arena.toJson());
		}
		if ((arenas == null) || (arenas.size() == 0)) {
			this._eithonPlugin.getEithonLogger().info("No Arenas saved.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Saving %d Arenas", arenas.size());
		File file = getArenaStorageFile();

		FileContent fileContent = new FileContent("Arena", 1, arenas);
		fileContent.save(file);
	}

	void load() {
		File file = getArenaStorageFile();
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "File was empty.");
			return;			
		}
		JSONArray array = (JSONArray) fileContent.getPayload();
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The list of TravelPads was empty.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Restoring %d Arenas from file.", array.size());
		this._arenas = new HashMap<String, Arena>();
		for (int i = 0; i < array.size(); i++) {
			verbose("load", "Loading Arena %d", i);
			Arena arena = null;
			try {
				arena = Arena.getFromJson((JSONObject) array.get(i));
				if (arena == null) {
					this._eithonPlugin.getEithonLogger().error("Could not load arena %d (result was null).", i);
					continue;
				}
				this._eithonPlugin.getEithonLogger().info("Loaded arena %s", arena.getName());
				this._arenas.put(arena.getName().toLowerCase(), arena);
			} catch (Exception e) {
				this._eithonPlugin.getEithonLogger().error("Could not load arena %d (exception).", i);
				if (arena != null) this._eithonPlugin.getEithonLogger().error("Could not load arena %s", arena.getName());
				this._eithonPlugin.getEithonLogger().error("%s", e.toString());
				throw e;
			}
		}
		for (Arena arena : this._arenas.values()) {
			String linkedToArenaName = arena.getLinkedToArenaName();
			if (linkedToArenaName == null) continue;
			Arena linkedToArena = getArena(linkedToArenaName);
			arena.linkToArena(linkedToArena);
		}
	}

	private File getArenaStorageFile() {
		File file = this._eithonPlugin.getDataFile("arenas.json");
		return file;
	}

	private void startRepeatingTask() {
		this._idleTask = this._eithonPlugin.getServer().getScheduler().runTaskTimer(this._eithonPlugin, new Runnable() {
			@Override
			public void run() {
				doEverySecond();
			}
		}, TimeMisc.secondsToTicks(1), TimeMisc.secondsToTicks(1)); // Every second
	}

	void doEverySecond() {
		Iterator<Entry<UUID, Runner>> iterator = this._runners.entrySet().iterator();
		while(iterator.hasNext()) {
			Runner runner = iterator.next().getValue();
			runner.doRepeatedly();
			if (runner.hasBeenIdleTooLong()) {
				Player player = runner.getPlayer();
				runner.leaveGame();
				iterator.remove();
				Config.M.idleKick.sendMessage(player);
			}
		}
		if (this._runners.size()==0) endRepeatingTask();
	}

	public void endRepeatingTask() {
		if (this._idleTask == null) return;
		this._eithonPlugin.getServer().getScheduler().cancelTask(this._idleTask.getTaskId());
		this._idleTask = null;
	}

	public boolean joinArena(Player player, String arenaName) {
		Arena arena = getArenaByNameOrInformUser(player, arenaName);
		if (arena == null) return false;
		return joinArena(player, arena);
	}

	private boolean joinArena(Player player, Arena arena) {
		if (arena == null) return false;
		if (!payOrInformPlayer(player,arena)) return false;

		Runner runner = arena.joinGame(player);
		this._runners.put(player, runner);
		if (this._idleTask == null) startRepeatingTask();
		return true;
	}

	public boolean payOrInformPlayer(Player player, Arena arena) {
		double amount = arena.getPrice();
		if (!this._vaultFacade.withdraw(player, amount)) {
			Config.M.withdrawFailed.sendMessage(player, amount);
			return false;
		}
		Config.M.withdrawSucceeded.sendMessage(player, amount);
		return true;
	}

	public boolean leaveGame(Player player) {
		Runner runner = this._runners.get(player);
		if (runner == null) return true;
		leaveGame(runner);
		return true;
	}

	public boolean resetGame(Player player) {
		Runner runner = this._runners.get(player);
		if (runner == null) return true;
		runner.teleportToSpawn();
		return true;
	}

	private void leaveGame(Runner runner) {
		runner.leaveGame();
		this._runners.remove(runner.getPlayer());
		if (this._runners.size()==0) endRepeatingTask();
	}

	// When we need to kick all runners, do that with refund
	public void kickAllRunners() {
		Iterator<Entry<UUID, Runner>> iterator = this._runners.entrySet().iterator();
		while (iterator.hasNext()) {
			Runner runner = iterator.next().getValue();
			runner.leaveGameWithRefund();
			iterator.remove();
		}
	}

	public void playerMovedOneBlock(Player player) {
		final Runner runner = this._runners.get(player);
		if ((runner == null) || !runner.isInGame()) return;

		// Player frozen?
		if (runner.isFrozen()) {
			runner.teleportToLastLocation();
			return;
		}

		boolean runnerLeftGame = runner.movedOneBlock(this._eithonPlugin);
		if (runnerLeftGame) this._runners.remove(player);
	}

	public void maybeLeaveGameBecauseOfTeleport(Player player) {
		final Runner runner = this._runners.get(player);
		if (runner == null) return;
		runner.maybeLeaveGameBecauseOfTeleport();
	}

	public boolean isInGame(Player player) {
		Runner runner = this._runners.get(player);
		return ((runner != null) && runner.isInGame());
	}

	public boolean verifyArenaNameIsNew(CommandSender sender, String name) {
		Arena arena = getArena(name);
		return arena == null;
	}

	public boolean verifyArenaExists(CommandSender sender, String name) {
		Arena arena = getArenaByNameOrInformUser(sender, name);
		return arena != null;
	}

	private Arena getArena(String name) {
		return this._arenas.get(name.toLowerCase());
	}

	public boolean createOrUpdateArena(Player player, String name) {
		Arena arena = new Arena(name, player.getLocation());
		this._arenas.put(arena.getName().toLowerCase(), arena);
		return true;
	}

	Arena getArenaByNameOrInformUser(CommandSender sender, String name) {
		Arena arena = getArena(name);
		if (arena != null) return arena;
		Config.M.unknownArena.sendMessage(sender, name);
		return null;
	}

	public boolean removeArena(CommandSender sender, String name) {
		Arena arena = getArenaByNameOrInformUser(sender, name);
		if (arena == null) return false;
		this._arenas.remove(name);
		return true;
	}

	public boolean linkArenas(CommandSender sender, String name1, String name2) {
		Arena arena1 = getArenaByNameOrInformUser(sender, name1);
		if (arena1 == null) return false;
		Arena arena2 = getArenaByNameOrInformUser(sender, name2);
		if (arena2 == null) return false;
		arena1.linkToArena(arena2);
		return true;
	}

	public boolean gotoArena(Player player, String name) {
		Arena arena = getArenaByNameOrInformUser(player, name);
		if (arena == null) return false;
		player.teleport(arena.getSpawnLocation());
		return true;
	}

	public void listArenas(CommandSender sender) {
		for (Arena arena : this._arenas.values()) {
			Config.M.arenaInfo.sendMessage(sender, arena.getName());
		}
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Controller.%s: %s", method, message);
	}
}
