package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class JinGangBolus extends BaseAccessory {
    public JinGangBolus(GiftsModule plugin) {
        super(plugin, "jin_gang_bolus", "金剛丸",
                "&#9F8B07", "色澤上佳的丸。",
                "被動：吸收 I｜每 5 秒獲得守護 2·3");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 30, 0, true, false));
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.PROTECTION, 2, 3, player);
    }
}
