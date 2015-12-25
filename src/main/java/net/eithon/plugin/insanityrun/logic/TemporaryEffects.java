package net.eithon.plugin.insanityrun.logic;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
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
				verbose("pumpkinHelmet Do", "Enter");
				Runner runner = (Runner) args[0];
				Player player = runner.getPlayer();
				player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN, 1, (short) 14));
				verbose("pumpkinHelmet Do", "Leave");
				return null;
			}
			@Override
			public void Undo(Object doReturnValue, Object... args) {
				verbose("pumpkinHelmet Undo", "Enter");
				Runner runner = (Runner) args[0];
				runner.resetHelmet();
				verbose("pumpkinHelmet Undo", "Leave");
			}
		});
		freeze = new TemporaryEffect(plugin, new ITemporaryEffect() {
			@Override
			public Object Do(Object... args) {
				verbose("freeze Do", "Enter");
				Runner runner = (Runner) args[0];
				runner.setIsFrozen(true);
				verbose("freeze Do", "Leave");
				return null;
			}
			@Override
			public void Undo(Object doReturnValue, Object... args) {
				verbose("freeze Undo", "Enter");
				Runner runner = (Runner) args[0];
				runner.setIsFrozen(false);
				verbose("freeze Do", "Leave");
			}
		});
	}
	
	private static void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: TemporaryEffects.%s: %s", method, message);
	}
}
