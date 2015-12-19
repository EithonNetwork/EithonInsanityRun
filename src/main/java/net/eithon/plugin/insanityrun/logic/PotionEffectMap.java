package net.eithon.plugin.insanityrun.logic;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

class PotionEffectMap {
	private static HashMap<Material, ArrayList<PotionEffectInfo>> potionEffectMap;
	
	static {
		potionEffectMap = new HashMap<Material, ArrayList<PotionEffectInfo>>();
		ArrayList<PotionEffectInfo> potionEffects = new ArrayList<PotionEffectInfo>();
		// SAND & GRAVEL = SLOW
		potionEffects.add(new PotionEffectInfo(PotionEffectType.SLOW));
		potionEffectMap.put(Material.SAND, potionEffects);
		potionEffectMap.put(Material.GRAVEL, potionEffects);
		// EMERALD = SPEED
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.SPEED));
		potionEffectMap.put(Material.EMERALD_BLOCK, potionEffects);
		// LAPIS = CONFUSION
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.CONFUSION, 5, 0));
		potionEffectMap.put(Material.LAPIS_BLOCK, potionEffects);
		// COAL = BLINDNESS
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.BLINDNESS));
		potionEffectMap.put(Material.COAL_BLOCK, potionEffects);
		// OBSIDIAN = BLINDNESS & NIGHTVISION
		potionEffects = new ArrayList<PotionEffectInfo>();
		potionEffects.add(new PotionEffectInfo(PotionEffectType.BLINDNESS));
		potionEffects.add(new PotionEffectInfo(PotionEffectType.NIGHT_VISION));
		potionEffectMap.put(Material.OBSIDIAN, potionEffects);
	}
	
	public static void addPotionEffects(final Material material, final Player player) {
		ArrayList<PotionEffectInfo> potionEffects = potionEffectMap.get(material);
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
}
