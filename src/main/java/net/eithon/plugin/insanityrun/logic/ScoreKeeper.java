package net.eithon.plugin.insanityrun.logic;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;

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

	public long updateTimeScore() {
		return getRunTimeInMillisecondsAndUpdateScore();
	}

	public void updateTimeScore(long timeInMilliseconds) {
		this._scoreDisplay.setTimeScore((int) Math.floor(timeInMilliseconds/100.0));
	}

	public void addCoinScore(int coins) {
		this._coins += coins;
		this._scoreDisplay.setCoinScore(this._coins);		
	}

	public void resetCoins() {
		this._coins = 0;
		this._scoreDisplay.setCoinScore(this._coins);		
	}

	public long getRunTimeInSecondsAndUpdateScore() { return (long) Math.floor(getRunTimeInMillisecondsAndUpdateScore()/1000.0); }
	public long getRunTimeInMillisecondsAndUpdateScore() { 
		final long runTime = System.currentTimeMillis()-this._startTime;
		updateTimeScore(runTime);
		return runTime; 
	}
	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: ScoreKeeper.%s: %s", method, message);
	}
}
