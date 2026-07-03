package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class EmeraldElytra extends BaseAccessory {
    public EmeraldElytra(GiftsModule plugin) {
        super(plugin, "emerald_elytra", "綠色鞘翅",
                "&#16B569", "走到傘底下，等待雨停。",
                "被動：緩降｜疾跑中攻擊：獲得呼吸法 3·2");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (attacker.isSprinting()) {
            applyScaled(attacker, StatusEffect.POISE, 3, 2, attacker);
        }
    }
}
