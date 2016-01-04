package net.eithon.plugin.insanityrun.racer;

import org.bukkit.Location;
import org.bukkit.Sound;

class SoundInfo {
	private Sound _sound;
	private int _volume;
	private int _pitch;
	
	public SoundInfo(Sound sound, int volume, int pitch) {
		this._sound = sound;
		this._volume = volume;
		this._pitch = pitch;
	}

	public SoundInfo(Sound sound) {
		this(sound, 1, 1);
	}

	public void play(Location location) {
		location.getWorld().playSound(location, this._sound, this._volume, this._pitch);
	}
}
