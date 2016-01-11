package net.eithon.plugin.insanityrun.racer;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.facades.VaultFacade;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.insanityrun.Config;
import net.eithon.plugin.insanityrun.logic.Arena;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Racer implements IUuidAndName {
	protected Arena _arena;
	protected Player _player;
	protected ScoreKeeper _scoreKeeper;
	protected boolean _isInGame;
	protected boolean _isFrozen;
	protected Block _lastBlock;
	protected long _lastMoveTime;
	protected Location _lastLocation;
	protected Location _lastCheckPoint;
	protected HashMap<Point, Location> _goldBlocks;
	protected boolean _stopTeleport;
	protected PlayerState _playerState;
	protected boolean _canBeIdle;
	protected static EithonPlugin eithonPlugin;

	Racer(Player player, Arena arena)
	{
		this._player = player;
		this._arena = arena;
		this._playerState = new PlayerState(player);
		this._scoreKeeper = new ScoreKeeper(player);
		this._canBeIdle = false;
		player.setGameMode(GameMode.SURVIVAL);
		teleportToSpawn();
	}

	public static void initialize(EithonPlugin plugin) {
		eithonPlugin = plugin;
		ScoreDisplay.initialize();
		TemporaryEffects.initialize(eithonPlugin);
		SoundMap.initialize(eithonPlugin);
		PotionEffectMap.initialize(eithonPlugin);
		BlockUnderFeet.initialize(eithonPlugin);	
	}

	protected Entity getMovingObject() {
		return this._player;
	}

	public void maybeLeaveGameBecauseOfTeleport(Location from, Location to) {
		if (!this._isInGame) return;
		if (!this._stopTeleport) return;
		if (shortTeleport(from.getBlock(), to.getBlock())) return;
		Config.M.teleportKick.sendMessage(this._player);
		leaveGame(false, false);
	}

	private boolean shortTeleport(Block from, Block to) {
		boolean isShort = (Math.abs(from.getX() - to.getX()) < 5) &&
				(Math.abs(from.getZ() - to.getZ()) < 5) &&
				(Math.abs(from.getY() - to.getY()) < 5);

		if (!isShort) return false;
		Logger.libraryWarning("EithonInsanityRun: Short teleport from (%s) to (%s)", from.toString(), to.toString());
		return isShort;
	}

	// protected Arena getArena() { return this._arena; }
	Player getPlayer() { return this._player; }
	public boolean isInGame() { return this._isInGame; }
	// public boolean isFrozen() { return this._isFrozen; }
	private boolean hasBeenIdleTooLong() { return this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore()-this._lastMoveTime > Config.V.idleKickTimeSeconds*1000; }

	public void setIsFrozen(boolean isFrozen) { 
		this._isFrozen = isFrozen;
		if (isFrozen) {
			this._player.setWalkSpeed(0);
			this._player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		}
		else {
			this._player.setWalkSpeed(0.2f);
			this._player.removePotionEffect(PotionEffectType.JUMP);
		}
	}

	public void resetHelmet() {
		this._playerState.restoreHelmetWorn();
	}

	@Override
	public String getName()	{ return this._player.getName(); }

	public String toString() { return String.format("%s", this.getName());	}

	@Override
	public UUID getUniqueId() { return this._player.getUniqueId();	}

	public void leaveGame() { leaveGame(false, Config.V.telePortAfterEnd); }

	public void leaveGameWithRefund() { leaveGame(true, Config.V.telePortAfterEnd);	}

	protected void leaveGame(boolean refund, boolean teleportToStart) {
		this._isInGame = false;
		this._scoreKeeper.disable();
		PotionEffectMap.removePotionEffects(this._player);
		this._player.setFireTicks(0);
		if (refund) refundMoney();
		this._playerState.restore(teleportToStart);
		PlayerLeftArenaEvent e = new PlayerLeftArenaEvent(this._player, this._arena);
		this._player.getServer().getPluginManager().callEvent(e);
	}

	public void doRepeatedly() {
		updateLocation();
		final long currentRuntime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
		if (this._isFrozen || !this._isInGame) {
			this._lastMoveTime = currentRuntime;
			if (!this._isInGame) return;
		}
		if (this._canBeIdle) return;
		verifyNotIdle(currentRuntime);
	}

	public void verifyNotIdle(final long currentRuntime) {
		Block currentBlock = getMovingObject().getLocation().getBlock();
		if (!lastBlockIsSame(currentBlock)) {
			this._lastMoveTime = currentRuntime;
			this._lastBlock = currentBlock;
		} else {
			if ((currentRuntime - this._lastMoveTime) > Config.V.idleKickTimeSeconds*500) {
				this._player.playSound(this._lastLocation, Sound.GHAST_SCREAM, 1, 1);
			}	
			if (hasBeenIdleTooLong()) {
				leaveGame();
				Config.M.idleKick.sendMessage(this._player);
			}

		}
	}

	public boolean playerMoved(final EithonPlugin plugin, final Location currentLocation) {
		if (!this._isInGame) return false;
		this._scoreKeeper.updateTimeScore();
		boolean runnerLeftGame = false;
		updateLocation(currentLocation);
		return false;
	}

	protected boolean willCollide() {
		Location location = this._lastLocation.clone();
		Block block = location.getBlock();
		int i = 0;
		do {
			location = location.add(getMovingObject().getVelocity());
			if (i++ > 10) return false;
		} while (block.equals(location.getBlock()));
		Material material = location.getBlock().getType();
		if (material != Material.AIR) verbose("willCollide", "material=%s", material.toString());
		return (material != Material.AIR);
	}

	protected boolean atLastCheckPoint() {
		return atLastCheckPoint(this._lastLocation);
	}

	protected boolean atLastCheckPoint(Location location) {
		if ((location == null) || (this._lastCheckPoint == null)) return false;
		return location.getBlock().equals(this._lastCheckPoint.getBlock());
	}

	protected void jump() {
		getMovingObject().setVelocity(this._player.getVelocity().setY(Config.V.jumpSpeed));
	}

	protected void bounceBack() {
		getMovingObject().setVelocity(this._lastLocation.getDirection().multiply(-1));
	}

	protected void teleportToLastLocation() {
		safeTeleport(this._lastLocation);
	}

	// Refund money if kicked
	protected void refundMoney() {
		// TODO: NOT IMPLEMENTED!
	}

	protected boolean lastBlockIsSame(final Block currentBlock) {
		return (this._lastBlock.getX() == currentBlock.getX()) && (this._lastBlock.getZ() == currentBlock.getZ());
	}

	protected Location updateLocation() {
		updateLocation(getMovingObject().getLocation().clone());
		return this._lastLocation;
	}

	protected void updateLocation(Location location) {
		this._lastLocation = location;
		this._lastBlock = this._lastLocation.getBlock();
		this._lastMoveTime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
	}

	public void teleportToSpawn() {
		this._isInGame = true;
		this._isFrozen = false;
		this._scoreKeeper.reset();
		this._lastMoveTime = 0;
		this._stopTeleport = true;
		this._goldBlocks = new HashMap<Point, Location>();
		PotionEffectMap.removePotionEffects(this._player);
		this._player.setFireTicks(0);
		this._player.setWalkSpeed(0.2f);
		this._player.setFoodLevel(20);
		Location spawnLocation = this._arena.getSpawnLocation();
		safeTeleport(spawnLocation);
	}

	protected void teleportToLastCheckPoint() {
		this._isInGame = true;
		this._lastMoveTime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
		PotionEffectMap.removePotionEffects(this._player);
		this._player.setFireTicks(0);
		Location location = this._lastCheckPoint;
		if (location == null) location = this._arena.getSpawnLocation();
		else revertGoldCoins();
		safeTeleport(location);
	}

	private void safeTeleport(final Location location) {
		try {
			this._stopTeleport = false;
			this._player.teleport(location);
		} finally {
			this._stopTeleport = true;
		}
		updateLocation();
	}

	public void revertGoldCoins() {
		if (this._lastCheckPoint == null) return;
		Iterator<Entry<Point, Location>> iterator = this._goldBlocks.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<Point,Location> entry = iterator.next();
			if (entry == null) continue;
			if (atLastCheckPoint(entry.getValue())) {
				this._scoreKeeper.addCoinScore(-1);
				iterator.remove();
			}
		}
	}

	protected boolean maybeGetCoin(BlockUnderFeet firstBlockUnderFeet) {
		verbose("maybeGetCoin", "Enter");
		Point point = new Point(firstBlockUnderFeet.getBlock().getX(), firstBlockUnderFeet.getBlock().getZ());
		if (this._goldBlocks.containsKey(point)) {
			verbose("maybeGetCoin", "Leave false");
			return false;
		}
		this._goldBlocks.put(point, this._lastCheckPoint);
		this._scoreKeeper.addCoinScore(1);
		verbose("maybeGetCoin", "Leave true");
		return true;
	}

	protected boolean restart(Plugin plugin) {
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
	protected void endLevelOrGame(EithonPlugin plugin) {
		final long runTimeInMilliseconds = this._scoreKeeper.updateTimeScore();
		final long coins = this._scoreKeeper.getCoins();
		this._isInGame = false;
		PotionEffectMap.removePotionEffects(this._player);
		WinnerFirework.doIt(this._lastLocation);
		if (Config.V.broadcastWins) {
			final double runTimeInSeconds = runTimeInMilliseconds/1000.0;
			Config.M.broadcastSuccess.broadcastMessage(getName(), this._arena.getName(), 
					TimeMisc.secondsToString(runTimeInSeconds), coins);
		}
		double reward = this._arena.getReward();
		if (reward >= 0.01) {
			VaultFacade vaultFacade = new VaultFacade(plugin);
			if (!vaultFacade.deposit(this._player, reward)) {
				Config.M.rewardFailed.sendMessage(this._player, reward);
			} else Config.M.rewarded.sendMessage(this._player, reward);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				leaveGame();
			}
		}, TimeMisc.secondsToTicks(5));
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: Racer.%s: %s", method, message);
	}
}

