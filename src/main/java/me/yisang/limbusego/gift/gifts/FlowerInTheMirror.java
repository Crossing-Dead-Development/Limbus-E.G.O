package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class FlowerInTheMirror extends BaseAccessory {
    public FlowerInTheMirror(GiftsModule plugin) {
        super(plugin, "flower_in_the_mirror", "鏡中花",
                "&7攻擊：施加破裂 2·2｜被動：每 5 秒對 5 格內敵人施加破裂 2·1");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker);
    }
    @Override public void onPassiveTick(Player player) {
        if (!gate(player, 5000)) return;
        double m = plugin.getUpgradeMultiplier(player, getId());
        for (var e : player.getLocation().getNearbyLivingEntities(5 * m)) {
            if (e.equals(player) || e instanceof Player) continue;
            apply(e, StatusEffect.RUPTURE, 2, 1, player);
        }
    }
}
