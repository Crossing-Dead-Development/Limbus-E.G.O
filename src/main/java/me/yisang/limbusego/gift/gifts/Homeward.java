package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Objects;
public class Homeward extends BaseAccessory {
    public Homeward(GiftsModule plugin) {
        super(plugin, "homeward", "歸途",
                "&#8EC58E", "\"在我奪走你的家之前，帶著它離開。快。\"",
                "被動：血量高於 80% 時生命再生 II");
    }
    @Override public void onPassiveTick(Player player) {
        double max = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        if (player.getHealth() > max * 0.8) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1, true, false));
        }
    }
}
