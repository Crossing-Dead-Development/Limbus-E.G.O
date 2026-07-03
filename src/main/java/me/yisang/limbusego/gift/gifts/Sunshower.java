package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Sunshower extends BaseAccessory {
    public Sunshower(GiftsModule plugin) {
        super(plugin, "sunshower", "狐雨",
                "&7被動：晴天每 5 秒獲得迅捷 2·2，雨天再生｜雨天攻擊：獲得強壯 2·1");
    }
    @Override public void onPassiveTick(Player player) {
        boolean raining = player.getWorld().hasStorm() || player.getWorld().isThundering();
        if (raining) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 1, true, false));
        } else if (gate(player, 5000)) {
            applyScaled(player, StatusEffect.HASTE, 2, 2, player);
        }
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        boolean raining = attacker.getWorld().hasStorm() || attacker.getWorld().isThundering();
        if (raining) applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker);
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        if (victim.getWorld().isThundering() && event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setCancelled(true);
        }
    }
}
