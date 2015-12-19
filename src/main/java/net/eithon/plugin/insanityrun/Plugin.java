package net.eithon.plugin.insanityrun;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.insanityrun.logic.Controller;

import org.bukkit.event.Listener;

public final class Plugin extends EithonPlugin {
	private Controller _controller;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._controller = new Controller(this);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		Listener eventListener = new EventListener(this, this._controller);
		super.activate(commandHandler, eventListener);

		// Is the config.yml set up for Vault support?
		if (Config.V.useVault) {
			//this.logger.info(pdfFile.getName() + " " + plugin.getConfig().getString(useLanguage + ".vaultEnabled"));
			// See if Vault is loaded
			/*
			if (Bukkit.getPluginManager().getPlugin("Vault") instanceof Vault) {
				this.logger.info(pdfFile.getName() + " " + plugin.getConfig().getString(useLanguage + ".vaultFound"));
				RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

				if(service != null) {
					economy = service.getProvider();
				}


			}
			else {
				this.logger.info(pdfFile.getName() + " " + plugin.getConfig().getString(useLanguage + ".vaultNotFound"));
				useVault = false; // They don't have vault so disable it!
			}
			*/
		}
		else {
			//this.logger.info(pdfFile.getName() + " " + plugin.getConfig().getString(useLanguage + ".vaultDisabled"));
		}

		// Enable MCStats.com statistics
		/*
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			this.logger.info(pdfFile.getName() + "InsanityRun: MCStats not enabled");
		}
		*/
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		this._controller.kickAllRunners();
		this._controller.endRepeatingTask();
		this._controller = null;
	}
}
