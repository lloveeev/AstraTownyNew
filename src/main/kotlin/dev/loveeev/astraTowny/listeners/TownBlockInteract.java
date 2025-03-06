package dev.loveeev.astraTowny.listeners;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.objects.townblocks.WorldCoord;
import dev.loveeev.astratowny.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class TownBlockInteract implements Listener {


    public TownBlockInteract(){
        Bukkit.getPluginManager().registerEvents(this,Core.getInstance());
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(!TownManager.getInstance().getResident(player).hasTownPermission("OP_BUILD") || !player.isOp()){
            return;
        }
        if (event.getClickedBlock() != null) {
            Block chunk = Objects.requireNonNull(event.getClickedBlock());

            if (canPlayerInteract(player, chunk)) {
                event.setCancelled(true);
                ChatUtil.sendSuccessNotification(player, Messages.interactBlock(player));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if(!TownManager.getInstance().getResident(player).hasTownPermission("OP_BUILD") || !player.isOp()){
                return;
            }
            if (event.getEntity() instanceof Animals) {
                Block chunk = event.getEntity().getLocation().getBlock();
                if (canPlayerInteract(player, chunk)) {
                    event.setCancelled(true);
                    ChatUtil.sendSuccessNotification(player, Messages.interactBlock(player));
                }
            }
        }
    }




    private boolean canPlayerInteract(Player player, Block block) {
        TownBlocks townBlock = TownManager.getInstance().getTownBlock(new WorldCoord(block.getWorld(),block.getX(),block.getY()));

        if (townBlock != null) {
            Town town = townBlock.getTown();
            Town playerTown = TownManager.getInstance().getTown(player);

            return playerTown == null || !playerTown.equals(town);
        }

        return false;
    }
}
