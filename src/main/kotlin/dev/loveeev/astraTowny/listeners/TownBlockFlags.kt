package dev.loveeev.astraTowny.listeners;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Town;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TownBlockFlags implements Listener {
    public TownBlockFlags() {
        Bukkit.getPluginManager().registerEvents(this, Core.getInstance());
    }

    @EventHandler
    public void onExplosionTown(EntityExplodeEvent event) {
        Location location = event.getLocation();
        Town town = getTownByChunk(location);

        if (town != null && town.getEventStatus(Town.PermsType.EXPLOSION)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvpInTown(EntityDamageByEntityEvent event) {
        Location location = event.getEntity().getLocation();
        Town town = getTownByChunk(location);


        if (town != null && town.getEventStatus(Town.PermsType.PVP)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireInTown(BlockIgniteEvent event) {
        Location chunk = event.getBlock().getLocation();
        Town town = getTownByChunk(chunk);

        if (town != null && town.getEventStatus(Town.PermsType.FIRE)) {
            event.setCancelled(true);
        }
    }

    public Town getTownByChunk(Location chunk) {
        return TownManager.getInstance().getTownByChunk(TownManager.getInstance().getTownBlock(chunk));
    }
}
