package net.eithon.plugin.insanityrun.racer;

import java.util.HashMap;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.insanityrun.racer.BlockUnderFeet.RunnerEffect;

import org.bukkit.Location;
import org.bukkit.Sound;

class SoundMap {
	private static HashMap<RunnerEffect, SoundInfo> soundMap;
	private static EithonPlugin eithonPlugin;
	
	static void initialize(EithonPlugin plugin) {
		eithonPlugin = plugin;
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
		verbose("playSound", "RunnerEffect: %s", runnerEffect.toString());
		SoundInfo soundInfo = soundMap.get(runnerEffect);
		if (soundInfo == null) return;
		soundInfo.play(location);
	}
	
	private static void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "SoundMap.%s: %s", method, message);
	}
}
