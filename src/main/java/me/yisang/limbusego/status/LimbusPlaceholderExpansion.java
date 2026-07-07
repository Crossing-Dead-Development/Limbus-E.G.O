package me.yisang.limbusego.status;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.yisang.limbusego.LimbusEGO;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LimbusPlaceholderExpansion extends PlaceholderExpansion {

    private final LimbusEGO plugin;

    public LimbusPlaceholderExpansion(LimbusEGO plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "YiSang";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "limbusego";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return "";
        }
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) return "";

        String lower = params.toLowerCase();

        if (lower.equals("sanity") || lower.equals("san")) {
            return String.valueOf(plugin.getSanityManager().getSan(onlinePlayer));
        }

        if (lower.equals("effects") || lower.equals("status")) {
            StatusState state = plugin.getStatusManager().get(onlinePlayer);
            if (state == null || state.isEmpty()) {
                return "";
            }
            java.util.List<String> list = new java.util.ArrayList<>();
            for (java.util.Map.Entry<StatusEffect, int[]> entry : state.snapshot().entrySet()) {
                StatusEffect eff = entry.getKey();
                int potency = entry.getValue()[0];
                int count = entry.getValue()[1];
                if (potency > 0 && count > 0) {
                    String name = plugin.getLang().get("status." + eff.name().toLowerCase());
                    list.add(eff.color + name + " " + potency + "·" + count);
                }
            }
            return String.join(" ", list);
        }

        for (StatusEffect eff : StatusEffect.values()) {
            String prefix = eff.name().toLowerCase() + "_";
            if (lower.startsWith(prefix)) {
                String sub = lower.substring(prefix.length());
                StatusState state = plugin.getStatusManager().get(onlinePlayer);
                int val = 0;
                if (state != null) {
                    if (sub.equals("potency") || sub.equals("pot")) {
                        val = state.potency(eff);
                    } else if (sub.equals("count") || sub.equals("cnt")) {
                        val = state.count(eff);
                    }
                }
                return String.valueOf(val);
            }
        }

        return null;
    }
}
