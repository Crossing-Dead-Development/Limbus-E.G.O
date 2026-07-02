package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.Random;
public class Sownpour extends BaseAccessory {
    private final Random rng = new Random();
    public Sownpour(GiftsModule plugin) {
        super(plugin, "sownpour", "暴雨", "&7攻擊時 30% 機率連鎖打擊附近敵人（50% 傷害）");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (rng.nextDouble() >= 0.30 * m) return;
        double chainDmg = event.getDamage() * 0.5 * m;
        event.getEntity().getNearbyEntities(3, 3, 3).forEach(e -> {
            if (e instanceof LivingEntity le && !(e instanceof Player) && !e.equals(event.getEntity()))
                le.damage(chainDmg, attacker);
        });
    }
}
