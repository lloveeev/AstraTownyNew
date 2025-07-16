package dev.loveeev.astratowny.commands;

import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Town;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TownyLists {
    private static final int NATIONS_PER_PAGE = 10;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public void handleListCommand(Player player, String[] args) {
        UUID playerUUID = player.getUniqueId();

        ConcurrentHashMap<UUID, Town> townsMap = TownManager.INSTANCE.getTowns();
        List<Town> towns = new ArrayList<>(townsMap.values());

        towns.sort(Comparator.comparing(t -> t.getName().toLowerCase()));

        int currentPage = playerPages.getOrDefault(playerUUID, 1);
        int totalPages = (int) Math.ceil((double) towns.size() / NATIONS_PER_PAGE);

        if (args.length == 2) {
            String arg = args[1].toLowerCase();
            if (arg.equals("next") && currentPage < totalPages) {
                currentPage++;
            } else if (arg.equals("prev") && currentPage > 1) {
                currentPage--;
            }
        }

        playerPages.put(playerUUID, currentPage);
        displayTowns(player, towns, currentPage, totalPages);
    }

    private void displayTowns(Player player, List<Town> towns, int currentPage, int totalPages) {
        int startIndex = (currentPage - 1) * NATIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + NATIONS_PER_PAGE, towns.size());

        player.sendMessage(ChatColor.YELLOW + "Города (Страница " + currentPage + " из " + totalPages + "):");

        for (int i = startIndex; i < endIndex; i++) {
            Town town = towns.get(i);
            TextComponent townComponent = new TextComponent(ChatColor.GRAY + " - " + town.getName());
            townComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Нажмите, чтобы выбрать " + town.getName()).create()));
            townComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t " + town.getName()));
            player.spigot().sendMessage(townComponent);
        }

        ComponentBuilder navigation = new ComponentBuilder();

        if (currentPage > 1) {
            TextComponent prevPage = new TextComponent(ChatColor.GREEN + " [<-]");
            prevPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Предыдущая страница").create()));
            prevPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t list prev"));
            navigation.append(prevPage);
        }

        if (currentPage < totalPages) {
            TextComponent nextPage = new TextComponent(ChatColor.GREEN + " [->]");
            nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Следующая страница").create()));
            nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t list next"));
            if (currentPage > 1) {
                navigation.append(" ");
            }
            navigation.append(nextPage);
        }

        player.spigot().sendMessage(navigation.create());
    }

}
