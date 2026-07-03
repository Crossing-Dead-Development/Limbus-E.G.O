package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Objects;
public class ArdentFlower extends BaseAccessory {
    public ArdentFlower(GiftsModule plugin) {
        super(plugin, "ardent_flower", "火光花",
                "&#FF7000", "煉獄炎蝶之夢。",
                "被動：免疫火焰傷害｜攻擊：施加燒傷 2·2｜攻擊燒傷中且生命低於30%的目標：+30% 傷害");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30, 0, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.BURN, 2, 2, attacker);
        double max = Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue();
        if (has(target, StatusEffect.BURN) && target.getHealth() < max * 0.30) {
            event.setDamage(event.getDamage() * 1.30);
        }
    }
}
