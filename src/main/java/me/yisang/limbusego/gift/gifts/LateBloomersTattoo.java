package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class LateBloomersTattoo extends BaseAccessory {
    public LateBloomersTattoo(GiftsModule plugin) {
        super(plugin, "late_bloomers_tattoo", "刺青：大器晚成",
                "&7生命低於 50% 時攻擊：獲得強壯 2·2 與守護 2·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (attacker.getHealth() < attacker.getAttribute(Attribute.MAX_HEALTH).getValue() * 0.5) {
            applyScaled(attacker, StatusEffect.POWER, 2, 2, attacker);
            apply(attacker, StatusEffect.PROTECTION, 2, 2, attacker);
        }
    }
}
