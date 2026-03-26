package com.thralltracker;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ThrallDamageTrackerPanel extends PluginPanel {
    private final ThrallDamageTrackerPlugin plugin;
    private final ThrallDamageTrackerConfig config;
    private final JLabel npcNameLabel;
    private final JLabel damageValueLabel;
    private final JLabel durationValueLabel;
    private final JLabel statusLabel;

    public ThrallDamageTrackerPanel(ThrallDamageTrackerPlugin plugin, ThrallDamageTrackerConfig config) {
        super();
        this.plugin = plugin;
        this.config = config;

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Thrall Damage Tracker", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));

        JPanel statsCard = new JPanel(new GridBagLayout());
        statsCard.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);

        npcNameLabel = new JLabel("No target", SwingConstants.CENTER);
        c.gridy = 0;
        c.gridwidth = 2;
        statsCard.add(npcNameLabel, c);

        damageValueLabel = new JLabel("0", SwingConstants.RIGHT);
        c.gridy = 1;
        c.gridwidth = 1;
        statsCard.add(new JLabel("Damage:"), c);
        c.gridx = 1;
        statsCard.add(damageValueLabel, c);

        durationValueLabel = new JLabel("0s", SwingConstants.RIGHT);
        c.gridx = 0;
        c.gridy = 2;
        statsCard.add(new JLabel("Time:"), c);
        c.gridx = 1;
        statsCard.add(durationValueLabel, c);

        statusLabel = new JLabel("Waiting for thrall...", SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 11f));

        JButton resetButton = new JButton("RESET SESSION");
        resetButton.setBackground(new Color(180, 40, 40));
        resetButton.setForeground(Color.WHITE);
        resetButton.addActionListener(e -> plugin.resetTracking());

        add(titleLabel, BorderLayout.NORTH);
        add(statsCard, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 5));
        bottom.setBackground(ColorScheme.DARK_GRAY_COLOR);
        bottom.add(statusLabel);
        bottom.add(resetButton);
        add(bottom, BorderLayout.SOUTH);

        applyConfig();
    }

    public void applyConfig() {
        npcNameLabel.setFont(npcNameLabel.getFont().deriveFont(Font.BOLD, (float) config.fontSize() + 2));
        npcNameLabel.setForeground(config.npcNameColour());
        damageValueLabel.setForeground(config.damageColour());
    }

    public void updateDisplay(String npcName, int damage, long seconds) {
        SwingUtilities.invokeLater(() -> {
            int remaining = plugin.getSecondsRemaining();

            npcNameLabel.setText(npcName != null ? npcName : "No target");
            damageValueLabel.setText(String.valueOf(damage));

            durationValueLabel.setText(remaining + "s");
            durationValueLabel.setForeground(getTimerColor(remaining));

            statusLabel.setText(remaining > 0 ? "Thrall Active" : "Thrall Expired");
            statusLabel.setForeground(remaining > 10 ? Color.GREEN : Color.RED);

            revalidate();
            repaint();
        });
    }

    private Color getTimerColor(int secondsLeft) {
        if (secondsLeft <= 10) return Color.RED;
        if (secondsLeft <= 20) return Color.YELLOW;
        return Color.WHITE;
    }
}