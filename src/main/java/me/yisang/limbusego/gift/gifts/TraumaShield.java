package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class TraumaShield extends BaseAccessory {
    public TraumaShield(GiftsModule plugin) {
        super(plugin, "trauma_shield", "精神屏蔽力場",
                "&#CBCBCB", "反精神力場盾",
                "受傷時：每 60 秒吸收一次傷害並獲得 2 SAN");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        double m = plugin.getUpgradeMultiplier(victim, getId());
        if (gate(victim, (long) (60000 / m))) {
            event.setDamage(0);
            plugin.sanity().gainSan(victim, 2);
        }
    }
}
