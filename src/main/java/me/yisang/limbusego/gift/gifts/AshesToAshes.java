package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class AshesToAshes extends BaseAccessory {
    public AshesToAshes(GiftsModule plugin) {
        super(plugin, "ashes_to_ashes", "塵歸塵",
                "&#9A9A9A", "步入迷霧。",
                "攻擊燒傷中目標：疊加燒傷 2·1");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.BURN)) {
            applyScaled(target, StatusEffect.BURN, 2, 1, attacker);
        }
    }
}
