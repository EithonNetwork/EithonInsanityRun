package net.eithon.plugin.insanityrun.racer;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerState {
	private Player _player;
	private Location _location;
	private GameMode _gameMode;
	private ItemStack _helmetWorn;
	private float _walkSpeed;
	private float _flySpeed;
	private boolean _isFlying;
	
	public PlayerState(Player player) {
		this._player = player;
	}
	
	public void save() {
		this._location = this._player.getLocation();
		this._gameMode = this._player.getGameMode();
		this._helmetWorn = this._player.getInventory().getHelmet();
		this._isFlying = this._player.isFlying();
		this._walkSpeed = this._player.getWalkSpeed();
		this._flySpeed = this._player.getFlySpeed();
		if (this._helmetWorn == null) {
			this._helmetWorn = new ItemStack(Material.AIR, 1, (short) 14);
		}
	}
	
	public void restore(boolean restoreLocation) {
		restoreHelmetWorn();
		if (!this._player.getGameMode().equals(this._gameMode)) this._player.setGameMode(this._gameMode);
		if (this._player.getWalkSpeed() != this._walkSpeed) this._player.setWalkSpeed(this._walkSpeed);
		if (this._player.getFlySpeed() != this._flySpeed) this._player.setFlySpeed(this._flySpeed);
		if (this._player.isFlying() != this._isFlying) this._player.setFlying(this._isFlying);
		if (restoreLocation) restoreLocation();
	}

	public void restoreLocation() {
		this._player.teleport(this._location);
	}

	public void restoreHelmetWorn() {
		this._player.getInventory().setHelmet(this._helmetWorn);
	}
}
