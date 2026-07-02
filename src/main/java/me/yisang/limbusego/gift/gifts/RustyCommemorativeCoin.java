package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Random;
public class RustyCommemorativeCoin extends BaseAccessory {
    private final Random rng = new Random();
    public RustyCommemorativeCoin(GiftsModule plugin) {
        super(plugin, "rusty_commemorative_coin", "生鏽的紀念幣", "&7擊殺時 35% 機率掉落額外金粒");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        double m = plugin.getUpgradeMultiplier(killer, getId());
        if (rng.nextDouble() < 0.35 * m)
            event.getEntity().getWorld().dropItemNaturally(
                event.getEntity().getLocation(), new ItemStack(Material.GOLD_NUGGET, 1 + (int)(m - 1)));
    }
}
