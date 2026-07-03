package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
public class DustToDust extends BaseAccessory {
    public DustToDust(GiftsModule plugin) {
        super(plugin, "dust_to_dust", "土歸土",
                "&#9A9A9A", "那並不是霧。",
                "攻擊：施加燒傷 3·2｜擊殺：對 3 格內敵人擴散燒傷 3·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.BURN, 3, 2, attacker);
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        double m = plugin.getUpgradeMultiplier(killer, getId());
        double r = 3 * m;
        for (var e : event.getEntity().getLocation().getNearbyLivingEntities(r)) {
            if (e.equals(killer) || e instanceof Player) continue;
            apply(e, StatusEffect.BURN, 3, 2, killer);
        }
    }
}
