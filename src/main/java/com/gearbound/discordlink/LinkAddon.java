package com.gearbound.discordlink;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.addon.DiscordIntegrationAddon;
import de.erdbeerbaerlp.dcintegration.common.storage.CommandRegistry;

public class LinkAddon implements DiscordIntegrationAddon {
    private ModalHandler modalHandler;

    @Override
    public void load(DiscordIntegration dc) {
        CommandRegistry.registerCommand(new CommandStartLinkModal());
        modalHandler = new ModalHandler();
        dc.getJDA().addEventListener(modalHandler);
        DiscordIntegration.LOGGER.info("📦 Discord Link Modal addon loaded");
    }

    @Override
    public void unload(DiscordIntegration dc) {
        dc.getJDA().removeEventListener(modalHandler);
        DiscordIntegration.LOGGER.info("🛑 Discord Link Modal addon unloaded");
    }

    @Override
    public void reload() {
        // Not used
    }
}
