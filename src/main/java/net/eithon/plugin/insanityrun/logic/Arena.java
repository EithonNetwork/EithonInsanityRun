package net.eithon.plugin.insanityrun.logic;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.JsonObject;
import net.eithon.plugin.insanityrun.Config;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

class Arena extends JsonObject<Arena>{
	public enum GameStyle { INSANITY_RUN, KART, FLY };
	private String _name;
	private PlayerCollection<Runner> _runners = new PlayerCollection<Runner>();
	private EithonLocation _spawnLocation;
	private String _linkedToArenaName;
	private double _price;
	private double _reward;
	private Arena _linkedToArena;
	private GameStyle _gameStyle;

	private Arena() {
		this._runners = new PlayerCollection<Runner>();
		this._price = Config.V.defaultArenaPrice;
		this._gameStyle = GameStyle.KART;
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
		Double price = (Double) jsonObject.get("price");
		if (price != null) this._price = price.doubleValue();
		Double reward = (Double) jsonObject.get("reward");
		if (reward != null) this._reward = reward.doubleValue();
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

	public boolean leaveGame(Player player) {
		Runner runner = this._runners.get(player);
		if (runner == null) return true;
		runner.leaveGame();
		return true;
	}

	public Runner getRunner(Player player) {
		return this._runners.get(player);
	}
	
	public GameStyle getGameStyle() {
		return this._gameStyle;
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

	public void doEverySecond() {
		for (Runner runner : this._runners) {
			runner.doRepeatedly();
			if (!runner.hasBeenIdleTooLong()) continue;
			Player player = runner.getPlayer();
			runner.leaveGame();
			Config.M.idleKick.sendMessage(player);
		}
	}

	public boolean resetGame(Player player) {
		Runner runner = this._runners.get(player);
		if (runner == null) return true;
		runner.teleportToSpawn();
		return true;
	}

	// When we need to kick all runners, do that with refund
	public void kickAllRunners() {
		for (Runner runner : this._runners) {
			runner.leaveGameWithRefund();
		}
	}

	public void playerMoved(EithonPlugin plugin, Player player, Location location) {
		final Runner runner = this._runners.get(player);
		if ((runner == null) || !runner.isInGame()) return;

		runner.playerMoved(plugin, location);
	}

	public void maybeLeaveGameBecauseOfTeleport(Player player, Location from, Location to) {
		final Runner runner = this._runners.get(player);
		if ((runner == null) || !runner.isInGame()) return;
		runner.maybeLeaveGameBecauseOfTeleport(from, to);
	}

	public boolean isInGame(Player player) {
		final Runner runner = this._runners.get(player);		
		return ((runner != null) && runner.isInGame());
	}

	public void playerLeftArena(Player player) {
		this._runners.remove(player);
	}

	public void setPrice(double amount) { this._price = amount; }
	public void setReward(double amount) { this._reward = amount; }
}
