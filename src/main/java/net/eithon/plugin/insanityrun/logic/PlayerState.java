package net.eithon.plugin.insanityrun.logic;

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
	
	public PlayerState(Player player) {
		this._player = player;
	}
	
	public void save() {
		this._location = this._player.getLocation();
		this._gameMode = this._player.getGameMode();
		this._helmetWorn = this._player.getInventory().getHelmet();
		if (this._helmetWorn == null) {
			this._helmetWorn = new ItemStack(Material.AIR, 1, (short) 14);
		}
	}
	
	public void restore(boolean restoreLocation) {
		restoreGameMode();
		restoreHelmetWorn();
		if (restoreLocation) restoreLocation();
	}

	public void restoreLocation() {
		this._player.teleport(this._location);
	}

	public void restoreGameMode() {
		this._player.setGameMode(this._gameMode);
	}

	public void restoreHelmetWorn() {
		this._player.getInventory().setHelmet(this._helmetWorn);
	}
}
