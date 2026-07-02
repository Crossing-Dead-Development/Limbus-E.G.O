package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class GlimpseOfFlames extends BaseAccessory {
    public GlimpseOfFlames(GiftsModule plugin) {
        super(plugin, "glimpse_of_flames", "炎鱗", "&7被攻擊時點燃攻擊者 3 秒並噴射火焰粒子");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        Entity damager = event.getDamager();
        double m = plugin.getUpgradeMultiplier(victim, getId());
        damager.setFireTicks((int)(60 * m));
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getLocation().add(0, 1, 0), 12, 0.3, 0.3, 0.3, 0.05);
    }
}
