package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class TangledBones extends BaseAccessory {
    public TangledBones(GiftsModule plugin) {
        super(plugin, "tangled_bones", "破碎的骨片",
                "&#969696", "骨片已經亂成一團，無法辨認出原本的形狀了。",
                "攻擊時：對骷髏類傷害翻倍；20% 機率點燃");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        Entity target = event.getEntity();
        if (isSkeleton(target)) {
            event.setDamage(event.getDamage() * 2.0);
            if (Math.random() < 0.20 && target instanceof LivingEntity le) {
                le.setFireTicks(60);
            }
        }
    }
    private boolean isSkeleton(Entity e) {
        return e instanceof Skeleton || e instanceof WitherSkeleton || e instanceof Stray;
    }
}
