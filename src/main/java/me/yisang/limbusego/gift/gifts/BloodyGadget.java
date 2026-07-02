package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class BloodyGadget extends BaseAccessory {
    public BloodyGadget(GiftsModule plugin) {
        super(plugin, "bloody_gadget", "鮮血裝飾", "&7攻擊額外 +2 傷害，但自損 0.5 HP");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        event.setDamage(event.getDamage() + 2.0 * m);
        Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> {
            if (attacker.isOnline() && attacker.getHealth() > 0.5)
                attacker.setHealth(attacker.getHealth() - 0.5);
        });
    }
}
