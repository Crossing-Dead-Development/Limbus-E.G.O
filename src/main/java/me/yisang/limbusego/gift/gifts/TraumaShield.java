package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class TraumaShield extends BaseAccessory {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    public TraumaShield(GiftsModule plugin) {
        super(plugin, "trauma_shield", "精神屏蔽力場",
                "&#CBCBCB", "反精神力場盾",
                "受傷時：每 60 秒吸收一次傷害");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        long now = System.currentTimeMillis();
        if (now - cooldowns.getOrDefault(victim.getUniqueId(), 0L) >= 60_000L) {
            cooldowns.put(victim.getUniqueId(), now);
            event.setDamage(0);
        }
    }
    @Override public void onQuit(Player player) { cooldowns.remove(player.getUniqueId()); }
}
