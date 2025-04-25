package com.gearbound.discordlink;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.UUID;

public class ModalHandler extends ListenerAdapter {
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("link_modal")) return;

        String codeStr = event.getValue("link_code").getAsString().trim();
        String discordID = event.getUser().getId();

        try {
            if (LinkManager.isDiscordUserLinked(discordID)) {
                UUID existingUUID = null;
                if (LinkManager.isDiscordUserLinkedToJava(discordID)) {
                    existingUUID = UUID.fromString(LinkManager.getLink(discordID, null).mcPlayerUUID);
                } else if (LinkManager.isDiscordUserLinkedToBedrock(discordID)) {
                    existingUUID = UUID.fromString(LinkManager.getLink(discordID, null).floodgateUUID);
                }

                event.replyEmbeds(new EmbedBuilder()
                        .setColor(0xFFA500)
                        .setDescription(Localization.instance().linking.alreadyLinked.replace(
                                "%player%", MessageUtils.getNameFromUUID(existingUUID)
                        ))
                        .build()).setEphemeral(true).queue();
                return;
            }

            int code = Integer.parseInt(codeStr);
            boolean linked = false;

            if (LinkManager.pendingLinks.containsKey(code)) {
                linked = LinkManager.linkPlayer(discordID, LinkManager.pendingLinks.get(code).getValue());
            } else if (LinkManager.pendingBedrockLinks.containsKey(code)) {
                linked = LinkManager.linkBedrockPlayer(discordID, LinkManager.pendingBedrockLinks.get(code).getValue());
            }

            if (linked) {
                LinkManager.save();
                UUID linkedUUID = LinkManager.getLink(discordID, null).mcPlayerUUID.isEmpty() ?
                        UUID.fromString(LinkManager.getLink(discordID, null).floodgateUUID) :
                        UUID.fromString(LinkManager.getLink(discordID, null).mcPlayerUUID);

                event.replyEmbeds(new EmbedBuilder()
                        .setColor(0x00FF00)
                        .setDescription(Localization.instance().linking.linkSuccessful
                                .replace("%prefix%", "/")
                                .replace("%player%", MessageUtils.getNameFromUUID(linkedUUID)))
                        .build()).setEphemeral(true).queue();
            } else {
                event.replyEmbeds(new EmbedBuilder()
                        .setColor(0xFF0000)
                        .setDescription(Localization.instance().linking.linkFailed)
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
                    .setDescription("❌ Error: " + e.getMessage())
                    .build()).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }
}
