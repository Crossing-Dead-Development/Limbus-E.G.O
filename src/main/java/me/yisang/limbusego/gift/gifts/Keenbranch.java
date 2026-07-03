package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Keenbranch extends BaseAccessory {
    public Keenbranch(GiftsModule plugin) {
        super(plugin, "keenbranch", "磨尖的樹枝",
                "&7攻擊：20% 機率 +30% 傷害並獲得呼吸法 1·1");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (Math.random() < Math.min(1.0, 0.20 * m)) {
            event.setDamage(event.getDamage() * 1.30);
            apply(attacker, StatusEffect.POISE, 1, 1, attacker);
        }
    }
}
