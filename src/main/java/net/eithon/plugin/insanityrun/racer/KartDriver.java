package net.eithon.plugin.insanityrun.racer;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.insanityrun.Config;
import net.eithon.plugin.insanityrun.logic.Arena;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class KartDriver extends Racer {
	private double _speed;
	private Minecart _minecart;

	public KartDriver(Player player, Arena arena)
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
		boolean playerLeftGame = super.playerMoved(plugin, getMovingObject().getLocation());
		if (willCollide()) {
			Config.M.failRestart.sendMessage(this._player);
			teleportToSpawn();
		}
		return playerLeftGame;
	}

	@Override
	protected void updateLocation(Location location) {
		super.updateLocation(location);
		this._minecart.setDerailedVelocityMod(getCurrentDirectionWithZeroYComponent().multiply(Config.V.speedIncrease));
	}

	@Override
	public void teleportToSpawn() {
		super.teleportToSpawn();
		this._player.setWalkSpeed(0.0F);
		this._player.setFlySpeed(0.0F);
		this._player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		this._speed = Config.V.startSpeed;
		this.getMovingObject().setPassenger(this._player);
		getMovingObject().setVelocity(getMovingObject().getLocation().getDirection().clone().multiply(Config.V.startSpeed));
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
		Vector currentViewDirection = this._player.getLocation().getDirection().clone();
		currentViewDirection.setY(0);
		if (currentViewDirection.length() == 0.0) {
			verbose("getVelocity", "Staring up?");
			return null;
		}
		return currentViewDirection.multiply(1.0/currentViewDirection.length());
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: KartDriver.%s: %s", method, message);
	}
}

