package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class CaskSpirits extends BaseAccessory {
    public CaskSpirits(GiftsModule plugin) {
        super(plugin, "cask_spirits", "桶裝烈酒",
                "&#A8BE78", "我從未想過沒有酒的生活。",
                "攻擊：獲得呼吸法 2·2｜自身呼吸法≥4時攻擊額外獲得 1 SAN");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        applyScaled(attacker, StatusEffect.POISE, 2, 2, attacker);
        if (pot(attacker, StatusEffect.POISE) >= 4) {
            plugin.sanity().gainSan(attacker, 1);
        }
    }
}
