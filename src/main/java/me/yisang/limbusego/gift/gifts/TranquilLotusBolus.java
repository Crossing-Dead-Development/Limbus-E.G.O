package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class TranquilLotusBolus extends BaseAccessory {
    private final Map<UUID, Long> sanTicks = new HashMap<>();
    public TranquilLotusBolus(GiftsModule plugin) {
        super(plugin, "tranquil_lotus_bolus", "靜蓮丸",
                "&#E07F9A", "枉自嗟。",
                "被動：每 5 秒獲得守護 2·2，每 10 秒回復 1 SAN");
    }
    @Override public void onPassiveTick(Player player) {
        if (gate(player, 5000)) applyScaled(player, StatusEffect.PROTECTION, 2, 2, player);
        long now = System.currentTimeMillis();
        Long last = sanTicks.get(player.getUniqueId());
        if (last == null || now - last >= 10_000) {
            plugin.sanity().gainSan(player, 1);
            sanTicks.put(player.getUniqueId(), now);
        }
    }
    @Override public void onQuit(Player player) {
        super.onQuit(player);
        sanTicks.remove(player.getUniqueId());
    }
}
