package net.eithon.plugin.insanityrun.racer;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.ITemporaryEffect;
import net.eithon.library.time.TemporaryEffect;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class TemporaryEffects {
	public static TemporaryEffect pumpkinHelmet;
	public static TemporaryEffect freeze;
	private static EithonPlugin eithonPlugin;
	
	public static void initialize(EithonPlugin plugin) {
		eithonPlugin = plugin;
		pumpkinHelmet = new TemporaryEffect(plugin, new ITemporaryEffect() {
			@Override
			public Object Do(Object... args) {
				verbose("pumpkinHelmet Do", "Enter");
				Racer runner = (Racer) args[0];
				Player player = runner.getPlayer();
				player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN, 1, (short) 14));
				verbose("pumpkinHelmet Do", "Leave");
				return null;
			}
			@Override
			public void Undo(Object doReturnValue, Object... args) {
				verbose("pumpkinHelmet Undo", "Enter");
				Racer runner = (Racer) args[0];
				runner.resetHelmet();
				verbose("pumpkinHelmet Undo", "Leave");
			}
		});
		freeze = new TemporaryEffect(plugin, new ITemporaryEffect() {
			@Override
			public Object Do(Object... args) {
				verbose("freeze Do", "Enter");
				Racer runner = (Racer) args[0];
				runner.setIsFrozen(true);
				verbose("freeze Do", "Leave");
				return null;
			}
			@Override
			public void Undo(Object doReturnValue, Object... args) {
				verbose("freeze Undo", "Enter");
				Racer runner = (Racer) args[0];
				runner.setIsFrozen(false);
				verbose("freeze Undo", "Leave");
			}
		});
	}
	
	private static void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "TemporaryEffects.%s: %s", method, message);
	}
}
