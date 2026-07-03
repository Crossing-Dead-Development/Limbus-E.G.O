package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Rags extends BaseAccessory {
    public Rags(GiftsModule plugin) {
        super(plugin, "rags", "破布",
                "&#755E42", "它們不再能阻止下落的雨水。",
                "攻擊沉淪中目標：+7.5% 傷害並獲得 1 SAN");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.SINKING)) {
            double m = plugin.getUpgradeMultiplier(attacker, getId());
            event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.075 * m)));
            plugin.sanity().gainSan(attacker, 1);
        }
    }
}
