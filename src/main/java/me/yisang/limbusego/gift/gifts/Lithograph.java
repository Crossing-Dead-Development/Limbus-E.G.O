package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
public class Lithograph extends BaseAccessory {
    public Lithograph(GiftsModule plugin) {
        super(plugin, "lithograph", "石板字符",
                "&#169876", "審判……定罪……懲戒……",
                "擊殺時：回復生命與飽食度");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        double m = plugin.getUpgradeMultiplier(killer, getId());
        double max = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
        killer.setHealth(Math.min(max, killer.getHealth() + 2.0 * m));
        killer.setFoodLevel(Math.min(20, killer.getFoodLevel() + 2));
    }
}
