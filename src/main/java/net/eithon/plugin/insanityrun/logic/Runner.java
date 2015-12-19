package net.eithon.plugin.insanityrun.logic;

import java.awt.Point;
import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.plugin.insanityrun.Config;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

class Runner implements IUuidAndName {
	private Arena _arena;
	private Player _player;
	private ScoreKeeper _scoreKeeper;
	private boolean _isInGame;
	private boolean _isFrozen;
	private Location _rememberLocation;
	private GameMode _rememberGameMode;
	private ItemStack _rememberHelmetWorn;
	private int _idleCount;
	private Block _lastBlock;
	private Location _lastLocation;
	private Location _lastCheckPoint;
	private HashMap<Point, Integer> _goldBlocks;
	private boolean _stopTeleport;

	Runner(Player player, Arena arena)
	{
		this._player = player;
		this._scoreKeeper = new ScoreKeeper(player);

		//player.sendMessage(ChatColor.GREEN + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".readyToPlay"));

		// Construct new player object

		this._rememberLocation = player.getLocation();
		this._rememberGameMode = player.getGameMode();
		this._rememberHelmetWorn = player.getInventory().getHelmet();
		if (this._rememberHelmetWorn == null) {
			this._rememberHelmetWorn = new ItemStack(Material.AIR, 1, (short) 14);
		}
		player.setGameMode(GameMode.SURVIVAL);
		this._arena = arena;
		teleportToSpawn();
	}

	public void maybeLeaveGameBecauseOfTeleport() {
		if (!this._stopTeleport) return;
		leaveGame(false, false);
	}

	public Arena getArena() { return this._arena; }
	public Player getPlayer() { return this._player; }
	public boolean isInGame() { return this._isInGame; }
	public boolean isFrozen() { return this._isFrozen; }
	public boolean hasBeenIdleTooLong() { return this._idleCount > Config.V.idleKickTime; }

	@Override
	public String getName()	{ return this._player.getName(); }

	public String toString() { return String.format("%s", this.getName());	}

	@Override
	public UUID getUniqueId() { return this._player.getUniqueId();	}

	public void leaveGame() { leaveGame(false, true); }

	public void leaveGameWithRefund() { leaveGame(true, true);	}

	private void leaveGame(boolean refund, boolean teleportToStart) {	
		this._isInGame = false;
		this._scoreKeeper.reset();
		final Player player = this._player;
		if (player == null) return;	
		PotionEffectMap.removePotionEffects(player);
		player.setFireTicks(0);
		player.getInventory().setHelmet(this._rememberHelmetWorn);
		player.setGameMode(this._rememberGameMode);
		if (refund) refundMoney();
		if (teleportToStart) teleportToStart();
		this._arena.runnerLeft(this);
	}

	public void doEverySecond() {
		if (this._isFrozen || !this._isInGame) {
			this._idleCount = 0;
			if (!this.isInGame()) return;
		}
		updateIdleCount();
		this._scoreKeeper.updateTimeScore();
	}

	public boolean movedOneBlock(Plugin plugin) {
		if (!this._isInGame) return false;
		boolean runnerLeftGame = false;
		Location currentLocation = updateLocation();
		currentLocation.setY(currentLocation.getY() - 2);
		Block blockUnderFeet = currentLocation.getBlock();
		Material blockMaterial = blockUnderFeet.getType();
		boolean playSound = true;
		if (blockMaterial == Material.GOLD_BLOCK) {
			playSound = maybeGetCoin(blockUnderFeet);
		}
		if (playSound) SoundMap.playSound(blockMaterial, this._lastLocation);
		PotionEffectMap.addPotionEffects(blockMaterial, this._player);
		// Player effects when walking on blocks
		switch(blockMaterial) {
		case DIAMOND_BLOCK: // Jump
			this._player.setVelocity(this._player.getVelocity().setY(1.5));
			break;
		case PUMPKIN: // Pumpkin helmet
			setPumpkinHelmet(plugin);
			break;
		case ICE: // freeze player
			freezePlayer(plugin);
			break;
		case SPONGE: // Bounce
			this._player.setVelocity(this._lastLocation.getDirection().multiply(-1));
			break;
		case GLOWSTONE: // checkpoint
			this._lastCheckPoint = this._lastLocation;
			break;
		case REDSTONE_BLOCK: // finish line
			endLevelOrGame(plugin);
			break;
		case WATER:
		case STATIONARY_WATER:
		case LAVA:
		case STATIONARY_LAVA:
			runnerLeftGame = restart(plugin);
			break;
		default:
			break;
		}
		return runnerLeftGame;
	}

	public void teleportToLastLocation() {
		safeTeleport(this._lastLocation);
	}

	// Refund money if kicked
	private void refundMoney() {
		/*
		if (InsanityRun.useVault && InsanityRun.plugin.getConfig().getInt(arenaName + ".charge") > 0) {
			EconomyResponse res = InsanityRun.economy.depositPlayer(playerName, InsanityRun.plugin.getConfig().getInt(arenaName + ".charge"));
			if(!res.transactionSuccess()) {
				Bukkit.getConsoleSender().sendMessage(String.format(InsanityRun.gameName + " Vault Deposit - An error occured: %s", res.errorMessage));
			}
		}	
		 */	
	}
	private void updateIdleCount() {
		Block currentBlock = this._player.getLocation().getBlock();
		if (lastBlockIsSame(currentBlock)) {
			this._idleCount ++;
		} else {
			this._idleCount = 0;
			this._lastBlock = currentBlock;
		}
	}

	private boolean lastBlockIsSame(Block currentBlock) {
		return (this._lastBlock.getX() == currentBlock.getX()) && (this._lastBlock.getZ() == currentBlock.getZ());
	}

	private Location updateLocation() {
		this._lastLocation = this._player.getLocation();
		this._lastBlock = this._lastLocation.getBlock();
		return this._lastLocation;
	}

	private void teleportToSpawn() {
		this._isInGame = true;
		this._isFrozen = false;
		this._scoreKeeper.resetCoins();
		this._idleCount = 0;
		this._stopTeleport = true;
		this._goldBlocks = new HashMap<Point, Integer>();
		PotionEffectMap.removePotionEffects(this._player);
		this._player.setFireTicks(0);
		safeTeleport(this._arena.getSpawnLocation());
	}

	private void teleportToLastCheckPoint() {
		this._isInGame = true;
		this._idleCount = 0;
		PotionEffectMap.removePotionEffects(this._player);
		this._player.setFireTicks(0);
		Location location = this._lastCheckPoint;
		if (location == null) location = this._arena.getSpawnLocation();
		safeTeleport(location);
	}

	private void teleportToStart() {
		safeTeleport(this._rememberLocation);
	}

	private void safeTeleport(Location location) {
		try {
			this._stopTeleport = false;
			this._player.teleport(this._rememberLocation);
			updateLocation();
		} finally {
			this._stopTeleport = true;
		}
	}

	private void freezePlayer(Plugin plugin) {
		final Runner runner = this;
		this._isFrozen = true;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				runner._isFrozen = false;
			}
		}, 20 * 2); // 20 ticks per second x 'gracetime' seconds			
	}

	private void setPumpkinHelmet(Plugin plugin) {
		final Player player = this._player;
		player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN, 1, (short) 14));
		final Runner runner = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				player.getInventory().setHelmet(runner._rememberHelmetWorn);
			}
		}, 20 * 2); // 20 ticks per second x 'gracetime' seconds
	}

	private boolean maybeGetCoin(Block block) {
		Point point = new Point(block.getX(), block.getY());
		if (this._goldBlocks.containsKey(point)) return false;
		this._goldBlocks.put(point, 1);
		this._scoreKeeper.addCoinScore(1);
		return true;
	}

	private boolean restart(Plugin plugin) {
		this._isInGame = false;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (Config.V.waterRestartsRun) {
					teleportToSpawn();
				}
				else if (Config.V.useCheckpoints) {
					teleportToLastCheckPoint();
				}
				else {
					leaveGame();
				}
			}
		}, 20 * 1); // 20 ticks per second x 1 seconds
		// Will leave game?
		return (!Config.V.waterRestartsRun && !Config.V.useCheckpoints);
	}

	// Player ran over Redstone. End the level and start next or end game
	private void endLevelOrGame(Plugin plugin) {
		this._isInGame = false;
		PotionEffectMap.removePotionEffects(this._player);
		WinnerFirework.doIt(this._lastLocation);
		if (Config.V.broadcastWins) {
			//InsanityRun.plugin.getServer().broadcastMessage(ChatColor.GOLD + "[Insanity Run]" + String.format(InsanityRun.broadcastWinsText, colourise("&3"+playerName+"&6"), colourise("&9"+arenaName+"&6"), colourise("&a"+currentPlayerObject.getCoins()+"&6"), InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".gameCurrency"), colourise("&f"+formatIntoHHMMSS(runTime))));
		}
		else {
			//player.sendMessage(ChatColor.GOLD + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".gameOver") + " " + colourise("&a"+currentPlayerObject.getCoins()+"&6") + " " + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".gameCurrency") + " Time: " + colourise("&f"+formatIntoHHMMSS(runTime)));
		}

		/*
		if (InsanityRun.useVault && InsanityRun.plugin.getConfig().getInt(arenaName + ".pay") > 0) {
			EconomyResponse res = InsanityRun.economy.depositPlayer(player.getName(), InsanityRun.plugin.getConfig().getInt(arenaName + ".pay"));
			if(res.transactionSuccess()) {
				player.sendMessage(ChatColor.GOLD + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".vaultAward") + " " + InsanityRun.plugin.getConfig().getInt(arenaName + ".pay") + " " + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".payCurrency"));
			} else {
				player.sendMessage(String.format("An error occured: %s", res.errorMessage));
			}
		}
		 */
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				// Check for linked arena
				/*
				if (InsanityRun.plugin.getConfig().getString(arenaName + ".link")!=null) {
					if ((!InsanityRun.useVault) || (InsanityRun.useVault && canAfford(currentPlayerObject,InsanityRun.plugin.getConfig().getString(arenaName + ".link")))) {
						// Remove player from this arena
						int playCount;
						playCount = InsanityRun.playersInThisArena.get(arenaName);
						playCount--;
						InsanityRun.playersInThisArena.put(arenaName, playCount);
						GameManager.updateJoinSign(arenaName);
						currentPlayerObject.setCoins(0);
						// Reset Scoreboard
						Score time = InsanityRun.objective.getScore(ChatColor.LIGHT_PURPLE + "Time:");
						Score score = InsanityRun.objective.getScore(ChatColor.YELLOW + "Coins:");
						time.setScore(0);
						score.setScore(0);
						GameManager.teleportToSpawn(player, InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						GameManager.updatePlayerXYZ(player);
						currentPlayerObject.setLastCheckpoint(player.getLocation());
						currentPlayerObject.setInArena(InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						currentPlayerObject.setFrozen(false);
						currentPlayerObject.setLastMovedTime(System.currentTimeMillis());
						currentPlayerObject.setStartRaceTime(System.currentTimeMillis());
						currentPlayerObject.setInGame(true);
						if (InsanityRun.playersInThisArena.get(InsanityRun.plugin.getConfig().getString(arenaName + ".link")) == null) {
							InsanityRun.playersInThisArena.put(InsanityRun.plugin.getConfig().getString(arenaName + ".link"), 0);
						}
						playCount = InsanityRun.playersInThisArena.get(InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						playCount++;
						InsanityRun.playersInThisArena.put(InsanityRun.plugin.getConfig().getString(arenaName + ".link"), playCount);
						GameManager.updateJoinSign(InsanityRun.plugin.getConfig().getString(arenaName + ".link"));
						return;
					}

				}
				 */
				// Check if player gets sent back to JOIN sign
				if (Config.V.telePortAfterEnd) {
					teleportToStart();
				}
				leaveGame();
			}
		}, 20 * 5); // 20 ticks per second x 5 seconds
	}

	/*
	// Can player afford to play next arena?
	@SuppressWarnings("deprecation")
	private static boolean canAfford(iPlayer currentPlayerObject, String arenaName) {
		Player player = InsanityRun.plugin.getServer().getPlayer(currentPlayerObject.getPlayerName());
		String playerName = currentPlayerObject.getPlayerName();
		// Does player have enough money to play?
		if (InsanityRun.useVault) {
			if (InsanityRun.economy.getBalance(player.getName()) < InsanityRun.plugin.getConfig().getInt(arenaName + ".charge")) {
				player.sendMessage(ChatColor.RED + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".notEnoughMoneyText") + InsanityRun.plugin.getConfig().getInt(arenaName + ".charge") + " " + InsanityRun.plugin.getConfig().getString(InsanityRun.useLanguage + ".payCurrency"));
				return false;
			}
			else {
				// Withdraw money
				EconomyResponse r = InsanityRun.economy.withdrawPlayer(playerName, InsanityRun.plugin.getConfig().getInt(arenaName + ".charge"));
				if(r.transactionSuccess()) {
					return true;
				} else {
					player.sendMessage(String.format("An error occured: %s", r.errorMessage));
					return false;
				}
			}
		}
		return false;
	}
	 */
}
