# EithonInsanityRun

Based on InsanityRun

## Prerequisits

* None

## Releases

### 1.5 (2016-05-04)

* CHANGE: Upgraded to Spigot 1.0 SOUND enum constants.

### 1.4.2 (2016-01-17)

* BUG: Missing messages for some effects.
* BUG: Last checkpoint was not erased when going back to spawn point.

### 1.4.1 (2016-01-11)

* BUG: Score board failed if player names was too long. Solution: Configurable messages for score board. 

### 1.4 (2015-12-30)

* NEW: Configurable player messages for all actions
* CHANGE: The join command is now not listed in the list of eir sub commands.
* BUG: The command reset did not reset the time score.

### 1.3 (2015-12-29)

* NEW: Now uses EithonPlayerMoveHalfBlockEvent
* CHANGE: Freeze now means that player can't jump
* CHANGE: You can now stand idle half the allowed time without hearing any warning noises.
* CHANGE: The idle time warning noise is now GHAST_SCREAM
* CHANGE: Allows teleports for short distances (configurable).
* BUG: Gold blocks taken where saved with the wrong coordinate.

### 1.2 (2015-12-27)

* CHANGE: MaxAirDepth is now a double, providing greater detail for settings.
* CHANGE: Run time is now displayed with format hh:mm:ss.dd
* CHANGE: Multiple changes to make things smoother.
* BUG: Was checking the old player block and not the current.

### 1.1 (2015-12-25)

* NEW: Added price and rewards
* CHANGE: Refactored when a player is removed from the list of active players.

### 1.0.1 (2015-12-26)

* BUG: Problems with capitalization of Arena names.
* BUG: Null pointer exceptions for checkpoints.
* BUG: Block detection under feet is not reliable.

### 1.0 (2015-12-25)

* New: First Eithon release.
