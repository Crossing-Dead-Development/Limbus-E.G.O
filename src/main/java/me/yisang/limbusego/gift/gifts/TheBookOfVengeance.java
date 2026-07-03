package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class TheBookOfVengeance extends BaseAccessory {
    public TheBookOfVengeance(GiftsModule plugin) {
        super(plugin, "the_book_of_vengeance", "復仇帳簿",
                "&#B900FF", "加倍奉還！",
                "受擊：獲得強壯 2·2 與守護 1·2");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        applyScaled(victim, StatusEffect.POWER, 2, 2, victim);
        apply(victim, StatusEffect.PROTECTION, 1, 2, victim);
    }
}
