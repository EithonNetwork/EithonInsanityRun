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
	private Vector _velocity;

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
		return this._player;
	}

	protected void leaveGame(boolean refund, boolean teleportToStart) {
		super.leaveGame(refund, teleportToStart);
	}

	public void doRepeatedly() {
		super.doRepeatedly();
		this._velocity = this._velocity.clone().normalize().multiply(this._velocity.length()+Config.V.speedIncrease);
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
		updateVelocity();
	}

	@Override
	public void teleportToSpawn() {
		super.teleportToSpawn();
		this._player.setWalkSpeed(0.0F);
		this._player.setFlySpeed(1.0F);
		this._player.setFlying(true);
		this._player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		this._velocity = this._arena.getSpawnLocation().getDirection().clone().setY(0).multiply(Config.V.startSpeed);
		updateVelocity();
	}
	
	private void updateVelocity() {
		try {
			Vector currentViewDirection = getCurrentDirectionWithZeroYComponent();
			final Vector deltaVelocity = currentViewDirection.multiply(Config.V.velocityChangeSpeed);
			final Vector currentVelocity = this._velocity;
			this._velocity = this._velocity.add(deltaVelocity);
			getMovingObject().setVelocity(this._velocity);
		} catch (Exception e) {
			Logger.libraryError("EithonRace.Runner.getVelocity() failed.\n%s", 
					e.getMessage());
			e.printStackTrace();
		}
	}

	public Vector getCurrentDirectionWithZeroYComponent() {
		Vector currentViewDirection = this._player.getLocation().getDirection().clone();
		return currentViewDirection.setY(0).normalize();
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: KartDriver.%s: %s", method, message);
	}
}

