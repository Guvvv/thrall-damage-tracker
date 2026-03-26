package com.thralltracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("thralldamagetracker")
public interface ThrallDamageTrackerConfig extends Config {
    @ConfigItem(keyName = "resetOnNewTarget", name = "Reset on New Target", description = "Reset stats when switching NPCs", position = 1)
    default boolean resetOnNewTarget() {
        return true;
    }

    @ConfigItem(keyName = "fontSize", name = "Font Size", description = "Size of text in panel", position = 2)
    default int fontSize() {
        return 12;
    }

    @ConfigItem(keyName = "npcNameColour", name = "NPC Name Colour", description = "Colour of the target name", position = 3)
    default Color npcNameColour() {
        return Color.WHITE;
    }

    @ConfigItem(keyName = "damageColour", name = "Damage Colour", description = "Colour of the damage value", position = 4)
    default Color damageColour() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "thrallLifespan",
            name = "Thrall Lifespan (seconds)",
            description = "How long your thralls last (60, 90, or 120)",
            position = 5
    )
    default int thrallLifespan() {
        return 60;
    }
}