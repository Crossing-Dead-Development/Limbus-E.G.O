package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class ColdIllusion extends BaseAccessory {
    public ColdIllusion(GiftsModule plugin) {
        super(plugin, "cold_illusion", "冰冷的幻想",
                "&#33CF4F", "綻放的九人會。",
                "攻擊：施加沉淪 2·2 與束縛 1·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker);
        apply(target, StatusEffect.BIND, 1, 2, attacker);
    }
}
