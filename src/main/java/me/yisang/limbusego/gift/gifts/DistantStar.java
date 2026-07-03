package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class DistantStar extends BaseAccessory {
    public DistantStar(GiftsModule plugin) {
        super(plugin, "distant_star", "彼方之星",
                "&#00DAFF", "追從著群星。",
                "攻擊沉淪中目標：獲得 1 SAN 並延長沉淪 1 層");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.SINKING)) {
            double m = plugin.getUpgradeMultiplier(attacker, getId());
            plugin.sanity().gainSan(attacker, (int) Math.round(1 * m));
            status().refresh(target, StatusEffect.SINKING, 1);
        }
    }
}
