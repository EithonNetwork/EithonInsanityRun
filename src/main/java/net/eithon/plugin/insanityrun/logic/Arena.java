package net.eithon.plugin.insanityrun.logic;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.json.JsonObject;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

class Arena extends JsonObject<Arena>{
	private String _name;
	private PlayerCollection<Runner> _runners = new PlayerCollection<Runner>();
	private EithonLocation _spawnLocation;
	private String _linkedToArenaName;
	private Arena _linkedToArena;
	
	private Arena() {
		this._runners = new PlayerCollection<Runner>();
	}
	
	public Arena(String name, Location spawnLocation) {
		this();
		this._name = name;
		this._spawnLocation = new EithonLocation(spawnLocation);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("name", this._name);
		json.put("spawnLocation", this._spawnLocation.toJson());
		json.put("linkedToArenaName", this._linkedToArenaName);
		return json;
	}

	@Override
	public Arena fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._name = (String) jsonObject.get("name");
		this._spawnLocation = EithonLocation.getFromJson(jsonObject.get("spawnLocation"));
		this._linkedToArenaName = (String) jsonObject.get("linkedToArenaName");
		return this;
	}

	@Override
	public Arena factory() {
		return new Arena();
	}	

	public static Arena getFromJson(Object json) {
		return new Arena().fromJson(json);
	}

	public Runner joinGame(Player player) {
		Runner runner = new Runner(player, this);
		this._runners.put(player, runner);
		return runner;
	}

	public void runnerLeft(Runner runner) {
		this._runners.remove(runner.getPlayer());
	}

	public Runner getRunner(Player player) {
		return this._runners.get(player);
	}

	public Location getSpawnLocation() { return this._spawnLocation.getLocation(); }

	public void linkToArena(Arena arena) {
		this._linkedToArena = arena;
		this._linkedToArenaName = arena == null ? null : arena._name;
	}

	public String getName() { return this._name; }

	public String getLinkedToArenaName() { return this._linkedToArenaName; }
}
