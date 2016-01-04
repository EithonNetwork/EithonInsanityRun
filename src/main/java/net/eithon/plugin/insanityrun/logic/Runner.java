package net.eithon.plugin.insanityrun.logic;

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
import net.eithon.plugin.insanityrun.logic.Arena.GameStyle;
import net.eithon.plugin.insanityrun.logic.BlockUnderFeet.RunnerEffect;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

class Runner implements IUuidAndName {
	private Arena _arena;
	private Player _player;
	private ScoreKeeper _scoreKeeper;
	private boolean _isInGame;
	private boolean _isFrozen;
	private Block _lastBlock;
	private long _lastMoveTime;
	private Location _lastLocation;
	private Location _lastCheckPoint;
	private HashMap<Point, Location> _goldBlocks;
	private boolean _stopTeleport;
	private PlayerState _playerState;
	private boolean _controlSpeed;
	private double _speed;
	private Minecart _minecart;

	Runner(Player player, Arena arena)
	{
		this._player = player;
		this._arena = arena;
		this._playerState = new PlayerState(player);
		this._scoreKeeper = new ScoreKeeper(player);
		switch (arena.getGameStyle()) {
		case INSANITY_RUN:
			break;
		case KART:
			this._player.setFlying(false);
			this._controlSpeed = true;
			break;
		case FLY:
			this._player.setFlying(true);
			this._controlSpeed = true;
			break;
		}

		if (this._controlSpeed) {
			Vector direction = arena.getSpawnLocation().getDirection();
			if (mustStayOnGround())  {
				direction = direction.setY(0.0);
				direction = direction.multiply(1/direction.length());
			}
			this._player.setVelocity(direction.multiply(Config.V.startSpeed));
		}

		//player.sendMessage(ChatColor.GREEN + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".readyToPlay"));

		// Construct new player object
		player.setGameMode(GameMode.SURVIVAL);
		teleportToSpawn();
	}

	private boolean mustStayOnGround() {
		return this._arena.getGameStyle() == GameStyle.KART;
	}

	public void maybeLeaveGameBecauseOfTeleport(Location from, Location to) {
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

	public Arena getArena() { return this._arena; }
	public Player getPlayer() { return this._player; }
	public boolean isInGame() { return this._isInGame; }
	public boolean isFrozen() { return this._isFrozen; }
	public boolean hasBeenIdleTooLong() { return this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore()-this._lastMoveTime > Config.V.idleKickTimeSeconds*1000; }


	public void setIsFrozen(boolean isFrozen) { 
		this._isFrozen = isFrozen;
		Player player = getPlayer();
		if (isFrozen) {
			player.setWalkSpeed(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		}
		else {
			player.setWalkSpeed(0.2f);
			player.removePotionEffect(PotionEffectType.JUMP);
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

	private void leaveGame(boolean refund, boolean teleportToStart) {
		verbose("leaveGame", "Enter refund: %s, teleportToStart: %s",
				refund?"true":"false", teleportToStart?"true":"false");
		this._isInGame = false;
		this._scoreKeeper.disable();
		final Player player = this._player;
		if (player == null) return;	
		PotionEffectMap.removePotionEffects(player);
		player.setFireTicks(0);
		if (refund) refundMoney();
		this._playerState.restore(teleportToStart);
		PlayerLeftArenaEvent e = new PlayerLeftArenaEvent(this.getPlayer(), this._arena);
		this._player.getServer().getPluginManager().callEvent(e);
		verbose("leaveGame", "Leave");
	}

	public void doRepeatedly() {
		final long currentRuntime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
		if (this._isFrozen || !this._isInGame) {
			this._lastMoveTime = currentRuntime;
			if (!this._isInGame) return;
		}
		Block currentBlock = this._player.getLocation().getBlock();
		if (this._controlSpeed) this._speed += Config.V.speedIncrease;
		if (!lastBlockIsSame(currentBlock)) {
			this._lastMoveTime = currentRuntime;
			this._lastBlock = currentBlock;
		} else {
			if ((currentRuntime - this._lastMoveTime) > Config.V.idleKickTimeSeconds*500) {
				this._player.playSound(this._lastLocation, Sound.GHAST_SCREAM, 1, 1);
			}
		}
	}

	public boolean playerMoved(final EithonPlugin plugin, final Location currentLocation) {
		if (!this._isInGame) return false;
		this._scoreKeeper.updateTimeScore();
		boolean runnerLeftGame = false;
		updateLocation(currentLocation);
		final BlockUnderFeet firstBlockUnderFeet = new BlockUnderFeet(currentLocation);
		final RunnerEffect runnerEffect = firstBlockUnderFeet.getRunnerEffect();
		if (runnerEffect == RunnerEffect.NONE) return false;
		boolean playSound = true;
		if (runnerEffect == RunnerEffect.COIN) {
			playSound = maybeGetCoin(firstBlockUnderFeet);
		}
		if (playSound) SoundMap.playSound(runnerEffect, this._lastLocation);
		// Player effects when walking on blocks
		switch(runnerEffect) {
		case SLOW:
			if (!PotionEffectMap.hasPotionEffect(this._player, PotionEffectType.SLOW)) {
				Config.M.effectSlowActivated.sendMessage(this._player);
			}
			break;
		case JUMP:
			jump();
			Config.M.jumpActivated.sendMessage(this._player);
			break;
		case PUMPKIN_HELMET:
			TemporaryEffects.pumpkinHelmet.run(TimeMisc.secondsToTicks(Config.V.pumpkinHelmetSeconds), this);
			Config.M.pumpkinHelmetActivated.sendMessage(this._player);
			break;
		case FREEZE:
			if (!isFrozen()) {
				TemporaryEffects.freeze.run(TimeMisc.secondsToTicks(Config.V.freezeSeconds), this);
				Config.M.freezeActivated.sendMessage(this._player);
			}
			break;
		case BOUNCE:
			bounceBack();
			Config.M.bounceActivated.sendMessage(this._player);
			break;
		case CHECKPOINT:
			if (!atLastCheckPoint()) {
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
		PotionEffectMap.addPotionEffects(runnerEffect, this._player);
		return runnerLeftGame;
	}

	private boolean willCollide() {
		Location location = this._lastLocation.clone();
		Block block = location.getBlock();
		int i = 0;
		do {
			location = location.add(this._player.getVelocity());
			if (i++ > 10) return false;
		} while (block.equals(location.getBlock()));
		return (location.getBlock().getType() != Material.AIR);
	}

	private boolean atLastCheckPoint() {
		return atLastCheckPoint(this._lastLocation);
	}

	private boolean atLastCheckPoint(Location location) {
		if ((location == null) || (this._lastCheckPoint == null)) return false;
		return location.getBlock().equals(this._lastCheckPoint.getBlock());
	}

	private void jump() {
		this._player.setVelocity(this._player.getVelocity().setY(Config.V.jumpSpeed));
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
		// TODO: NOT IMPLEMENTED!
	}

	private boolean lastBlockIsSame(final Block currentBlock) {
		return (this._lastBlock.getX() == currentBlock.getX()) && (this._lastBlock.getZ() == currentBlock.getZ());
	}

	private Location updateLocation() {
		updateLocation(this._player.getLocation());
		return this._lastLocation;
	}

	private void updateLocation(Location location) {
		this._lastLocation = this._player.getLocation().clone();
		this._lastBlock = this._lastLocation.getBlock();
		this._lastMoveTime = this._scoreKeeper.getRunTimeInMillisecondsAndUpdateScore();
		if (this._controlSpeed) {
			this._player.setVelocity(getVelocity());
		}
	}

	void teleportToSpawn() {
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
		if (this._controlSpeed) {
			this._speed = Config.V.startSpeed;
		}
		Location spawnLocation = this._arena.getSpawnLocation();
		safeTeleport(spawnLocation);
		if (this._arena.getGameStyle() == GameStyle.KART) {
			Minecart minecart = spawnLocation.getWorld().spawn(spawnLocation, Minecart.class);
			minecart.setPassenger(this._player);
		}
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

	private Vector getVelocity() {
		try {
			final Vector currentViewDirection = this._player.getLocation().getDirection().clone();
			if (mustStayOnGround()) {
				currentViewDirection.setY(0);
				if (currentViewDirection.length() == 0.0) return null;
				currentViewDirection.multiply(1/currentViewDirection.length());
			}
			final Vector currentVelocity = this._player.getVelocity().clone();
			final Vector goalVelocity= currentViewDirection.multiply(this._speed);
			final Vector velocityDelta = goalVelocity.subtract(currentVelocity);
			double length = velocityDelta.length();
			Vector velocityChange = velocityDelta;
			if (length > Config.V.velocityChangeSpeed) {
				velocityChange = velocityDelta.multiply(Config.V.velocityChangeSpeed/length);
			}
			Vector newVelocity = currentVelocity.add(velocityChange);
			final double newSpeed = newVelocity.length();
			if (newSpeed > 0.0) {
				newVelocity = newVelocity.multiply(this._speed/newSpeed);
			} else {
				newVelocity = currentViewDirection.multiply(Config.V.startSpeed);
			}
			if (mustStayOnGround()) {
				newVelocity.setY(0);
				if (newVelocity.length() == 0.0) return null;
				newVelocity.multiply(this._speed/newVelocity.length());
			}
			return newVelocity;
		} catch (Exception e) {
			Logger.libraryError("EithonRace.Runner.getVelocity() failed.\n%s", 
					e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private boolean maybeGetCoin(BlockUnderFeet firstBlockUnderFeet) {
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
	private void endLevelOrGame(EithonPlugin plugin) {
		final long runTimeInMilliseconds = this._scoreKeeper.updateTimeScore();
		final long coins = this._scoreKeeper.getCoins();
		this._isInGame = false;
		PotionEffectMap.removePotionEffects(this._player);
		WinnerFirework.doIt(this._lastLocation);
		if (Config.V.broadcastWins) {
			final double runTimeInSeconds = runTimeInMilliseconds/1000.0;
			Config.M.broadcastSuccess.broadcastMessage(this._player.getName(), this._arena.getName(), 
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
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: Runner.%s: %s", method, message);
	}

	private boolean mustNotBeIdle() {
		return this._arena.getGameStyle() == GameStyle.INSANITY_RUN;
	}
}

