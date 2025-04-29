package com.gearbound.discordlink;

import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.discordCommands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class CommandLinkEmbed extends DiscordCommand {

    public static final String BUTTON_ID = "start_link_via_button";

    public CommandLinkEmbed() {
        super("linkembed", "Posts the account linking embed.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, ReplyCallbackAction reply) {
        LinkEmbedConfig config = LinkAddon.getEmbedConfig();
        if (config == null) {
            reply.setContent("Error: Embed configuration not loaded.").setEphemeral(true).queue();
            return;
        }

        GuildMessageChannel channel = DiscordIntegration.INSTANCE.getChannel(config.channelId);
        if (channel == null) {
            reply.setContent("Error: Configured channel ID '" + config.channelId + "' is invalid or bot cannot access it.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(config.embedTitle);
        embedBuilder.setDescription(config.embedDescription);
        try {
            embedBuilder.setColor(Color.decode(config.embedColor));
        } catch (NumberFormatException e) {
            embedBuilder.setColor(Color.decode("#5865F2")); // Default
            DiscordIntegration.LOGGER.warn("Invalid embed color format in LinkEmbedConfig. Using default.");
        }

        Button linkButton = Button.primary(BUTTON_ID, config.buttonLabel);

        reply.setContent("Sending link embed...").setEphemeral(true).queue(hook -> {
            channel.sendMessageEmbeds(embedBuilder.build())
                    .addActionRow(linkButton)
                    .queue(
                            success -> hook.editOriginal("Link embed posted successfully in <#" + channel.getId() + ">.").queue(), // Edit original reply on success
                            failure -> {
                                hook.editOriginal("Error sending embed: " + failure.getMessage()).queue();
                                DiscordIntegration.LOGGER.error("Failed to send link embed", failure);
                            }
                    );
        });
    }
}
