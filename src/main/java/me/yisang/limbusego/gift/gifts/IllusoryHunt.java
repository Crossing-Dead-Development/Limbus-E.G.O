package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class IllusoryHunt extends BaseAccessory {
    public IllusoryHunt(GiftsModule plugin) {
        super(plugin, "illusory_hunt", "異想狩獵",
                "&#C6CDEF", "你是特别的。",
                "攻擊：20% 機率獲得強壯 2·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (Math.random() < Math.min(1.0, 0.20 * m)) {
            apply(attacker, StatusEffect.POWER, 2, 2, attacker);
        }
    }
}
