package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Ruin extends BaseAccessory {
    public Ruin(GiftsModule plugin) {
        super(plugin, "ruin", "破滅",
                "&7攻擊：施加破裂 3·2｜對已破裂目標追加脆弱 1·1，但自身損失 0.5 生命");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        boolean already = has(target, StatusEffect.RUPTURE);
        applyScaled(target, StatusEffect.RUPTURE, 3, 2, attacker);
        if (already) {
            apply(target, StatusEffect.FRAGILE, 1, 1, attacker);
            attacker.setHealth(Math.max(1.0, attacker.getHealth() - 0.5));
        }
    }
}
