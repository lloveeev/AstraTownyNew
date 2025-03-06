package dev.loveeev.astraTowny.commands;

import dev.loveeev.astraTowny.Core;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToggleClaimCommand implements TabExecutor, Listener {

    // Хранение задачи для каждого игрока
    private final Map<Player, Boolean> claimingEnabled = new HashMap<>();
    private final Map<Player, Chunk> lastChunk = new HashMap<>();
    private final Map<Player, String[]> playerArgs = new HashMap<>();


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only.");
            return false;
        }

        if (args.length != 2) {
            player.sendMessage("Usage: /toggleclaim <on/off> claim/unclaim");
            return false;
        }

        if (args[0].equalsIgnoreCase("on")) {
            startClaimingTask(player, args);
            player.sendMessage("Claiming toggle enabled.");
        } else if (args[0].equalsIgnoreCase("off")) {
            stopClaimingTask(player);
            player.sendMessage("Claiming toggle disabled.");
        } else {
            player.sendMessage("Usage: /toggleclaim <on/off> claim/unclaim");
        }

        return true;
    }

    private void startClaimingTask(Player player, String[] args) {
        if (claimingEnabled.getOrDefault(player, false)) {
            player.sendMessage("Claiming is already enabled.");
            return;
        }

        claimingEnabled.put(player, true);
        lastChunk.put(player, player.getLocation().getChunk());
        playerArgs.put(player, args); // Сохраняем аргументы
    }


    private void stopClaimingTask(Player player) {
        if (claimingEnabled.remove(player) == null) {
            player.sendMessage("Claiming is not enabled.");
        } else {
            lastChunk.remove(player);
            playerArgs.remove(player); // Удаляем сохраненные аргументы
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!claimingEnabled.getOrDefault(player, false)) return;

        Chunk currentChunk = player.getLocation().getChunk();
        Chunk previousChunk = lastChunk.get(player);

        // Выполняем команду только если игрок перешел в новый чанк
        if (!currentChunk.equals(previousChunk)) {
            lastChunk.put(player, currentChunk);

            String[] args = playerArgs.get(player); // Получаем сохраненные аргументы
            if (args == null) return; // На случай, если args не найдены

            // Выполнение команды в зависимости от выбранного режима
            if (args[1].equalsIgnoreCase("claim")) {
                player.performCommand("t claim");
            } else if (args[1].equalsIgnoreCase("unclaim")) {
                player.performCommand("t unclaim");
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("on", "off");
    }
}
