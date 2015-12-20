package net.eithon.plugin.insanityrun;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {
		public static boolean useVault;
		public static boolean broadcastWins;
		public static int idleKickTime;
		public static float blockJumpHeight;
		public static int potionDuration;
		public static boolean waterRestartsRun;
		public static boolean useCheckpoints;
		public static boolean telePortAfterEnd;
		public static int maxHeightOverBlock;

		static void load(Configuration config) {
			useVault = config.getBoolean("UseVault", false);
			idleKickTime = config.getInt("IdleKickTime", 2);
			broadcastWins = config.getBoolean("BroadcastWins", false);
			potionDuration =  config.getInt("PotionDuration", 2);
			waterRestartsRun = config.getBoolean("WaterRestartsRun", true);
			useCheckpoints = config.getBoolean("UseCheckpoints", true);
			telePortAfterEnd = config.getBoolean("TelePortAfterEnd", true);
			maxHeightOverBlock =  config.getInt("MaxHeightOverBlock", 2);
		}

	}
	public static class C {

		static void load(Configuration config) {
		}

	}
	public static class M {
		public static ConfigurableMessage playerFinished;
		public static ConfigurableMessage arenaAdded;
		public static ConfigurableMessage arenaRemoved;
		public static ConfigurableMessage unknownArena;
		public static ConfigurableMessage arenasAreLinked;
		public static ConfigurableMessage gotoArena;
		public static ConfigurableMessage arenaInfo;
		public static ConfigurableMessage joinArena;

		static void load(Configuration config) {
			playerFinished = config.getConfigurableMessage(
					"messages.PlayerFinished", 5, 
					"%s finished %s with %s %s. Time: %s");
			arenaAdded = config.getConfigurableMessage(
					"messages.ArenaAdded", 1, 
					"Arena %s has been added.");
			arenaRemoved = config.getConfigurableMessage(
					"messages.ArenaRemoved", 1, 
					"Arena %s has been removed.");
			unknownArena = config.getConfigurableMessage(
					"messages.UnknownArena", 1, 
					"Arena %s could not be found.");
			arenasAreLinked = config.getConfigurableMessage(
					"messages.ArenasAreLinked", 2, 
					"Arena %s is now linked to arena %s.");
			gotoArena = config.getConfigurableMessage(
					"messages.GotoArena", 1, 
					"You have been teleported to Arena %.");
			joinArena = config.getConfigurableMessage(
					"messages.JoinArena", 1, 
					"You have joined Arena %.");
			arenaInfo = config.getConfigurableMessage(
					"messages.ArenaInfo", 1, 
					"%s");
		}		
	}

}
