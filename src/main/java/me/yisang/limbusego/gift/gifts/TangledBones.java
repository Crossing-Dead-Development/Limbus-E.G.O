package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class TangledBones extends BaseAccessory {
    public TangledBones(GiftsModule plugin) {
        super(plugin, "tangled_bones", "破碎的骨片",
                "&#969696", "骨片已經亂成一團，無法辨認出原本的形狀了。",
                "攻擊：施加沉淪 2·2｜攻擊抑鬱目標：+15% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (plugin.sanity().isDepressed(target)) {
            event.setDamage(event.getDamage() * 1.15);
        }
        applyScaled(target, StatusEffect.SINKING, 2, 2, attacker);
    }
}
