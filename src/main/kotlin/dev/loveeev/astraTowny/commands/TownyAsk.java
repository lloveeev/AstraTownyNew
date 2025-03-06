package dev.loveeev.astraTowny.commands;

import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.utils.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TownyAsk implements TabExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только в игре.");
            return true;
        }
        if(args.length == 0){
            ChatUtil.getInstance().sendSuccessNotification(player, Messages.asksend(player));
            for(ConcurrentHashMap.Entry<UUID,Resident> entry : TownManager.getInstance().getResidents().entrySet()){
                Resident resident = entry.getValue();
                if(resident.hasTownPermission("ASTRATOWN_TOWN_INVITE")){
                    TextComponent message = new TextComponent(ChatUtil.getInstance().colorize(Messages.confirmation(player)));
                    TextComponent confirmCommand = new TextComponent(ChatColor.GREEN + "[/t invite " + player.getName() + " ]");
                    confirmCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t invite " + player.getName()));
                    confirmCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.confirmation(player)).create()));
                    message.addExtra(confirmCommand);
                    resident.getPlayer().spigot().sendMessage(ChatMessageType.CHAT, message);
                }
            }
        }
        if(args.length == 1){
            if(TownManager.getInstance().getTown(args[0]) == null){
                ChatUtil.getInstance().sendSuccessNotification(player,Messages.townexist(player));
                return true;
            }
            ChatUtil.getInstance().sendSuccessNotification(player, Messages.asksendtown(player));
            for(Resident resident : TownManager.getInstance().getTown(args[0]).getResidents()){
                if(resident.hasTownPermission("ASTRATOWN_TOWN_INVITE")){
                    TextComponent message = new TextComponent(ChatUtil.getInstance().colorize(Messages.confirmation(player)));
                    TextComponent confirmCommand = new TextComponent(ChatColor.GREEN + "[/t invite " + player.getName() + " ]");
                    confirmCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t invite " + player.getName()));
                    confirmCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.confirmation(player)).create()));
                    message.addExtra(confirmCommand);
                    resident.getPlayer().spigot().sendMessage(ChatMessageType.CHAT, message);
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return getPartialMatches(args[0], TownManager.getInstance().getTownNames());
        }
        return new ArrayList<>();
    }
    private List<String> getPartialMatches(String arg, List<String> options) {
        return options.stream().filter(option -> option.startsWith(arg)).collect(Collectors.toList());
    }
}
