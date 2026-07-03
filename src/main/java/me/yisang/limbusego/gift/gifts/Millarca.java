package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Millarca extends BaseAccessory {
    public Millarca(GiftsModule plugin) {
        super(plugin, "millarca", "蜜拉卡",
                "&7攻擊：施加流血 2·2｜攻擊流血中目標：偷取 1 點生命（隨升級提升）");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        boolean wasBleeding = has(target, StatusEffect.BLEED);
        applyScaled(target, StatusEffect.BLEED, 2, 2, attacker);
        if (wasBleeding) {
            double m = plugin.getUpgradeMultiplier(attacker, getId());
            double heal = 1 * m;
            attacker.setHealth(Math.min(attacker.getAttribute(Attribute.MAX_HEALTH).getValue(), attacker.getHealth() + heal));
        }
    }
}
