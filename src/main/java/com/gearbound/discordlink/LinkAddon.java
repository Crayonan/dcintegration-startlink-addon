package com.gearbound.discordlink;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.addon.AddonConfigRegistry;
import de.erdbeerbaerlp.dcintegration.common.addon.DiscordIntegrationAddon;
import de.erdbeerbaerlp.dcintegration.common.storage.CommandRegistry;
import java.io.File;

public class LinkAddon implements DiscordIntegrationAddon {
    private ModalHandler modalAndButtonHandler;
    private static LinkEmbedConfig embedConfig;

    @Override
    public void load(DiscordIntegration dc) {
        File configFile = new File(DiscordIntegration.discordDataDir,"addons/" + LinkEmbedConfig.CONFIG_FILE_NAME);
        embedConfig = AddonConfigRegistry.loadConfig(new LinkEmbedConfig(), configFile);

        if (embedConfig == null) {
            DiscordIntegration.LOGGER.error("Failed to load LinkEmbedConfig! Using defaults.");
            embedConfig = new LinkEmbedConfig();
            AddonConfigRegistry.saveConfig(embedConfig, configFile);
        }

        CommandRegistry.registerCommand(new CommandLinkEmbed());

        modalAndButtonHandler = new ModalHandler();
        dc.getJDA().addEventListener(modalAndButtonHandler);

        DiscordIntegration.LOGGER.info("📦 Discord Link Embed Addon loaded (Direct Button -> Modal Attempt)");
    }

    @Override
    public void unload(DiscordIntegration dc) {
        if (modalAndButtonHandler != null) {
            dc.getJDA().removeEventListener(modalAndButtonHandler);
        }
        DiscordIntegration.LOGGER.info("🛑 Discord Link Embed Addon unloaded");
    }

    @Override
    public void reload() {
        File configFile = new File(DiscordIntegration.discordDataDir,"addons/" + LinkEmbedConfig.CONFIG_FILE_NAME);
        embedConfig = AddonConfigRegistry.loadConfig(new LinkEmbedConfig(), configFile);
        if (embedConfig == null) {
            DiscordIntegration.LOGGER.error("Failed to reload LinkEmbedConfig! Using defaults.");
            embedConfig = new LinkEmbedConfig();
        }
        DiscordIntegration.LOGGER.info("📦 Discord Link Embed Addon reloaded config");
    }

    public static LinkEmbedConfig getEmbedConfig() {
        return embedConfig;
    }
}