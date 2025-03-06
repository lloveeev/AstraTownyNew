package dev.loveeev.astraTowny.hooks;

import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Town;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "astratowny";
    }

    @Override
    public @NotNull String getAuthor() {
        return "loveeev";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("town")) {
            Resident resident = TownManager.getInstance().getResident(player);
            if (resident != null) {
                Town town = resident.getTown();
                if (town != null) {
                    return town.getName();
                } else {
                    return "";
                }
            }
            return "";
        }
        if (identifier.equals("nation")) {
            Resident resident = TownManager.getInstance().getResident(player);
            if (resident != null) {
                Nation nation = resident.getNation();
                if (nation != null) {
                    return nation.getName();
                } else {
                    return "";
                }
            }
            return "";
        }
        return null;
    }
}
