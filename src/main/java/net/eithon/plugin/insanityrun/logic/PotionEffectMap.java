package net.eithon.plugin.insanityrun.logic;

import java.util.ArrayList;
import java.util.HashMap;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.insanityrun.logic.BlockUnderFeet.RunnerEffect;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

class PotionEffectMap {
	private static HashMap<RunnerEffect, ArrayList<PotionEffectInfo>> potionEffectMap;
	private static EithonPlugin eithonPlugin;
	
	static void initialize(EithonPlugin plugin) {
		eithonPlugin = plugin;
		potionEffectMap = new HashMap<RunnerEffect, ArrayList<PotionEffectInfo>>();
		ArrayList<PotionEffectInfo> potionEffects = new ArrayList<PotionEffectInfo>();
		// SLOW
		potionEffects.add(new PotionEffectInfo(PotionEffectType.SLOW));
		potionEffectMap.put(RunnerEffect.SLOW, potionEffects);
		// SPEED
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.SPEED));
		potionEffectMap.put(RunnerEffect.SPEED, potionEffects);
		// DRUNK
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.CONFUSION, 5, 0));
		potionEffectMap.put(RunnerEffect.DRUNK, potionEffects);
		// BLIND
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.BLINDNESS));
		potionEffectMap.put(RunnerEffect.BLIND, potionEffects);
		// OBSIDIAN = BLINDNESS & NIGHTVISION
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.BLINDNESS));
		potionEffects.add(new PotionEffectInfo(PotionEffectType.NIGHT_VISION));
		potionEffectMap.put(RunnerEffect.DARK, potionEffects);
	}
	
	public static void addPotionEffects(final RunnerEffect runnerEffect, final Player player) {
		verbose("addPotionEffects", "RunnerEffect: %s", runnerEffect.toString());
		ArrayList<PotionEffectInfo> potionEffects = potionEffectMap.get(runnerEffect);
		if (potionEffects == null) return;
		for (PotionEffectInfo potionEffectInfo : potionEffects) {
			potionEffectInfo.addPotionEffect(player);
		}
	}
	
	public static void removePotionEffects(final Player player) {
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	}
	
	private static void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "PotionEffectMap.%s: %s", method, message);
	}
}
