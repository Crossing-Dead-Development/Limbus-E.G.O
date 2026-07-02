package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class SpecialContract extends BaseAccessory {
    public SpecialContract(GiftsModule plugin) {
        super(plugin, "special_contract", "特殊合約", "&7被動：力量 II 但緩慢 II");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        int strLevel = (int)(1 + (m - 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, strLevel, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1, true, false));
    }
}
