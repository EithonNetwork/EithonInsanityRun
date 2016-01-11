package net.eithon.plugin.insanityrun.racer;

import org.bukkit.entity.Player;

public class ScoreKeeper {
	private int _coins;
	private long _startTime;
	private ScoreDisplay _scoreDisplay;
	
	public ScoreKeeper(String arenaName, Player player) {
		this._coins = 0;
		this._startTime = System.currentTimeMillis();
		this._scoreDisplay = new ScoreDisplay(arenaName, player);
	}

	public void reset() {
		this._coins = 0;
		this._startTime = System.currentTimeMillis();
		this._scoreDisplay.reset();
	}

	public long updateTimeScore() {
		return getRunTimeInMillisecondsAndUpdateScore();
	}

	public void updateTimeScore(long timeInMilliseconds) {
		this._scoreDisplay.setTimeScore((int) Math.floor(timeInMilliseconds/1000.0));
	}

	public void addCoinScore(int coins) {
		this._coins += coins;
		this._scoreDisplay.setCoinScore(this._coins);		
	}

	public void resetCoins() {
		this._coins = 0;
		this._scoreDisplay.setCoinScore(this._coins);		
	}

	public long getRunTimeInMillisecondsAndUpdateScore() { 
		final long runTime = System.currentTimeMillis()-this._startTime;
		updateTimeScore(runTime);
		return runTime; 
	}

	public void disable() {
		this._scoreDisplay.disable();
	}

	public long getCoins() { return this._coins; }
}
