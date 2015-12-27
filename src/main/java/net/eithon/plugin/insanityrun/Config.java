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
		public static long idleKickTimeSeconds;
		public static int potionDuration;
		public static boolean waterRestartsRun;
		public static boolean useCheckpoints;
		public static boolean telePortAfterEnd;
		public static long pumpkinHelmetSeconds;
		public static long freezeSeconds;
		public static long restartAfterTicks;
		public static int maxTotalDepth;
		public static double maxAirDepth;
		public static double defaultArenaPrice;
		public static double defaultArenaReward;

		static void load(Configuration config) {
			useVault = config.getBoolean("UseVault", false);
			idleKickTimeSeconds = config.getSeconds("IdleKickTime", 4);
			broadcastWins = config.getBoolean("BroadcastWins", false);
			potionDuration =  config.getInt("PotionDuration", 2);
			waterRestartsRun = config.getBoolean("WaterRestartsRun", true);
			useCheckpoints = config.getBoolean("UseCheckpoints", true);
			telePortAfterEnd = config.getBoolean("TelePortAfterEnd", true);
			pumpkinHelmetSeconds = config.getSeconds("PumpkinHelmetTimeSpan", 2);
			freezeSeconds = config.getSeconds("FreezeTimeSpan", 2);
			restartAfterTicks = config.getTicks("RestartTimeSpan", 10);
			maxTotalDepth =  config.getInt("MaxTotalDepth", 2);
			double maxAirDepth=  config.getDouble("MaxAirDepth", 0.5);
			defaultArenaPrice =  config.getDouble("DefaultArenaPrice", 0);
			defaultArenaReward =  config.getDouble("DefaultArenaReward", 0);
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
		public static ConfigurableMessage leftArena;
		public static ConfigurableMessage reachedCheckPoint;
		public static ConfigurableMessage failRestart;
		public static ConfigurableMessage idleKick;
		public static ConfigurableMessage teleportKick;
		public static ConfigurableMessage broadcastSuccess;
		public static ConfigurableMessage withdrawFailed;
		public static ConfigurableMessage withdrawSucceeded;
		public static ConfigurableMessage priceArena;
		public static ConfigurableMessage rewardArena;
		public static ConfigurableMessage rewardFailed;
		public static ConfigurableMessage rewarded;
		public static ConfigurableMessage effectSlowActivated;

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
			arenaInfo = config.getConfigurableMessage(
					"messages.ArenaInfo", 1, 
					"%s");
			joinArena = config.getConfigurableMessage(
					"messages.JoinArena", 1, 
					"You have joined Arena %.");
			leftArena = config.getConfigurableMessage(
					"messages.LeftArena", 1, 
					"You have left Arena %.");
			reachedCheckPoint = config.getConfigurableMessage(
					"messages.ReachedCheckPoint", 0, 
					"You have reached a check point.");
			failRestart = config.getConfigurableMessage(
					"messages.FailRestart", 0, 
					"You failed, try again.");
			idleKick = config.getConfigurableMessage(
					"messages.IdleKick", 0, 
					"You were idle too long.");
			teleportKick = config.getConfigurableMessage(
					"messages.TeleportKick", 0, 
					"You are not allowed to teleport in a game.");
			broadcastSuccess = config.getConfigurableMessage(
					"messages.BroadcastSuccess", 3, 
					"Player %s completed arena %s in %s seconds.");
			withdrawFailed = config.getConfigurableMessage(
					"messages.WithdrawFailed", 1, 
					"Could not withdraw %.2f from your account.");
			withdrawSucceeded = config.getConfigurableMessage(
					"messages.WithdrawSucceeded", 1, 
					"%.2f was withdrawn from your account.");
			priceArena = config.getConfigurableMessage(
					"messages.PriceArena", 2, 
					"The price for arena %s is now %.2f.");
			rewardArena = config.getConfigurableMessage(
					"messages.RewardArena", 2, 
					"The reward for arena %s is now %.2f.");
			rewardFailed = config.getConfigurableMessage(
					"messages.RewardFailed", 1, 
					"Failed to reward you %.2f for completing the arena.");
			rewarded = config.getConfigurableMessage(
					"messages.Rewarded", 1, 
					"You were rewarded %.2f for completing the arena.");
			effectSlowActivated = config.getConfigurableMessage(
					"messages.EffectSlowActivated", 0, 
					"SLOWNESS effect applied!");
		}		
	}

}
