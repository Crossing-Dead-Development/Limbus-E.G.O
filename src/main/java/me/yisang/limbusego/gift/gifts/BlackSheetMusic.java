package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class BlackSheetMusic extends BaseAccessory {
    public BlackSheetMusic(GiftsModule plugin) {
        super(plugin, "black_sheet_music", "黑色樂譜",
                "&#FFFFFF", "這首樂章將會穿透你的靈魂。",
                "被動：急迫 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 30, 0, true, false));
    }
}
