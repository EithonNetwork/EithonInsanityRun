package net.eithon.plugin.insanityrun.racer;

import net.eithon.plugin.insanityrun.Config;

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
	
	public ScoreDisplay(final String arenaName, final Player player) {
		final String playerName = player.getName();
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		this._board = manager.getNewScoreboard();
		player.setScoreboard(this._board);
		// Objective
		this._objective = this._board.registerNewObjective("ignore", "dummy");
		this._objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this._objective.setDisplayName(Config.M.scoreTitle.getMessageWithColorCoding(arenaName, playerName));
		// Coin score
		this._coinScore = this._objective.getScore(getCoinScoreLabel());
		this._coinScore.setScore(0);
		// Time score
		this._timeScore = this._objective.getScore(getTimeScoreLabel());
		this._timeScore.setScore(0);
	}

	private String getTimeScoreLabel() {
		return Config.M.scoreTime.getMessageWithColorCoding();
	}

	private String getCoinScoreLabel() {
		return Config.M.scoreCoins.getMessageWithColorCoding();
	}

	public void reset() {
		this._board.resetScores(getCoinScoreLabel());
		this._board.resetScores(getTimeScoreLabel());
	}

	public void setTimeScore(int score) {
		this._timeScore.setScore(score);
		
	}

	public void setCoinScore(int score) {
		this._coinScore.setScore(score);
	}

	public void disable() {
		this._board.clearSlot(DisplaySlot.SIDEBAR);
	}
}
