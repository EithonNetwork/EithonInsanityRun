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
import org.bukkit.util.Vector;

public class KartDriver extends Racer {
	private double _speed;
	private Minecart _minecart;

	public KartDriver(Player player, Arena arena)
	{
		super(player, arena);
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
		super.leaveGame();
	}

	public void doRepeatedly() {
		super.doRepeatedly();
		this._speed += Config.V.speedIncrease;
	}

	public boolean playerMoved(final EithonPlugin plugin, final Location currentLocation) {
		boolean playerLeftGame = super.playerMoved(plugin, currentLocation);
		if (willCollide()) {
			Config.M.failRestart.sendMessage(this._player);
			teleportToSpawn();
		}
		return playerLeftGame;
	}

	@Override
	protected void updateLocation(Location location) {
		super.updateLocation(location);
		Vector velocity = getVelocity();
		if (velocity == null) return;
		getMovingObject().setVelocity(velocity);
	}

	@Override
	public void teleportToSpawn() {
		super.teleportToSpawn();
		this._speed = Config.V.startSpeed;
		Location spawnLocation = this._arena.getSpawnLocation();
		getMovingObject().setVelocity(spawnLocation.getDirection().multiply(Config.V.startSpeed));
	}
	
	@Override
	protected Vector getVelocity() {
		try {
			final Vector currentViewDirection = getMovingObject().getLocation().getDirection().clone();
				currentViewDirection.setY(0);
				if (currentViewDirection.length() == 0.0) return null;
				currentViewDirection.multiply(1/currentViewDirection.length());
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
	
	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: KartDriver.%s: %s", method, message);
	}
}

