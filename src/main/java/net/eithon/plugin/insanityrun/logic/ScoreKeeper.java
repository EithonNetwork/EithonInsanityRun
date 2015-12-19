package net.eithon.plugin.insanityrun.logic;

import org.bukkit.entity.Player;

public class ScoreKeeper {
	private int _coins;
	private long _startTime;
	private ScoreDisplay _scoreDisplay;
	
	public ScoreKeeper(Player player) {
		this._coins = 0;
		this._startTime = System.currentTimeMillis();
		this._scoreDisplay = new ScoreDisplay(player);
	}

	public void reset() {
		this._coins = 0;
		this._startTime = System.currentTimeMillis();
		this._scoreDisplay.reset();
	}

	public void updateTimeScore() {
		final long currentTimeMillis = System.currentTimeMillis();
		int runTimeSeconds = (int) Math.round((currentTimeMillis - this._startTime)/1000.0);
		this._scoreDisplay.setTimeScore(runTimeSeconds);
	}

	public void addCoinScore(int coins) {
		this._coins += coins;
		this._scoreDisplay.setCoinScore(this._coins);		
	}

	public void resetCoins() {
		this._coins = 0;
		this._scoreDisplay.setCoinScore(this._coins);		
	}
}
