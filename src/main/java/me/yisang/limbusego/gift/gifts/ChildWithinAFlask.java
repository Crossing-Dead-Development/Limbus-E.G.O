package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class ChildWithinAFlask extends BaseAccessory {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long CD = 120_000L;
    public ChildWithinAFlask(GiftsModule plugin) {
        super(plugin, "child_within_a_flask", "瓶中嬰孩", "&7受致命傷時有一次免死（冷卻 2 分鐘）");
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        if (victim.getHealth() - event.getFinalDamage() > 0) return;
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(victim.getUniqueId(), 0L) > now) return;
        event.setDamage(victim.getHealth() - 0.5);
        cooldowns.put(victim.getUniqueId(), now + CD);
        victim.sendActionBar(plugin.color("&#AAAAAA瓶中嬰孩護你一命！"));
    }
    @Override public void onQuit(Player player) { cooldowns.remove(player.getUniqueId()); }
}
