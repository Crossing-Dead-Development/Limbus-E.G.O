package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Lithograph extends BaseAccessory {
    public Lithograph(GiftsModule plugin) {
        super(plugin, "lithograph", "石板字符",
                "&#169876", "審判……定罪……懲戒……",
                "被動：所有傷害減少 10%");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        event.setDamage(event.getDamage() * 0.9);
    }
}
