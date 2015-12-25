package net.eithon.plugin.insanityrun.logic;

import java.util.HashMap;

import net.eithon.plugin.insanityrun.logic.BlockUnderFeet.RunnerEffect;

import org.bukkit.Location;
import org.bukkit.Sound;

class SoundMap {
	private static HashMap<RunnerEffect, SoundInfo> soundMap;
	
	static {
		soundMap = new HashMap<RunnerEffect, SoundInfo>();
		soundMap.put(RunnerEffect.COIN, new SoundInfo(Sound.ORB_PICKUP));
		soundMap.put(RunnerEffect.JUMP, new SoundInfo(Sound.EXPLODE));
		soundMap.put(RunnerEffect.SLOW, new SoundInfo(Sound.FIZZ));
		soundMap.put(RunnerEffect.SPEED, new SoundInfo(Sound.GLASS));
		soundMap.put(RunnerEffect.DRUNK, new SoundInfo(Sound.BURP));
		soundMap.put(RunnerEffect.BLIND, new SoundInfo(Sound.ANVIL_LAND));
		soundMap.put(RunnerEffect.DARK, new SoundInfo(Sound.ANVIL_LAND));
		soundMap.put(RunnerEffect.PUMPKIN_HELMET, new SoundInfo(Sound.ENDERMAN_TELEPORT));
		soundMap.put(RunnerEffect.BOUNCE, new SoundInfo(Sound.SHOOT_ARROW));
		soundMap.put(RunnerEffect.CHECKPOINT, new SoundInfo(Sound.NOTE_PLING));
		soundMap.put(RunnerEffect.WATER, new SoundInfo(Sound.SPLASH));
		soundMap.put(RunnerEffect.LAVA, new SoundInfo(Sound.LAVA));
	}
	
	public static void playSound(RunnerEffect runnerEffect, Location location) {
		SoundInfo soundInfo = soundMap.get(runnerEffect);
		if (soundInfo == null) return;
		soundInfo.play(location);
	}
}
