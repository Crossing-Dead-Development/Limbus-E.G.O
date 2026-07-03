package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class Homeward extends BaseAccessory {
    private final Map<UUID, Long> lastCombat = new HashMap<>();
    private final Map<UUID, Boolean> claimed = new HashMap<>();
    public Homeward(GiftsModule plugin) {
        super(plugin, "homeward", "歸途",
                "&#8EC58E", "\"在我奪走你的家之前，帶著它離開。快。\"",
                "脫戰 5 秒後：每次脫戰一次，回復最多 50% 最大生命");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        markCombat(attacker);
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        markCombat(victim);
    }
    @Override public void onPassiveTick(Player player) {
        UUID id = player.getUniqueId();
        Long last = lastCombat.get(id);
        if (last == null || claimed.getOrDefault(id, false)) return;
        if (System.currentTimeMillis() - last < 5000) return;
        double m = plugin.getUpgradeMultiplier(player, getId());
        double max = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double heal = max * Math.min(0.5, 0.20 * m);
        player.setHealth(Math.min(max, player.getHealth() + heal));
        claimed.put(id, true);
    }
    private void markCombat(Player player) {
        lastCombat.put(player.getUniqueId(), System.currentTimeMillis());
        claimed.put(player.getUniqueId(), false);
    }
    @Override public void onQuit(Player player) {
        super.onQuit(player);
        lastCombat.remove(player.getUniqueId());
        claimed.remove(player.getUniqueId());
    }
}
