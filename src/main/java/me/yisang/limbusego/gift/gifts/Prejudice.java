package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Prejudice extends BaseAccessory {
    public Prejudice(GiftsModule plugin) {
        super(plugin, "prejudice", "偏見", "&7攻擊血量比例低於自己的目標時：最多 +30% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        double attackerMax = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
        double targetMax = target.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (targetMax <= 0 || attackerMax <= 0) return;
        if (target.getHealth() / targetMax >= attacker.getHealth() / attackerMax) return;
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.15 * m)));
    }
}
