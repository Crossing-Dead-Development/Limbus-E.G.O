package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Carmilla extends BaseAccessory {
    public Carmilla(GiftsModule plugin) {
        super(plugin, "carmilla", "卡蜜拉", "&7攻擊滿血目標：+20% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (target.getHealth() >= target.getAttribute(Attribute.MAX_HEALTH).getValue() - 0.01) {
            double m = plugin.getUpgradeMultiplier(attacker, getId());
            event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.20 * m)));
        }
    }
}
