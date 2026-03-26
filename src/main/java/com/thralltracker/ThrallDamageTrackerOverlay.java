package com.thralltracker;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class ThrallDamageTrackerOverlay extends OverlayPanel {
    private final ThrallDamageTrackerPlugin plugin;

    @Inject
    private ThrallDamageTrackerOverlay(ThrallDamageTrackerPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        int damage = plugin.getThrallDamage();
        int remaining = plugin.getSecondsRemaining();

        if (remaining <= 0 && damage <= 0) return null;

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Thrall Tracker")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Damage:")
                .right(Integer.toString(damage))
                .build());

        // Time Remaining with Dynamic Colour
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Time:")
                .right(remaining + "s")
                .rightColor(getTimerColor(remaining)) // This sets the text colour
                .build());

        return super.render(graphics);
    }

    private Color getTimerColor(int secondsLeft) {
        if (secondsLeft <= 10) return Color.RED;
        if (secondsLeft <= 20) return Color.YELLOW;
        return Color.GREEN;
    }
}