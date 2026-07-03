package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class SpicebushBranch extends BaseAccessory {
    public SpicebushBranch(GiftsModule plugin) {
        super(plugin, "spicebush_branch", "檀香梅枝",
                "&#FFF29B", "我看不見花朵，可我聞到花香。\n花香盛放，我在那兒挖起墳墓。",
                "被動：中毒時轉化為回血效果｜每 5 秒獲得迅捷 2·3");
    }
    @Override public void onPassiveTick(Player player) {
        if (player.hasPotionEffect(PotionEffectType.POISON)) {
            player.removePotionEffect(PotionEffectType.POISON);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, true));
        }
        if (gate(player, 5000)) {
            applyScaled(player, StatusEffect.HASTE, 2, 3, player);
        }
    }
}
