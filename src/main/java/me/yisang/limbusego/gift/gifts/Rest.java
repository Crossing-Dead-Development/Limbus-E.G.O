package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Rest extends BaseAccessory {
    public Rest(GiftsModule plugin) {
        super(plugin, "rest", "安息",
                "&#FFFFFF", "\"一口棺材被孤單地放置於此。\"",
                "被動：靜止時生命再生 I｜攻擊沉淪中目標：+15% 傷害");
    }
    @Override public void onPassiveTick(Player player) {
        if (player.getVelocity().length() < 0.05) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
        }
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.SINKING)) {
            double m = plugin.getUpgradeMultiplier(attacker, getId());
            event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.15 * m)));
        }
    }
}
