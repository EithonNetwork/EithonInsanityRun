package net.eithon.plugin.insanityrun.logic;

import java.awt.Point;

import net.eithon.plugin.insanityrun.Config;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

class BlockUnderFeet {
	private Block _block;
	private RunnerEffect _runnerEffect;

	public enum RunnerEffect {
		NONE, COIN, SLOW, SPEED, JUMP, PUMPKIN_HELMET, FREEZE, BOUNCE, CHECKPOINT, FINISH, WATER, LAVA, DRUNK, BLIND, DARK
	}

	BlockUnderFeet(final Location feetLocation) {
		this._block = null;
		this._runnerEffect = RunnerEffect.NONE;
		Block feetBlock = feetLocation.getBlock();
		for (int delta = 0; delta <= Config.V.maxHeightOverBlock; delta++) {
			Block block = feetBlock.getWorld().getBlockAt(feetBlock.getX(),  feetBlock.getY()-delta, feetBlock.getZ());
			Material blockMaterial = block.getType();
			switch(blockMaterial) {
			case AIR:
				continue;
			case WATER:
			case STATIONARY_WATER:
			case LAVA:
			case STATIONARY_LAVA:
				if (delta == 0) this._block = block;
			default:
				if (delta > 0) this._block = block;
			}
		}
		this._runnerEffect = translateMaterialToRunnerEffect();
	}

	public boolean notFound() { return this._block == null; }

	RunnerEffect getRunnerEffect() { return this._runnerEffect; }
	
	private RunnerEffect translateMaterialToRunnerEffect() {
		if (this._block == null) return RunnerEffect.NONE;
		final Material blockMaterial = this._block.getType();
		switch(blockMaterial) {
		case GOLD_BLOCK: return RunnerEffect.COIN;
		case DIAMOND_BLOCK: return RunnerEffect.JUMP;
		case EMERALD_BLOCK: return RunnerEffect.SPEED;
		case PUMPKIN: return RunnerEffect.PUMPKIN_HELMET;
		case ICE: return RunnerEffect.FREEZE;
		case SPONGE: return RunnerEffect.BOUNCE;
		case GLOWSTONE: return RunnerEffect.CHECKPOINT;
		case REDSTONE_BLOCK: return RunnerEffect.FINISH;
		case LAPIS_BLOCK: return RunnerEffect.DRUNK;
		case COAL_BLOCK: return RunnerEffect.BLIND;
		case OBSIDIAN: return RunnerEffect.DARK;
		case SAND:
		case GRAVEL:
			return RunnerEffect.SLOW;
		case WATER:
		case STATIONARY_WATER:
			return RunnerEffect.WATER;
		case LAVA:
		case STATIONARY_LAVA:
			return RunnerEffect.LAVA;
		default:
			return RunnerEffect.NONE;
		}
	}

	Block getBlock() { return this._block;}
}
