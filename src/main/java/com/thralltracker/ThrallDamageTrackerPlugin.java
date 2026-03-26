package com.thralltracker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
        name = "Thrall Damage Tracker",
        description = "Tracks damage dealt by your thralls",
        tags = {"combat", "thrall", "tracker", "arceuus"}
)
public class ThrallDamageTrackerPlugin extends Plugin {
    private static final Set<Integer> THRALL_IDS = new HashSet<>();

    static {
        for (int i = 10880; i <= 10888; i++) THRALL_IDS.add(i); // Ghost, Skeleton, Zombie (All tiers)
    }

    @Inject
    private Client client;
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private ThrallDamageTrackerConfig config;
    @Inject
    private ThrallDamageTrackerOverlay overlay;
    @Inject
    private net.runelite.client.ui.overlay.OverlayManager overlayManager;

    public int getThrallDamage() {
        return thrallDamage;
    }

    private ThrallDamageTrackerPanel panel;
    private NavigationButton navButton;

    private NPC currentTargetNpc = null;
    private int thrallDamage = 0;
    private long trackingStartMs = 0;
    private final Set<NPC> myThralls = new HashSet<>();

    @Override
    protected void startUp() throws Exception {
        panel = new ThrallDamageTrackerPanel(this, config);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Thrall Damage Tracker")
                .icon(icon)
                .priority(6)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
        myThralls.clear();
        resetState();
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        if (event.getMessage().contains("You resurrect")) {
            // Reset ONLY the timer
            trackingStartMs = System.currentTimeMillis();
            updateUI();
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        // If the timer isn't running, we aren't tracking
        if (trackingStartMs == 0) return;

        Hitsplat hitsplat = event.getHitsplat();
        int type = hitsplat.getHitsplatType();

        // 16 = Cyan Me, 17 = Cyan Other (Standard Thrall IDs)
        if (type == 16 || type == 17) {
            if (!(event.getActor() instanceof NPC)) return;
            NPC target = (NPC) event.getActor();

            // Update target name if it changed or is new
            if (currentTargetNpc == null || currentTargetNpc != target) {
                currentTargetNpc = target;
            }

            // Add the damage
            thrallDamage += hitsplat.getAmount();
            updateUI();

            log.info("Thrall hit {} for {}! Total: {}", target.getName(), hitsplat.getAmount(), thrallDamage);
        }
    }


    public int getSecondsRemaining() {
        if (trackingStartMs == 0) return 0;

        long elapsed = (System.currentTimeMillis() - trackingStartMs) / 1000;
        int remaining = config.thrallLifespan() - (int) elapsed;

        return Math.max(0, remaining);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        myThralls.remove(event.getNpc());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        if (THRALL_IDS.contains(npc.getId())) {
            myThralls.add(npc);
            log.info("FOUND THRALL: {} (Index: {})", npc.getName(), npc.getIndex());
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // If a session is active (timer > 0), refresh the UI every 0.6s
        if (trackingStartMs != 0) {
            updateUI();


            if (getSecondsRemaining() <= 0) {
                log.info("Thrall lifespan reached.");
            }
        }
    }

    private void updateUI() {
        if (panel != null) {
            String name = currentTargetNpc != null ? currentTargetNpc.getName() : "None";
            panel.updateDisplay(name, thrallDamage, getElapsedSeconds());
        }
    }

    public long getElapsedSeconds() {
        return trackingStartMs == 0 ? 0 : (System.currentTimeMillis() - trackingStartMs) / 1000;
    }

    private void resetState() {
        thrallDamage = 0;
        trackingStartMs = 0;
        currentTargetNpc = null;
        updateUI();
    }

    @Provides
    ThrallDamageTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ThrallDamageTrackerConfig.class);
    }

    public void resetTracking() {
        thrallDamage = 0;
        trackingStartMs = 0;
        currentTargetNpc = null;
        updateUI();
    }
}
