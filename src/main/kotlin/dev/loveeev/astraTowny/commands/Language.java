package dev.loveeev.astraTowny.commands;

import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.config.ConfigYML;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Language implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только в игре.");
            return true;
        }
        if(args.length < 1){
            ChatUtil.sendSuccessNotification(player,"/language <lang>");
            ChatUtil.sendSuccessNotification(player,"Select the desired language, and use /language to select:");
            for (String lang : ConfigYML.language){
                ChatUtil.sendSuccessNotification(player,lang);
            }
            ChatUtil.sendSuccessNotification(player,"You lang: " + TownManager.getInstance().getResident(player).getLanguage());
            return true;
        }
        Resident resident = TownManager.getInstance().getResident(player);
        if(ConfigYML.language.contains(args[0])){
            resident.setLanguage(args[0]);
            ChatUtil.sendSuccessNotification(player, Messages.language(player));
        }else {
            ChatUtil.sendSuccessNotification(player,"The language does not exist.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return getPartialMatches(args[0], ConfigYML.language);
        }
        return new ArrayList<>();
    }
    private List<String> getPartialMatches(String arg, List<String> options) {
        return options.stream().filter(option -> option.startsWith(arg)).collect(Collectors.toList());
    }


}
