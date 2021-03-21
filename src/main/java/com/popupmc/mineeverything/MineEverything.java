package com.popupmc.mineeverything;

import com.popupmc.customtradeevent.CustomTradeEvent;
import dev.dbassett.skullcreator.SkullCreator;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MineEverything extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Setup events
        Bukkit.getPluginManager().registerEvents(this, this);

        netherPortalBlock = createBlock("Nether Portal Block", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBiZmMyNTc3ZjZlMjZjNmM2ZjczNjVjMmM0MDc2YmNjZWU2NTMxMjQ5ODkzODJjZTkzYmNhNGZjOWUzOWIifX19");
        endPortalBlock = createBlock("End Portal Block", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDBiZDFlYTZiMjY0YjdhMWNlYmY0N2ZiZWRlMTM1NThjN2QxZTFiMTY4NjA4ZTY1YjY1MmE5MTQ3Y2Y3ZjEyNSJ9fX0=");
        endGatewayBlock = createBlock("End Gateway Block", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQzZDI1MGUyNWJiY2EzYTYyYmU1YjNlZjAyY2ZjYWI2ZGNkYzQyNDg4NGM5YTdkNWNjOTVjOWQwIn19fQ==");

        getLogger().info("MineEverything is enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MineEverything is disabled");
    }

    public ItemStack createBlock(String name, String texture) {
        ItemStack item = SkullCreator.itemFromBase64(texture);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(name);
        item.setItemMeta(meta);
        item.setLore(lore);

        return item;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityAddToWorldEvent(CustomTradeEvent event) {

        // Must not be at spawn
        if (event.getRelayEvent().getEntity().getWorld().getName().equalsIgnoreCase("imperial_city"))
            return;

        // 10% chance
        if(random.nextInt(100 + 1) > 10)
            return;

        // Randomize result or 1t ingridient replace
        // Randomize reality or void portal
        int whichItem = random.nextInt(6);
        boolean isResultItem = random.nextInt(100 + 1) <= 50;

        // Get Reality or Void item
        ItemStack item;

        switch (whichItem) {
            case 0:
                item = netherPortalBlock.asOne().clone();
                break;
            case 1:
                item = endPortalBlock.asOne().clone();
                break;
            case 2:
                item = endGatewayBlock.asOne().clone();
                break;
            case 3:
                item = new ItemStack(Material.END_PORTAL_FRAME);
                break;
            case 4:
                item = new ItemStack(Material.BARRIER);
                break;
            default:
                item = new ItemStack(Material.BEDROCK);
        }

        getLogger().info("Villager Trade: Mine Everything " + item.getType());

        // Get current recipe
        MerchantRecipe recipe = event.getRelayEvent().getRecipe();

        // Replace result item if needs replacing
        ItemStack result = (isResultItem)
                ? item
                : recipe.getResult();

        // Ensure amount matches
        result.setAmount(recipe.getResult().getAmount());

        // Create new recipe thats a copy of the old one
        MerchantRecipe newRecipe = new MerchantRecipe(result,
                recipe.getUses(),
                recipe.getMaxUses(),
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier(),
                false);

        // get ingridients
        List<ItemStack> ings = recipe.getIngredients();

        // Stop if none, this is an error
        if(ings.size() == 0)
            return;

        // Set item as ing1 if not result
        if(!isResultItem) {
            item.setAmount(ings.get(0).getAmount());
            ings.set(0, item);
        }

        // Update new recipe with modified ings
        newRecipe.setIngredients(ings);

        // Set recipe
        event.getRelayEvent().setRecipe(newRecipe);
    }

    @EventHandler
    public void onBlockDamageEvent(BlockDamageEvent e) {
        Block block = e.getBlock();

        if(e.isCancelled())
            return;

        Material material = block.getType();

        // We're only interested in bedrock
        if(material != Material.NETHER_PORTAL &&
                material != Material.END_PORTAL &&
                material != Material.END_GATEWAY &&
                material != Material.END_CRYSTAL &&
                material != Material.END_PORTAL_FRAME &&
                material != Material.BARRIER)
            return;

        Player p = e.getPlayer();
        Material mainHand = p.getInventory().getItemInMainHand().getType();

        // Requires an axe of any kind in your hand
        if(mainHand != Material.WOODEN_AXE &&
                mainHand != Material.STONE_AXE &&
                mainHand != Material.IRON_AXE &&
                mainHand != Material.GOLDEN_AXE &&
                mainHand != Material.DIAMOND_AXE &&
                mainHand != Material.NETHERITE_AXE)
            return;

        // Allow to break instantly
        e.setInstaBreak(true);

        // Break naturally
        block.breakNaturally(p.getInventory().getItemInMainHand(), true);

        // Drop item at players feet
        if(material == Material.END_PORTAL)
            p.getWorld().dropItemNaturally(p.getLocation(), endPortalBlock);
        else if(material == Material.END_GATEWAY)
            p.getWorld().dropItemNaturally(p.getLocation(), endGatewayBlock);
        else if(material == Material.NETHER_PORTAL)
            p.getWorld().dropItemNaturally(p.getLocation(), netherPortalBlock);
        else
            p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(material));

        // Set as air
        block.setType(Material.AIR);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Do nothing if canceled
        if(event.isCancelled())
            return;

        ItemStack item = event.getItemInHand();

        if(item.asOne().isSimilar(netherPortalBlock.asOne())) {
            Block block = event.getBlock();
            Player player = event.getPlayer();

            new BukkitRunnable() {
                @Override
                public void run() {
                    BlockFace face = player.getFacing();
                    Axis axis = Axis.Z;

                    if(face == BlockFace.NORTH ||
                            face == BlockFace.SOUTH
                    )
                        axis = Axis.X;

                    block.setType(Material.AIR);
                    block.setType(Material.NETHER_PORTAL);

                    Orientable data = (Orientable)block.getBlockData();
                    data.setAxis(axis);
                    block.setBlockData(data);
                }
            }.runTaskLater(this, 1);
        }

        else if(item.asOne().isSimilar(endPortalBlock.asOne())) {
            Block block = event.getBlock();

            new BukkitRunnable() {
                @Override
                public void run() {
                    block.setType(Material.AIR);
                    block.setType(Material.END_PORTAL);
                }
            }.runTaskLater(this, 1);
        }

        else if(item.asOne().isSimilar(endGatewayBlock.asOne())) {
            Block block = event.getBlock();

            new BukkitRunnable() {
                @Override
                public void run() {
                    block.setType(Material.AIR);
                    block.setType(Material.END_GATEWAY);
                }
            }.runTaskLater(this, 1);
        }
    }

    public ItemStack netherPortalBlock;
    public ItemStack endPortalBlock;
    public ItemStack endGatewayBlock;

    public static final Random random = new Random();
}
