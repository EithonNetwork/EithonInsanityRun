package net.eithon.plugin.insanityrun;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.move.EithonPlayerMoveHalfBlockEvent;
import net.eithon.plugin.insanityrun.logic.Controller;
import net.eithon.plugin.insanityrun.racer.PlayerLeftArenaEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventListener implements Listener {
	private EithonPlugin _eithonPlugin = null;
	private Controller _controller;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
	}	
	
	@EventHandler
	public void onEithonPlayerMoveHalfBlockEvent(EithonPlayerMoveHalfBlockEvent event) {
		final Player player = event.getPlayer();
		this._controller.playerMoved(player, event.getToLocation());
	}

	// Stop players teleporting
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event)
	{
		final Player player = event.getPlayer();
		this._controller.maybeLeaveGameBecauseOfTeleport(player, event.getFrom(), event.getTo());
	}

	// Stop players teleporting
	@EventHandler
	public void onPlayerLeftArenaEvent(PlayerLeftArenaEvent event)
	{
		final Player player = event.getPlayer();
		this._controller.playerLeftArena(player);
	}
	
	// Stop player going hungry
	@EventHandler(priority = EventPriority.MONITOR)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (this._controller.isInGame(player)) event.setCancelled(true);
		}
	}

	// Notice when they leave and remove them from the game data
	@EventHandler
	public void onLeave(PlayerQuitEvent event){
		Player player = event.getPlayer();
		this._controller.leaveGame(player);
	}

	// Stop all player damage, including fire from lava
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (this._controller.isInGame(player)) {
				event.setCancelled(true);
				event.getEntity().setFireTicks(0);
			}
		}
	}
}
