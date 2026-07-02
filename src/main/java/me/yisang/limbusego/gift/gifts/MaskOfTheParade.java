package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class MaskOfTheParade extends BaseAccessory {
    public MaskOfTheParade(GiftsModule plugin) {
        super(plugin, "mask_of_the_parade", "遊行的面具",
                "&#9928BB", "\"只要稍微支付一點血液，就能將這一瞬間永遠儲存下來！\"",
                "被動：潛行時持續獲得隱身");
    }
    @Override public void onPassiveTick(Player player) {
        if (player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 0, true, false));
        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
}
