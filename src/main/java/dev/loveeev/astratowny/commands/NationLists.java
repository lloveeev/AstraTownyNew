package dev.loveeev.astratowny.commands;

import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class NationLists {
    private static final int NATIONS_PER_PAGE = 10;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public void handleListCommand(Player player, String[] args) {
        UUID playerUUID = player.getUniqueId();

        List<Nation> nations = new ArrayList<>(TownManager.INSTANCE.getNations().values());
        nations.sort(Comparator.comparing(n -> n.getName().toLowerCase()));

        int currentPage = playerPages.getOrDefault(playerUUID, 1);
        int totalPages = (int) Math.ceil((double) nations.size() / NATIONS_PER_PAGE);

        if (args.length == 2) {
            switch (args[1].toLowerCase()) {
                case "next" -> {
                    if (currentPage < totalPages) currentPage++;
                }
                case "prev" -> {
                    if (currentPage > 1) currentPage--;
                }
            }
        }

        playerPages.put(playerUUID, currentPage);
        displayNations(player, nations, currentPage, totalPages);
    }

    private void displayNations(Player player, List<Nation> nations, int currentPage, int totalPages) {
        int startIndex = (currentPage - 1) * NATIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + NATIONS_PER_PAGE, nations.size());

        player.sendMessage(ChatColor.YELLOW + "Нации (Страница " + currentPage + " из " + totalPages + "):");

        for (int i = startIndex; i < endIndex; i++) {
            Nation nation = nations.get(i);
            TextComponent component = new TextComponent(ChatColor.GRAY + " - " + nation.getName());
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Нажмите, чтобы выбрать " + nation.getName()).create()));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/n " + nation.getName()));
            player.spigot().sendMessage(component);
        }

        ComponentBuilder navigation = new ComponentBuilder();

        if (currentPage > 1) {
            TextComponent prevPage = new TextComponent(ChatColor.GREEN + " [<-]");
            prevPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Предыдущая страница").create()));
            prevPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nlist prev"));
            navigation.append(prevPage);
        }

        if (currentPage < totalPages) {
            if (currentPage > 1) navigation.append(" ");

            TextComponent nextPage = new TextComponent(ChatColor.GREEN + " [->]");
            nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Следующая страница").create()));
            nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nlist next"));
            navigation.append(nextPage);
        }

        player.spigot().sendMessage(navigation.create());
    }
}
