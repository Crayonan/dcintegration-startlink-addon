package com.gearbound.discordlink;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.PlayerLink;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.UUID;

public class ModalHandler extends ListenerAdapter {

    /**
     * Creates the Modal for account linking. Static within the handler.
     * @return The Modal object.
     */
    public static Modal createLinkModal() { // Keep as public static
        TextInput codeInput = TextInput.create("link_code", "Link Code", TextInputStyle.SHORT)
                .setPlaceholder("12345")
                .setRequired(true)
                .build();

        return Modal.create("link_modal", "Link Account")
                .addActionRow(codeInput)
                .build();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals(CommandLinkEmbed.BUTTON_ID)) {
            try {
                event.replyModal(createLinkModal()).queue();
            } catch (Exception e) {
                DiscordIntegration.LOGGER.error("Failed to replyModal on button click for button ID: " + event.getComponentId(), e);
                if (!event.isAcknowledged()) {
                    event.reply("❌ An error occurred trying to open the linking window. See console.").setEphemeral(true).queue(s->{}, f->{});
                }
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("link_modal")) return;

        String codeStr = event.getValue("link_code").getAsString().trim();
        String discordID = event.getUser().getId();

        try {
            PlayerLink link = LinkManager.getLink(discordID, null);

            if (link != null) {
                UUID existingUUID = null;
                if (link.mcPlayerUUID != null && !link.mcPlayerUUID.isEmpty()) {
                    existingUUID = UUID.fromString(link.mcPlayerUUID);
                } else if (link.floodgateUUID != null && !link.floodgateUUID.isEmpty()) {
                    existingUUID = UUID.fromString(link.floodgateUUID);
                }

                if (existingUUID != null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(0xFFA500)
                            .setDescription(Localization.instance().linking.alreadyLinked.replace(
                                    "%player%", MessageUtils.getNameFromUUID(existingUUID)
                            ))
                            .build()).setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(0xFF0000)
                            .setDescription("❌ Error: Your account seems partially linked but is missing Minecraft details. Please contact an admin.")
                            .build()).setEphemeral(true).queue();
                }
                return;
            }

            int code = Integer.parseInt(codeStr);
            UUID linkedUUID = null;
            boolean isBedrock = false;

            if (LinkManager.pendingLinks.containsKey(code)) {
                UUID targetUUID = LinkManager.pendingLinks.get(code).getValue();

                if (LinkManager.linkPlayer(discordID, targetUUID)) {
                    linkedUUID = targetUUID;
                    LinkManager.pendingLinks.remove(code);
                }
            } else if (LinkManager.pendingBedrockLinks.containsKey(code)) {
                UUID targetUUID = LinkManager.pendingBedrockLinks.get(code).getValue();
                if (LinkManager.linkBedrockPlayer(discordID, targetUUID)) {
                    linkedUUID = targetUUID;
                    LinkManager.pendingBedrockLinks.remove(code);
                    isBedrock = true;
                }
            }

            if (linkedUUID != null) {
                LinkManager.save();
                event.replyEmbeds(new EmbedBuilder()
                        .setColor(0x00FF00) // Green
                        .setDescription(Localization.instance().linking.linkSuccessful
                                .replace("%prefix%", "/")
                                .replace("%player%", MessageUtils.getNameFromUUID(linkedUUID)))
                        .build()).setEphemeral(true).queue();

                DiscordIntegration.INSTANCE.getServerInterface().sendIngameMessage(
                        Localization.instance().linking.linkSuccessfulIngame
                                .replace("%name%", event.getUser().getName())
                                .replace("%name#tag%", event.getUser().getAsTag()),
                        linkedUUID);

            } else {
                event.replyEmbeds(new EmbedBuilder()
                        .setColor(0xFF0000)
                        .setDescription(Localization.instance().linking.invalidLinkNumber)
                        .build()).setEphemeral(true).queue();
            }
        } catch (NumberFormatException e) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(0xFF0000)
                    .setDescription(Localization.instance().linking.linkNumberNAN)
                    .build()).setEphemeral(true).queue();
        } catch (Exception e) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(0xFF0000)
                    .setDescription("❌ An unexpected error occurred: " + e.getMessage())
                    .build()).setEphemeral(true).queue();
            DiscordIntegration.LOGGER.error("Error during modal interaction handling:", e);
        }
    }
}