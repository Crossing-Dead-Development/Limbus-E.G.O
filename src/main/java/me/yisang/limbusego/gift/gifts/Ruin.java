package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Ruin extends BaseAccessory {
    public Ruin(GiftsModule plugin) {
        super(plugin, "ruin", "破滅", "&7攻擊時：額外 4 傷害，但自身損失 1 生命值");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        event.setDamage(event.getDamage() + 4.0);
        if (attacker.getHealth() > 2.0) {
            attacker.damage(1.0);
        }
    }
}