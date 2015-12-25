package net.eithon.plugin.insanityrun.logic;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.core.IUuidAndName;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.insanityrun.Config;
import net.eithon.plugin.insanityrun.logic.BlockUnderFeet.RunnerEffect;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

class Runner implements IUuidAndName {
	private Arena _arena;
	private Player _player;
	private ScoreKeeper _scoreKeeper;
	private boolean _isInGame;
	private boolean _isFrozen;
	private Location _rememberLocation;
	private GameMode _rememberGameMode;
	private ItemStack _rememberHelmetWorn;
	private Block _lastBlock;
	private long _lastMoveTime;
	private Location _lastLocation;
	private Location _lastCheckPoint;
	private HashMap<Point, Location> _goldBlocks;
	private boolean _stopTeleport;

	Runner(Player player, Arena arena)
	{
		this._player = player;
		this._scoreKeeper = new ScoreKeeper(player);

		//player.sendMessage(ChatColor.GREEN + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".readyToPlay"));

		// Construct new player object

		this._rememberLocation = player.getLocation();
		this._rememberGameMode = player.getGameMode();
		this._rememberHelmetWorn = player.getInventory().getHelmet();
		if (this._rememberHelmetWorn == null) {
			this._rememberHelmetWorn = new ItemStack(Material.AIR, 1, (short) 14);
		}
		player.setGameMode(GameMode.SURVIVAL);
		this._arena = arena;
		teleportToSpawn();
	}

	public void maybeLeaveGameBecauseOfTeleport() {
		if (!this._stopTeleport) return;
		Config.M.teleportKick.sendMessage(this._player);
		leaveGame(false, false);
	}

	public Arena getArena() { return this._arena; }
	public Player getPlayer() { return this._player; }
	public boolean isInGame() { return this._isInGame; }
	public boolean isFrozen() { return this._isFrozen; }
	public boolean hasBeenIdleTooLong() { return this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore()-this._lastMoveTime > Config.V.idleKickTimeSeconds*1000; }


	public void setIsFrozen(boolean isFrozen) { 
		this._isFrozen = isFrozen;
		Player player = getPlayer();
		if (isFrozen) player.setWalkSpeed(0);
		else player.setWalkSpeed(0.2f);
	}

	public void resetHelmet() {
		Player player = getPlayer();
		player.getInventory().setHelmet(this._rememberHelmetWorn);
	}

	@Override
	public String getName()	{ return this._player.getName(); }

	public String toString() { return String.format("%s", this.getName());	}

	@Override
	public UUID getUniqueId() { return this._player.getUniqueId();	}

	public void leaveGame() { leaveGame(false, true); }

	public void leaveGameWithRefund() { leaveGame(true, true);	}

	private void leaveGame(boolean refund, boolean teleportToStart) {
		verbose("leaveGame", "Enter refund: %s, teleportToStart: %s",
				refund?"true":"false", teleportToStart?"true":"false");
		this._isInGame = false;
		this._scoreKeeper.disable();
		final Player player = this._player;
		if (player == null) return;	
		PotionEffectMap.removePotionEffects(player);
		player.setFireTicks(0);
		player.getInventory().setHelmet(this._rememberHelmetWorn);
		player.setGameMode(this._rememberGameMode);
		if (refund) refundMoney();
		if (teleportToStart) teleportToStart();
		this._arena.runnerLeft(this);
		verbose("leaveGame", "Leave");
	}

	public void doRepeatedly() {
		final long currentRuntime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
		if (this._isFrozen || !this._isInGame) {
			this._lastMoveTime = currentRuntime;
			if (!this._isInGame) return;
		}
		Block currentBlock = this._player.getLocation().getBlock();
		if (!lastBlockIsSame(currentBlock)) {
			this._lastMoveTime = currentRuntime;
			this._lastBlock = currentBlock;
		} else {
			this._player.playSound(this._lastLocation, Sound.ENDERMAN_IDLE, 1, 1);
		}
	}

	public boolean movedOneBlock(final Plugin plugin) {
		if (!this._isInGame) return false;
		this._scoreKeeper.updateTimeScore();
		boolean runnerLeftGame = false;
		final Location currentLocation = updateLocation();
		final BlockUnderFeet firstBlockUnderFeet = new BlockUnderFeet(currentLocation);
		final RunnerEffect runnerEffect = firstBlockUnderFeet.getRunnerEffect();
		if (runnerEffect == RunnerEffect.NONE) return false;
		boolean playSound = true;
		if (runnerEffect == RunnerEffect.COIN) {
			playSound = maybeGetCoin(firstBlockUnderFeet);
		}
		if (playSound) SoundMap.playSound(runnerEffect, this._lastLocation);
		PotionEffectMap.addPotionEffects(runnerEffect, this._player);
		// Player effects when walking on blocks
		switch(runnerEffect) {
		case JUMP:
			jump();
			break;
		case PUMPKIN_HELMET:
			TemporaryEffects.pumpkinHelmet.run(TimeMisc.secondsToTicks(Config.V.pumpkinHelmetSeconds), this);
			break;
		case FREEZE:
			if (!isFrozen()) TemporaryEffects.freeze.run(TimeMisc.secondsToTicks(Config.V.freezeSeconds), this);
			break;
		case BOUNCE:
			bounceBack();
			break;
		case CHECKPOINT:
			if (!this._lastLocation.getBlock().equals(this._lastCheckPoint.getBlock())) {
				this._lastCheckPoint = this._lastLocation;
				Config.M.reachedCheckPoint.sendMessage(this._player);
			}
			break;
		case FINISH:
			endLevelOrGame(plugin);
			break;
		case WATER:
		case LAVA:
			Config.M.failRestart.sendMessage(this._player);
			runnerLeftGame = restart(plugin);
			break;
		default:
			break;
		}
		return runnerLeftGame;
	}

	private void jump() {
		verbose("jump", "Enter");
		this._player.setVelocity(this._player.getVelocity().setY(1.5));
		verbose("jump", "Leave");
	}

	private void bounceBack() {
		verbose("bounceBack", "Enter");
		this._player.setVelocity(this._lastLocation.getDirection().multiply(-1));
		verbose("bounceBack", "Leave");
	}

	public void teleportToLastLocation() {
		safeTeleport(this._lastLocation);
	}

	// Refund money if kicked
	private void refundMoney() {
		/*
		if (InsanityRun.useVault && InsanityRun.plugin.getConfig().getInt(arenaName + ".charge") > 0) {
			EconomyResponse res = InsanityRun.economy.depositPlayer(playerName, InsanityRun.plugin.getConfig().getInt(arenaName + ".charge"));
			if(!res.transactionSuccess()) {
				Bukkit.getConsoleSender().sendMessage(String.format(InsanityRun.gameName + " Vault Deposit - An error occured: %s", res.errorMessage));
			}
		}	
		 */	
	}

	private boolean lastBlockIsSame(final Block currentBlock) {
		return (this._lastBlock.getX() == currentBlock.getX()) && (this._lastBlock.getZ() == currentBlock.getZ());
	}

	private Location updateLocation() {
		this._lastLocation = this._player.getLocation();
		this._lastBlock = this._lastLocation.getBlock();
		this._lastMoveTime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
		return this._lastLocation;
	}

	void teleportToSpawn() {
		this._isInGame = true;
		this._isFrozen = false;
		this._scoreKeeper.resetCoins();
		this._lastMoveTime = 0;
		this._stopTeleport = true;
		this._goldBlocks = new HashMap<Point, Location>();
		PotionEffectMap.removePotionEffects(this._player);
		this._player.setFireTicks(0);
		this._player.setWalkSpeed(0.2f);
		this._player.setFoodLevel(20);
		safeTeleport(this._arena.getSpawnLocation());
	}

	private void teleportToLastCheckPoint() {
		this._isInGame = true;
		this._lastMoveTime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
		PotionEffectMap.removePotionEffects(this._player);
		this._player.setFireTicks(0);
		Location location = this._lastCheckPoint;
		if (location == null) location = this._arena.getSpawnLocation();
		else revertGoldCoins();
		safeTeleport(location);
	}

	public void revertGoldCoins() {
		if (this._lastCheckPoint == null) return;
		Iterator<Entry<Point, Location>> iterator = this._goldBlocks.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<Point,Location> entry = iterator.next();
			if ((entry == null) || (entry.getValue() == null)) continue;
			if (entry.getValue().equals(this._lastCheckPoint)) {
				this._scoreKeeper.addCoinScore(-1);
				iterator.remove();
			}
		}
	}

	private void teleportToStart() {
		safeTeleport(this._rememberLocation);
	}

	private void safeTeleport(final Location location) {
		try {
			this._stopTeleport = false;
			this._player.teleport(location);
			updateLocation();
		} finally {
			this._stopTeleport = true;
		}
	}

	private boolean maybeGetCoin(BlockUnderFeet firstBlockUnderFeet) {
		verbose("maybeGetCoin", "Enter");
		Point point = new Point(firstBlockUnderFeet.getBlock().getX(), firstBlockUnderFeet.getBlock().getY());
		if (this._goldBlocks.containsKey(point)) {
			verbose("maybeGetCoin", "Leave false");
			return false;
		}
		this._goldBlocks.put(point, this._lastCheckPoint);
		this._scoreKeeper.addCoinScore(1);
		verbose("maybeGetCoin", "Leave true");
		return true;
	}

	private boolean restart(Plugin plugin) {
		this._isInGame = false;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (Config.V.waterRestartsRun) {
					verbose("restart", "WaterRestartsRun");
					teleportToSpawn();
				} else if (Config.V.useCheckpoints) {
					verbose("restart", "UseCheckPoints");
					teleportToLastCheckPoint();
				} else {
					verbose("restart", "LeaveGame");
					leaveGame();
				}
			}
		}, Config.V.restartAfterTicks);
		// Will leave game?
		return (!Config.V.waterRestartsRun && !Config.V.useCheckpoints);
	}

	// Player ran over Redstone. End the level and start next or end game
	private void endLevelOrGame(Plugin plugin) {
		this._scoreKeeper.updateTimeScore();
		this._isInGame = false;
		PotionEffectMap.removePotionEffects(this._player);
		WinnerFirework.doIt(this._lastLocation);
		if (Config.V.broadcastWins) {
			Config.M.broadcastSuccess.broadcastMessage(this._player.getName(), this._arena.getName(), 5.7);
		}

		/*
		if (InsanityRun.useVault && InsanityRun.plugin.getConfig().getInt(arenaName + ".pay") > 0) {
			EconomyResponse res = InsanityRun.economy.depositPlayer(player.getName(), InsanityRun.plugin.getConfig().getInt(arenaName + ".pay"));
			if(res.transactionSuccess()) {
				player.sendMessage(ChatColor.GOLD + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".vaultAward") + " " + InsanityRun.plugin.getConfig().getInt(arenaName + ".pay") + " " + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".payCurrency"));
			} else {
				player.sendMessage(String.format("An error occured: %s", res.errorMessage));
			}
		}
		 */
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				// Check for linked arena
				/*
				if (InsanityRun.plugin.getConfig().getString(arenaName + ".link")!=null) {
					if ((!InsanityRun.useVault) || (InsanityRun.useVault && canAfford(currentPlayerObject,InsanityRun.plugin.getConfig().getString(arenaName + ".link")))) {
						// Remove player from this arena
						int playCount;
						playCount = InsanityRun.playersInThisArena.get(arenaName);
						playCount--;
						InsanityRun.playersInThisArena.put(arenaName, playCount);
						GameManager.updateJoinSign(arenaName);
						currentPlayerObject.setCoins(0);
						// Reset Scoreboard
						Score time = InsanityRun.objective.getScore(ChatColor.LIGHT_PURPLE + "Time:");
						Score score = InsanityRun.objective.getScore(ChatColor.YELLOW + "Coins:");
						time.setScore(0);
						score.setScore(0);
						GameManager.teleportToSpawn(player, InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						GameManager.updatePlayerXYZ(player);
						currentPlayerObject.setLastCheckpoint(player.getLocation());
						currentPlayerObject.setInArena(InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						currentPlayerObject.setFrozen(false);
						currentPlayerObject.setLastMovedTime(System.currentTimeMillis());
						currentPlayerObject.setStartRaceTime(System.currentTimeMillis());
						currentPlayerObject.setInGame(true);
						if (InsanityRun.playersInThisArena.get(InsanityRun.plugin.getConfig().getString(arenaName + ".link")) == null) {
							InsanityRun.playersInThisArena.put(InsanityRun.plugin.getConfig().getString(arenaName + ".link"), 0);
						}
						playCount = InsanityRun.playersInThisArena.get(InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						playCount++;
						InsanityRun.playersInThisArena.put(InsanityRun.plugin.getConfig().getString(arenaName + ".link"), playCount);
						GameManager.updateJoinSign(InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						return;
					}

				}
				 */
				// Check if player gets sent back to JOIN sign
				if (Config.V.telePortAfterEnd) {
					teleportToStart();
				}
				leaveGame();
			}
		}, 20 * 5); // 20 ticks per second x 5 seconds
	}

	/*
	// Can player afford to play next arena?
	@SuppressWarnings("deprecation")
	private static boolean canAfford(iPlayer currentPlayerObject, String arenaName) {
		Player player = InsanityRun.plugin.getServer().getPlayer(currentPlayerObject.getPlayerName());
		String playerName = currentPlayerObject.getPlayerName();
		// Does player have enough money to play?
		if (InsanityRun.useVault) {
			if (InsanityRun.economy.getBalance(player.getName()) < InsanityRun.plugin.getConfig().getInt(arenaName + ".charge")) {
				player.sendMessage(ChatColor.RED + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".notEnoughMoneyText") + InsanityRun.plugin.getConfig().getInt(arenaName + ".charge") + " " + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".payCurrency"));
				return false;
			}
			else {
				// Withdraw money
				EconomyResponse r = InsanityRun.economy.withdrawPlayer(playerName, InsanityRun.plugin.getConfig().getInt(arenaName + ".charge"));
				if(r.transactionSuccess()) {
					return true;
				} else {
					player.sendMessage(String.format("An error occured: %s", r.errorMessage));
					return false;
				}
			}
		}
		return false;
	}
	 */


	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: Runner.%s: %s", method, message);
	}
}

