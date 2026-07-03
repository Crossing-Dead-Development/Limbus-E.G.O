package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
public class RustyCommemorativeCoin extends BaseAccessory {
    public RustyCommemorativeCoin(GiftsModule plugin) {
        super(plugin, "rusty_commemorative_coin", "生鏽的紀念幣",
                "&7攻擊低血量目標：每 8 秒處決一次｜擊殺時：獲得強壯 2·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        double max = target.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (max <= 0) return;
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (target.getHealth() / max >= Math.min(0.30, 0.15 * m)) return;
        if (!gate(attacker, 8000)) return;
        status().hurtTrue(target, attacker, target.getHealth() + 10, StatusEffect.RUPTURE);
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        applyScaled(killer, StatusEffect.POWER, 2, 2, killer);
    }
}
