package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
public class Hardship extends BaseAccessory {
    public Hardship(GiftsModule plugin) {
        super(plugin, "hardship", "苦難",
                "&#9E9E41", "在這不虔敬的空間裡，連這是否算祈禱都不得而知。",
                "SAN≥40 時攻擊：獲得強壯 2·1｜擊殺：獲得 2 SAN");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (plugin.sanity().getSan(attacker) >= 40) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker);
        }
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        plugin.sanity().gainSan(killer, 2);
    }
}
