package net.eithon.plugin.insanityrun.racer;

import net.eithon.plugin.insanityrun.logic.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeftArenaEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Player _player;
	private Arena _arena;

	public PlayerLeftArenaEvent(Player player, Arena arena) {
		this._player = player;
		this._arena = arena;
	}

	public static HandlerList getHandlerList() {
		return handlers;
		}


	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Player getPlayer() { return this._player; }
	
	public Arena getArena() { return this._arena; }
}
