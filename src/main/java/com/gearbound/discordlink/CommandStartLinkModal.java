package com.gearbound.discordlink;

import de.erdbeerbaerlp.dcintegration.common.discordCommands.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class CommandStartLinkModal extends DiscordCommand {
    public CommandStartLinkModal() {
        super("startlink", "Start Minecraft account linking process");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, ReplyCallbackAction reply) {
        TextInput codeInput = TextInput.create("link_code", "Link Code", TextInputStyle.SHORT)
                .setPlaceholder("12345")
                .setRequired(true)
                .build();

        Modal modal = Modal.create("link_modal", "Link Account")
                .addActionRow(codeInput)
                .build();

        event.replyModal(modal).queue();
    }
}
