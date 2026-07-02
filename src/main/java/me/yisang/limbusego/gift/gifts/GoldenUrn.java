package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
public class GoldenUrn extends BaseAccessory {
    public GoldenUrn(GiftsModule plugin) {
        super(plugin, "golden_urn", "金甕",
                "&#DA8F24", "擁抱本身即是接納他人。",
                "擊殺時：額外掉落 1 金粒");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        event.getEntity().getWorld().dropItemNaturally(
            event.getEntity().getLocation(), new ItemStack(Material.GOLD_NUGGET));
    }
}
