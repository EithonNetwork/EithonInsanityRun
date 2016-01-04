package net.eithon.plugin.insanityrun.racer;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.insanityrun.Config;
import net.eithon.plugin.insanityrun.logic.Arena;
import net.eithon.plugin.insanityrun.racer.BlockUnderFeet.RunnerEffect;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Runner extends Racer {
	private double _speed;
	private Minecart _minecart;

	public Runner(Player player, Arena arena)
	{
		super(player, arena);
		this._canBeIdle = true;
		Location spawnLocation = arena.getSpawnLocation();
		Vector direction = spawnLocation.getDirection();
		direction = direction.setY(0.0);
		direction = direction.multiply(1/direction.length());
	}

	@Override
	protected Entity getMovingObject() {
		if (this._minecart == null) {
			Location spawnLocation = this._arena.getSpawnLocation();
			this._minecart = spawnLocation.getWorld().spawn(spawnLocation, Minecart.class);
			this._minecart.setPassenger(this._player);
		}
		return this._minecart;
	}

	protected void leaveGame(boolean refund, boolean teleportToStart) {
		super.leaveGame(refund, teleportToStart);
		if (this._minecart != null) {
			this._minecart.remove();
			this._minecart = null;
		}
	}

	public void doRepeatedly() {
		super.doRepeatedly();
		this._speed += Config.V.speedIncrease;
	}

	@Override


	public boolean playerMoved(final EithonPlugin plugin, final Location currentLocation) {
		if (!this._isInGame) return false;
		boolean runnerLeftGame = super.playerMoved(plugin, currentLocation);
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
			if (this._isFrozen) {
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

	@Override
	protected void updateLocation(Location location) {
		super.updateLocation(location);
		getMovingObject().getLocation().setDirection(this._player.getLocation().getDirection());
		Vector velocity = getVelocity();
		if (velocity == null) return;
		getMovingObject().setVelocity(velocity);
	}

	@Override
	public void teleportToSpawn() {
		super.teleportToSpawn();
		this._speed = Config.V.startSpeed;
		Location spawnLocation = this._arena.getSpawnLocation();
		this.getMovingObject().setPassenger(this._player);
		this._player.setWalkSpeed(0.0F);
		this._player.setFlySpeed(0.0F);
		this._player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		getMovingObject().setVelocity(spawnLocation.getDirection().multiply(Config.V.startSpeed));
	}

	private Vector getVelocity1() {
		try {
			final Vector currentViewDirection = getCurrentDirectionWithZeroYComponent();
			final Vector currentVelocity = getMovingObject().getVelocity().clone();
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
			newVelocity.setY(0);
			if (newVelocity.length() == 0.0) return null;
			newVelocity.multiply(this._speed/newVelocity.length());
			return newVelocity;
		} catch (Exception e) {
			Logger.libraryError("EithonRace.Runner.getVelocity() failed.\n%s", 
					e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private Vector getVelocity() {
		try {
			Vector currentViewDirection = getCurrentDirectionWithZeroYComponent();
			if (currentViewDirection == null) currentViewDirection = new Vector(1,0,0);
			final Vector deltaVelocity = currentViewDirection.clone().multiply(Config.V.speedIncrease);
			final Vector currentVelocity = getMovingObject().getVelocity().clone();
			return currentVelocity.add(deltaVelocity);
		} catch (Exception e) {
			Logger.libraryError("EithonRace.Runner.getVelocity() failed.\n%s", 
					e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public Vector getCurrentDirectionWithZeroYComponent() {
		final Vector currentViewDirection = getMovingObject().getLocation().getDirection().clone();
		currentViewDirection.setY(0);
		if (currentViewDirection.length() == 0.0) {
			verbose("getVelocity", "Staring up?");
			return null;
		}
		currentViewDirection.multiply(1.0/currentViewDirection.length());
		return currentViewDirection;
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: KartDriver.%s: %s", method, message);
	}
}

