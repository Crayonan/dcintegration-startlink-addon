package com.gearbound.discordlink;

import com.moandjiezana.toml.TomlComment;
import com.moandjiezana.toml.TomlIgnore;
import de.erdbeerbaerlp.dcintegration.common.util.TextColors;

@SuppressWarnings("unused")
public class LinkEmbedConfig {

    @TomlIgnore
    public static final String CONFIG_FILE_NAME = "Link Embed Addon.toml";

    @TomlComment({"Channel ID where the embed with the link button will be posted.",
            "Leave 'default' to use the main bot channel configured in DiscordIntegration."})
    public String channelId = "default";

    @TomlComment("Title of the embed message.")
    public String embedTitle = "**Start linking your Minecraft account to Discord**";

    @TomlComment("Main text/description of the embed message.")
    public String embedDescription = "Press the button below and fill in your linking code.";

    @TomlComment("Color of the embed's side bar (HEX format, e.g., #5865F2 for Discord Blurple).")
    public String embedColor = TextColors.DISCORD_BLURPLE.asHexString();

    @TomlComment("Text displayed on the button.")
    public String buttonLabel = "Start Linking";

    public LinkEmbedConfig() {}
}