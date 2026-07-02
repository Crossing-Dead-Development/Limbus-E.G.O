package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class DuelingManualBook3 extends BaseAccessory {
    public DuelingManualBook3(GiftsModule plugin) {
        super(plugin, "dueling_manual_book_3", "決鬥教材第3冊", "&7受傷時：30% 機率格擋 50% 傷害");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        if (Math.random() < 0.30) {
            event.setDamage(event.getDamage() * 0.5);
        }
    }
}