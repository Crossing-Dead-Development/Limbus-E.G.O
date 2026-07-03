package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class DuelingManualBook3 extends BaseAccessory {
    public DuelingManualBook3(GiftsModule plugin) {
        super(plugin, "dueling_manual_book_3", "決鬥教材第3冊",
                "&7被動：每 5 秒獲得強壯 2·2｜受擊：25% 機率獲得迅捷 2·3");
    }
    @Override public void onPassiveTick(Player player) {
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.POWER, 2, 2, player);
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        double m = plugin.getUpgradeMultiplier(victim, getId());
        if (Math.random() < Math.min(1.0, 0.25 * m)) {
            apply(victim, StatusEffect.HASTE, 2, 3, victim);
        }
    }
}
