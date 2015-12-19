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

	public void updateTimeScore() {
		final long currentTimeMillis = System.currentTimeMillis();
		verbose("updateTimeScore","currentTimeMillis: %d", currentTimeMillis);
		int runTimeSeconds = (int) Math.round((currentTimeMillis - this._startTime)/1000.0);
		verbose("updateTimeScore","runTimeSeconds: %d", runTimeSeconds);
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

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "EithonInsanityRun: ScoreKeeper.%s: %s", method, message);
	}
}
