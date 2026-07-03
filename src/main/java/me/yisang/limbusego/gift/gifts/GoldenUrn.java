package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
public class GoldenUrn extends BaseAccessory {
    public GoldenUrn(GiftsModule plugin) {
        super(plugin, "golden_urn", "金甕",
                "&#DA8F24", "擁抱本身即是接納他人。",
                "擊殺時：15% 機率複製目標掉落物");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        double m = plugin.getUpgradeMultiplier(killer, getId());
        if (Math.random() >= 0.15 * m) return;
        for (ItemStack drop : event.getDrops()) {
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), drop.clone());
        }
    }
}
