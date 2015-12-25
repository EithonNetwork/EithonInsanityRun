package net.eithon.plugin.insanityrun.logic;

import net.eithon.library.time.ITemporaryEffect;
import net.eithon.library.time.TemporaryEffect;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

class TemporaryEffects {
	public static TemporaryEffect pumpkinHelmet;
	public static TemporaryEffect freeze;
	
	public static void initialize(Plugin plugin) {
		pumpkinHelmet = new TemporaryEffect(plugin, new ITemporaryEffect() {
			@Override
			public Object Do(Object... args) {
				Runner runner = (Runner) args[0];
				Player player = runner.getPlayer();
				player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN, 1, (short) 14));
				return null;
			}
			@Override
			public void Undo(Object doReturnValue, Object... args) {
				Runner runner = (Runner) args[0];
				runner.resetHelmet();
			}
		});
		freeze = new TemporaryEffect(plugin, new ITemporaryEffect() {
			@Override
			public Object Do(Object... args) {
				Runner runner = (Runner) args[0];
				runner.setIsFrozen(true);
				return null;
			}
			@Override
			public void Undo(Object doReturnValue, Object... args) {
				Runner runner = (Runner) args[0];
				runner.setIsFrozen(false);
			}
		});
	}
}
