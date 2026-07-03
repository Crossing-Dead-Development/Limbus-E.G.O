package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.Random;
public class ETypeDimensionalDagger extends BaseAccessory {
    private final Random rng = new Random();
    public ETypeDimensionalDagger(GiftsModule plugin) {
        super(plugin, "e_type_dimensional_dagger", "E型次元短劍",
                "&7攻擊：獲得充能 2·2｜25% 機率瞬移背刺（額外傷害）並改獲充能 4·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (rng.nextDouble() < Math.min(1.0, 0.25 * m)) {
            Location behind = target.getLocation().subtract(target.getLocation().getDirection().multiply(1.5));
            behind.setY(target.getLocation().getY());
            behind.setYaw(target.getLocation().getYaw());
            behind.setPitch(target.getLocation().getPitch());
            attacker.teleport(behind);
            event.setDamage(event.getDamage() * 1.5 * m);
            apply(attacker, StatusEffect.CHARGE, 4, 2, attacker);
        } else {
            apply(attacker, StatusEffect.CHARGE, 2, 2, attacker);
        }
    }
}
