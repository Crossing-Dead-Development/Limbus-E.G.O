package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class Finifugality extends BaseAccessory {
    public Finifugality(GiftsModule plugin) {
        super(plugin, "finifugality", "留戀",
                "&7攻擊：獲得呼吸法 2·2｜呼吸法達 5 時額外獲得強壯 2·1");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        applyScaled(attacker, StatusEffect.POISE, 2, 2, attacker);
        if (pot(attacker, StatusEffect.POISE) >= 5) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker);
        }
    }
}
