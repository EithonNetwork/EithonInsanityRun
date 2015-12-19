package net.eithon.plugin.insanityrun.logic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

class ScoreDisplay {
	private Scoreboard _board;
	public Objective _objective;
	private Score _coinScore;
	private Score _timeScore;
	
	static void initialize() {
	}
	
	public ScoreDisplay(final Player player) {
		final String playerName = player.getName();
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		this._board = manager.getNewScoreboard();
		player.setScoreboard(this._board);
		// Objective
		this._objective = this._board.registerNewObjective("ignore", "dummy");
		this._objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this._objective.setDisplayName(getDisplayName(playerName));
		// Coin score
		this._coinScore = this._objective.getScore(getCoinScore());
		this._coinScore.setScore(0);
		// Time score
		this._timeScore = this._objective.getScore(getTimeScore());
		this._timeScore.setScore(0);
	}

	private static String getTimeScore() {
		return ChatColor.LIGHT_PURPLE + "Time:";
	}

	private static String getCoinScore() {
		return ChatColor.YELLOW + "Coins:";
	}

	private static String getDisplayName(final String playerName) {
		return ChatColor.WHITE + "Insanity Run : " + ChatColor.GREEN + playerName;
	}

	public void reset() {
		this._board.resetScores(getCoinScore());
		this._board.resetScores(getTimeScore());
	}

	public void setTimeScore(int score) {
		this._timeScore.setScore(score);
		
	}

	public void setCoinScore(int score) {
		this._coinScore.setScore(score);
	}
}
