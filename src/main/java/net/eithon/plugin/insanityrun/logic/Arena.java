package net.eithon.plugin.insanityrun.logic;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.json.JsonObject;
import net.eithon.plugin.insanityrun.Config;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

class Arena extends JsonObject<Arena>{
	private String _name;
	private PlayerCollection<Runner> _runners = new PlayerCollection<Runner>();
	private EithonLocation _spawnLocation;
	private String _linkedToArenaName;
	private double _price;
	private double _reward;
	private Arena _linkedToArena;
	
	private Arena() {
		this._runners = new PlayerCollection<Runner>();
		this._price = Config.V.defaultArenaPrice;
		this._reward = Config.V.defaultArenaReward;
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
		json.put("price", this._price);
		json.put("reward", this._reward);
		return json;
	}

	@Override
	public Arena fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._name = (String) jsonObject.get("name");
		this._spawnLocation = EithonLocation.getFromJson(jsonObject.get("spawnLocation"));
		this._linkedToArenaName = (String) jsonObject.get("linkedToArenaName");
		Long price = (Long) jsonObject.get("price");
		if (price != null) this._price = price.intValue();
		Long reward = (Long) jsonObject.get("reward");
		if (reward != null) this._reward = reward.intValue();
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
	public double getPrice() { return this._price; }
	public double getReward() { return this._reward; }

	public String getLinkedToArenaName() { return this._linkedToArenaName; }
}
