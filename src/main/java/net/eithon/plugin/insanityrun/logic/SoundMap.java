package net.eithon.plugin.insanityrun.logic;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

class SoundMap {
	private static HashMap<Material, SoundInfo> soundMap;
	
	static {
		soundMap = new HashMap<Material, SoundInfo>();
		soundMap.put(Material.GOLD_BLOCK, new SoundInfo(Sound.ORB_PICKUP));
		soundMap.put(Material.DIAMOND, new SoundInfo(Sound.EXPLODE));
		soundMap.put(Material.SAND, new SoundInfo(Sound.FIZZ));
		soundMap.put(Material.GRAVEL, new SoundInfo(Sound.FIZZ));
		soundMap.put(Material.EMERALD_BLOCK, new SoundInfo(Sound.GLASS));
		soundMap.put(Material.LAPIS_BLOCK, new SoundInfo(Sound.BURP));
		soundMap.put(Material.COAL_BLOCK, new SoundInfo(Sound.ANVIL_LAND));
		soundMap.put(Material.OBSIDIAN, new SoundInfo(Sound.ANVIL_LAND));
		soundMap.put(Material.PUMPKIN, new SoundInfo(Sound.ENDERMAN_TELEPORT));
		soundMap.put(Material.SPONGE, new SoundInfo(Sound.SHOOT_ARROW));
		soundMap.put(Material.GLOWSTONE, new SoundInfo(Sound.NOTE_PLING));
		soundMap.put(Material.WATER, new SoundInfo(Sound.SPLASH));
		soundMap.put(Material.STATIONARY_WATER, new SoundInfo(Sound.SPLASH));
		soundMap.put(Material.LAVA, new SoundInfo(Sound.LAVA));
		soundMap.put(Material.STATIONARY_LAVA, new SoundInfo(Sound.LAVA));
	}
	
	public static void playSound(Material material, Location location) {
		SoundInfo soundInfo = soundMap.get(material);
		if (soundInfo == null) return;
		soundInfo.play(location);
	}
}
