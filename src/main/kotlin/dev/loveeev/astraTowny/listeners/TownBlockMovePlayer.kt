package dev.loveeev.astraTowny.listeners;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.config.TranslateYML;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.objects.townblocks.WorldCoord;
import dev.loveeev.astratowny.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class TownBlockMovePlayer implements Listener {
    private final Map<Player, Town> playerTownMap = new HashMap<>();
    private final Map<Player, Boolean> sendTownMessageAlready = new HashMap<>();
    private final Map<Player, Boolean> actionBarActive = new HashMap<>();

    public TownBlockMovePlayer() {
        Bukkit.getPluginManager().registerEvents(this, Core.getInstance());
    }

    @EventHandler
    public void onBlockInteract(PlayerMoveEvent event) {
        Bukkit.getScheduler().runTask(Core.getInstance(), () -> handlePlayerMove(event));
    }

    private void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        WorldCoord currentCoord = new WorldCoord(location.getWorld(), location.getBlockX(), location.getBlockY());
        TownBlocks currentTownBlock = TownManager.getInstance().getTownBlock(currentCoord);

        // Пропустить, если игрок не перемещается между разными блоками
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Town currentTown = (currentTownBlock != null) ? currentTownBlock.getTown() : null;
        Town lastTown = playerTownMap.get(player);

        // Обновить сообщения и строку состояния, если город изменился
        if (currentTown != lastTown) {
            if (currentTown != null) {
                if (!sendTownMessageAlready.getOrDefault(player, false)) {
                    actionBarActive.put(player, true);
                    ChatUtil.sendTitle(player, TranslateYML.get(player).getString("enterTown").replace("{town}", currentTown.getName()),
                            TranslateYML.get(player).getString("subEnterTown").replace("{town}", currentTown.getName()));
                    sendTownMessageAlready.put(player, true);
                }
                ChatUtil.sendActionBar(player, TranslateYML.get(player).getString("actionBar").replace("{town}", currentTown.getName()));
            } else {
                actionBarActive.put(player, false);
                ChatUtil.sendActionBar(player, "");
            }

            // Обновить последний известный город игрока
            playerTownMap.put(player, currentTown);
        }
    }
}
