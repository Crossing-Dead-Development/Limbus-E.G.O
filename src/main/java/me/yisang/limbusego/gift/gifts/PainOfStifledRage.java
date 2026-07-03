package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class PainOfStifledRage extends BaseAccessory {
    public PainOfStifledRage(GiftsModule plugin) {
        super(plugin, "pain_of_stifled_rage", "鬱火",
                "&#DA3D24", "公牛絕望地咆哮著……",
                "攻擊燒傷中目標：獲得強壯 2·1；否則施加燒傷 2·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.BURN)) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker);
        } else {
            applyScaled(target, StatusEffect.BURN, 2, 2, attacker);
        }
    }
}
