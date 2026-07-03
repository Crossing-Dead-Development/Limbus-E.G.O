package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.Random;
public class Thunderbranch extends BaseAccessory {
    private final Random rng = new Random();
    public Thunderbranch(GiftsModule plugin) {
        super(plugin, "thunderbranch", "雷擊木",
                "&7攻擊：施加破裂 2·2；10% 機率召喚閃電並追加破裂 2·1");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        apply(target, StatusEffect.RUPTURE, 2, 2, attacker);
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (rng.nextDouble() < Math.min(1.0, 0.10 * m)) {
            target.getWorld().strikeLightningEffect(target.getLocation());
            apply(target, StatusEffect.RUPTURE, 2, 1, attacker);
        }
    }
}
