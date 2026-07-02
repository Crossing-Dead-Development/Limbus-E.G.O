package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Keenbranch extends BaseAccessory {
    public Keenbranch(GiftsModule plugin) {
        super(plugin, "keenbranch", "磨尖的樹枝", "&7攻擊時：15% 機率傷害提升 50%");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (Math.random() < 0.15) {
            event.setDamage(event.getDamage() * 1.5);
        }
    }
}
