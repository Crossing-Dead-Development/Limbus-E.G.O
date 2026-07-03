package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class SourLiquorAroma extends BaseAccessory {
    public SourLiquorAroma(GiftsModule plugin) {
        super(plugin, "sour_liquor_aroma", "酸味的酒香",
                "&#00BC6B", "\"原來不是客人，而是惡客啊。\"",
                "攻擊：施加震顫 2·1｜攻擊震顫≥3目標：+20% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (pot(target, StatusEffect.TREMOR) >= 3) {
            double m = plugin.getUpgradeMultiplier(attacker, getId());
            event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.20 * m)));
        }
        apply(target, StatusEffect.TREMOR, 2, 1, attacker);
    }
}
