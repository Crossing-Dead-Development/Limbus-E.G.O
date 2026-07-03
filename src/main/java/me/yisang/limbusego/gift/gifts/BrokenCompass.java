package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class BrokenCompass extends BaseAccessory {
    public BrokenCompass(GiftsModule plugin) {
        super(plugin, "broken_compass", "破碎羅盤",
                "&#0772AB", "失去方向的人們的叫喊聲仍不停息。",
                "攻擊：25% 機率施加沉淪 2·3");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (Math.random() < Math.min(1.0, 0.25 * m)) {
            apply(target, StatusEffect.SINKING, 2, 3, attacker);
        }
    }
}
