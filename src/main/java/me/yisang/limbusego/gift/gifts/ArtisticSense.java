package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class ArtisticSense extends BaseAccessory {
    public ArtisticSense(GiftsModule plugin) {
        super(plugin, "artistic_sense", "美感",
                "&7攻擊：施加沉淪 2·2｜攻擊沉淪中或抑鬱目標：+25% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.SINKING) || plugin.sanity().isDepressed(target)) {
            event.setDamage(event.getDamage() * 1.25);
        }
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker);
    }
}
