package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.Random;
public class Thunderbranch extends BaseAccessory {
    private final Random rng = new Random();
    public Thunderbranch(GiftsModule plugin) {
        super(plugin, "thunderbranch", "雷擊木", "&7攻擊時 10% 機率召喚閃電");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (rng.nextDouble() >= 0.10 * m) return;
        if (event.getEntity() instanceof LivingEntity target)
            target.getWorld().strikeLightningEffect(target.getLocation());
    }
}
