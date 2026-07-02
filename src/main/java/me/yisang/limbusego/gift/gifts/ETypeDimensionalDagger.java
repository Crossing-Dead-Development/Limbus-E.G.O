package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.Random;
public class ETypeDimensionalDagger extends BaseAccessory {
    private final Random rng = new Random();
    public ETypeDimensionalDagger(GiftsModule plugin) {
        super(plugin, "e_type_dimensional_dagger", "E型次元短劍", "&7攻擊時 25% 機率傳送到目標身後並造成額外傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (rng.nextDouble() >= 0.25 * m) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        Location behind = target.getLocation().subtract(target.getLocation().getDirection().multiply(1.5));
        behind.setY(target.getLocation().getY());
        behind.setYaw(target.getLocation().getYaw());
        behind.setPitch(target.getLocation().getPitch());
        attacker.teleport(behind);
        event.setDamage(event.getDamage() * 1.5 * m);
    }
}
