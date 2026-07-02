package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class SourLiquorAroma extends BaseAccessory {
    public SourLiquorAroma(GiftsModule plugin) {
        super(plugin, "sour_liquor_aroma", "酸味的酒香",
                "&#00BC6B", "\"原來不是客人，而是惡客啊。\"",
                "被動：5 格以內的敵人持續受虛弱 I");
    }
    @Override public void onPassiveTick(Player player) {
        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            if (e instanceof LivingEntity living && !(e instanceof Player)) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, true, false));
            }
        }
    }
}
