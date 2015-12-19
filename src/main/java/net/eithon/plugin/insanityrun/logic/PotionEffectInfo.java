package net.eithon.plugin.insanityrun.logic;

import net.eithon.plugin.insanityrun.Config;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

class PotionEffectInfo {
	private PotionEffectType _potionEffectType;
	private int _duration;
	private int _amplification;
	
	public PotionEffectInfo(PotionEffectType potionEffectType, int duration, int amplification) {
		this._potionEffectType = potionEffectType;
		this._duration = duration;
		this._amplification = amplification;
	}

	public PotionEffectInfo(PotionEffectType potionEffectType) {
		this(potionEffectType, 2, 2);
	}

	public void addPotionEffect(Player player) {
		player.addPotionEffect(new PotionEffect(this._potionEffectType, Config.V.potionDuration*this._duration, this._amplification));
	}
}
