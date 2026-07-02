package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Carmilla extends BaseAccessory {
    public Carmilla(GiftsModule plugin) {
        super(plugin, "carmilla", "卡蜜拉", "&7攻擊時偷取 3 HP；日光下每秒損 1 HP");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 3.0 * m));
    }
    @Override public void onPassiveTick(Player player) {
        if (!player.getWorld().isDayTime()) return;
        if (player.getLocation().getBlock().getLightFromSky() < 13) return;
        if (player.getHealth() > 1.0)
            player.setHealth(Math.max(0.5, player.getHealth() - 1.0));
    }
}
