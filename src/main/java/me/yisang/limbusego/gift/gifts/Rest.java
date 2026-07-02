package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Rest extends BaseAccessory {
    public Rest(GiftsModule plugin) {
        super(plugin, "rest", "安息",
                "&#FFFFFF", "\"一口棺材被孤單地放置於此。\"",
                "被動：靜止時生命再生 I");
    }
    @Override public void onPassiveTick(Player player) {
        if (player.getVelocity().length() < 0.05) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
        }
    }
}
