package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Prejudice extends BaseAccessory {
    public Prejudice(GiftsModule plugin) {
        super(plugin, "prejudice", "偏見", "&7對亡靈與蜘蛛類造成額外 50% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        Entity target = event.getEntity();
        boolean isUndead = target instanceof Zombie || target instanceof Skeleton ||
            target instanceof Phantom || target instanceof WitherSkeleton ||
            target instanceof Drowned || target instanceof Stray ||
            target instanceof Wither;
        boolean isSpider = target instanceof Spider || target instanceof CaveSpider;
        if (!isUndead && !isSpider) return;
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        event.setDamage(event.getDamage() * (1.0 + 0.5 * m));
    }
}
