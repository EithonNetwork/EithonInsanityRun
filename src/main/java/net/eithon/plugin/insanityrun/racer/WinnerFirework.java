package net.eithon.plugin.insanityrun.racer;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

class WinnerFirework {
	public static void doIt(Location location) {
		Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		Random r = new Random();   
		Type type = Type.BALL;       
		Color c1 = Color.GREEN;
		Color c2 = Color.YELLOW;
		FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();
		FireworkMeta meta = fw.getFireworkMeta();
		meta.addEffect(effect);
		meta.setPower(1);
		fw.setFireworkMeta(meta);
	}
}
