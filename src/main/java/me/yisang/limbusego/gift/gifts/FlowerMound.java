package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class FlowerMound extends BaseAccessory {
    public FlowerMound(GiftsModule plugin) {
        super(plugin, "flower_mound", "花塚",
                "&#F1B1B1", "爾今死去儂收葬，未卜儂身何日喪？\n儂今葬花人笑癡，他日葬儂知是誰？",
                "被動：生命再生 I");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
    }
}
