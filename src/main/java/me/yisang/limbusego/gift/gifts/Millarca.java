package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Millarca extends BaseAccessory {
    public Millarca(GiftsModule plugin) {
        super(plugin, "millarca", "蜜拉卡", "&7攻擊時偷取 3 HP（強力吸血）");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + 3.0 * m));
    }
}
