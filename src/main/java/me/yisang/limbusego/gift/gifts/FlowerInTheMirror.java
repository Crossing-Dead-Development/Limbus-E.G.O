package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
public class FlowerInTheMirror extends BaseAccessory {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long CD = 90_000L;
    private final Random rng = new Random();
    public FlowerInTheMirror(GiftsModule plugin) {
        super(plugin, "flower_in_the_mirror", "鏡中花", "&7受致命傷時 40% 機率免死並獲得隱形+速度");
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        if (victim.getHealth() - event.getFinalDamage() > 0) return;
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(victim.getUniqueId(), 0L) > now) return;
        double m = plugin.getUpgradeMultiplier(victim, getId());
        if (rng.nextDouble() >= 0.40 * m) return;
        event.setDamage(victim.getHealth() - 0.5);
        cooldowns.put(victim.getUniqueId(), now + CD);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, true, false));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false));
        victim.getNearbyEntities(8, 8, 8).forEach(e -> {
            if (e instanceof org.bukkit.entity.Mob mob) mob.setTarget(null);
        });
    }
    @Override public void onQuit(Player player) { cooldowns.remove(player.getUniqueId()); }
}
