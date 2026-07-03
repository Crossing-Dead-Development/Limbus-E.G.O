package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class ChildWithinAFlask extends BaseAccessory {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long CD = 120_000L;
    public ChildWithinAFlask(GiftsModule plugin) {
        super(plugin, "child_within_a_flask", "瓶中嬰孩",
                "&7受致命傷時：每 2 分鐘免死一次，回復 4 生命並擊退附近敵人");
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        preventLethalDamage(event, victim);
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        preventLethalDamage(event, victim);
    }
    private void preventLethalDamage(EntityDamageEvent event, Player victim) {
        if (victim.getHealth() - event.getFinalDamage() > 0) return;
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(victim.getUniqueId(), 0L) > now) return;
        double m = plugin.getUpgradeMultiplier(victim, getId());
        event.setDamage(0);
        victim.setHealth(Math.min(victim.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue(), 4.0));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 3, true, true));
        for (org.bukkit.entity.Entity entity : victim.getNearbyEntities(3, 3, 3)) {
            if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
                Vector push = living.getLocation().toVector().subtract(victim.getLocation().toVector());
                if (push.lengthSquared() == 0) push = new Vector(0, 0.2, 0);
                living.setVelocity(push.normalize().multiply(1.1).setY(0.35));
            }
        }
        cooldowns.put(victim.getUniqueId(), now + (long) (CD / m));
        victim.sendActionBar(plugin.color("&#AAAAAA瓶中嬰孩護你一命！"));
    }
    @Override public void onQuit(Player player) {
        super.onQuit(player);
        cooldowns.remove(player.getUniqueId());
    }
}
