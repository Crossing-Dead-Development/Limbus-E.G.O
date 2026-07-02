package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class CQCManual extends BaseAccessory {
    public CQCManual(GiftsModule plugin) {
        super(plugin, "cqc_manual", "近身格鬥手冊", "&7被動：力量 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false));
    }
}