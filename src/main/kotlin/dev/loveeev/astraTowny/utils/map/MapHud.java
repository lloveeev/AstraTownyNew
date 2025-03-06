package dev.loveeev.astraTowny.utils.map;
import dev.loveeev.astratowny.Core;
import dev.loveeev.astratowny.data.TownyMapData;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.objects.townblocks.WorldCoord;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astraTowny.utils.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
public class MapHud {

    /* Scoreboards use old-timey colours. */
    private static final ChatColor WHITE = ChatColor.WHITE;
    private static final ChatColor GOLD = ChatColor.GOLD;
    private static final ChatColor GREEN = ChatColor.GREEN;
    private static final ChatColor DARK_GREEN = ChatColor.DARK_GREEN;

    /* Scoreboards use Teams here is our team names.*/
    private static final String HUD_OBJECTIVE = "MAP_HUD_OBJ";
    private static final String TEAM_MAP_PREFIX = "mapTeam";
    private static final String TEAM_OWNER = "ownerTeam";
    private static final String TEAM_TOWN = "townTeam";

    private static int lineWidth = 19, lineHeight = 10;
    private static int halfLineWidth = lineWidth / 2;
    private static int halfLineHeight = lineHeight / 2;


    public static String mapHudTestKey() {
        return "mapTeam1";
    }


    public static Objective objective(Scoreboard board, @NotNull String name, @NotNull String displayName) {
        return board.registerNewObjective(name, "dummy", displayName);
    }

    public static void toggleOn(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = objective(board, HUD_OBJECTIVE, "maphud");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = lineHeight + 2;
        ChatColor[] colors = ChatColor.values();
        for (int i = 0; i < lineHeight; i++) {
            board.registerNewTeam(TEAM_MAP_PREFIX + i).addEntry(colors[i].toString());
            objective.getScore(colors[i].toString()).setScore(score);
            score--;
        }
        //хз проверять нужно
        String townEntry = DARK_GREEN + "_" + ": ";
        String ownerEntry = DARK_GREEN + "^" + ": ";

        board.registerNewTeam(TEAM_TOWN).addEntry(townEntry);
        objective.getScore(townEntry).setScore(2);

        board.registerNewTeam(TEAM_OWNER).addEntry(ownerEntry);
        objective.getScore(ownerEntry).setScore(1);

        player.setScoreboard(board);
        updateMap(player);
    }

    public static void updateMap(Player player) {
        updateMap(player, WorldCoord.parseWorldCoord(player));
    }

    public static void updateMap(Player player, WorldCoord wc) {
        Scoreboard board = player.getScoreboard();
        if (board.getObjective(HUD_OBJECTIVE) == null || wc.getBukkitWorld() == null) {
            toggleOff(player);
            return;
        }

        int wcX = wc.getX();
        int wcZ = wc.getZ();
        // Set the board title.
        String boardTitle = String.format("%sASTRATOWNY MAP %s(%s, %s)", GOLD, WHITE, wcX, wcZ);
        board.getObjective(HUD_OBJECTIVE).setDisplayName(boardTitle);

        // Populate our map into an array.
        String[][] map = new String[lineWidth][lineHeight];
        fillMapArray(wcX, wcZ, TownManager.getInstance().getResident(player.getName()), player.getWorld(), map);

        // Write out the map to the board.
        writeMapToBoard(board, map);
    }

    private static void fillMapArray(int wcX, int wcZ, Resident resident, World bukkitWorld, String[][] map) {
        int x, y = 0;
        for (int tby = wcX + (lineWidth - halfLineWidth - 1); tby >= wcX - halfLineWidth; tby--) {
            x = 0;
            for (int tbx = wcZ - halfLineHeight; tbx <= wcZ + (lineHeight - halfLineHeight - 1); tbx++) {
                final WorldCoord worldCoord = new WorldCoord(bukkitWorld, tby, tbx);
                if (worldCoord.hasTownBlock()) {
                    mapTownBlock(resident, map, x, y, worldCoord.getTownBlockOrNull());
                } else {
                    mapWilderness(map, x, y, worldCoord);
                }
                x++;
            }
            y++;
        }
    }

    static List<Player> permUsers = new ArrayList<>();
    static List<Player> mapUsers = new ArrayList<>();

    public static void toggleOff(final Player player) {
        Optional.ofNullable(Bukkit.getScoreboardManager()).ifPresent(manager -> player.setScoreboard(manager.getMainScoreboard()));
    }

    public static void toggleMapHud(Player player) {

        if (!mapUsers.contains(player)) {
            toggleAllOff(player);
            mapUsers.add(player);
            toggleOn(player);
        } else
            toggleAllOff(player);
    }
    public static void startScoreboardUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : mapUsers) {
                    updateMap(player); // Update scoreboard
                }
            }
        }.runTaskTimer(Core.getInstance(), 0L, 1); // 20 ticks = 1 second
    }

    public static void toggleAllOff(Player p) {
        permUsers.remove(p);
        mapUsers.remove(p);
        if (p.isOnline())
            toggleOff(p);
    }


    private static void mapTownBlock(Resident resident, String[][] map, int x, int y, final TownBlocks townBlock) {
        map[y][x] = getTownBlockColour(resident, x, y, townBlock);


        if (townBlock.isHomeBlock()) {
            map[y][x] += "H";
        } else{
            map[y][x] += "+";
        }
    }

    private static String getTownBlockColour(Resident resident, int x, int y, final TownBlocks townBlock) {
        if (playerLocatedAtThisCoord(x, y))
            // This is the player's location, colour it special.
            return dev.loveeev.astratowny.utils.Colors.Gold;
        else if (resident.hasTown())
            // The townblock could have a variety of colours.
            return getTownBlockColour(resident, townBlock.getTown());
        else
            // Default fallback.
            return dev.loveeev.astratowny.utils.Colors.White;
    }

    private static String getTownBlockColour(Resident resident, Town townAtTownBlock) {

        if (!resident.hasNation())
            return dev.loveeev.astratowny.utils.Colors.White;

        Nation resNation = resident.getNation();
        // Another town in the player's nation.
        if (resNation.hasTown(townAtTownBlock))
            return dev.loveeev.astratowny.utils.Colors.Green;

        return dev.loveeev.astratowny.utils.Colors.White;
    }

    private static boolean playerLocatedAtThisCoord(int x, int y) {
        return x == halfLineHeight && y == halfLineWidth;
    }

    private static void mapWilderness(String[][] map, int x, int y, final WorldCoord worldCoord) {
        // Colour gold if this is the player loc, otherwise normal gray.
        map[y][x] = playerLocatedAtThisCoord(x, y) ? dev.loveeev.astratowny.utils.Colors.Gold : Colors.Gray;

        String symbol;
        // Cached TownyMapData is present and not old.
        final dev.loveeev.astratowny.data.TownyMapData data = getWildernessMapDataMap().get(worldCoord);

        if (data != null && !data.isOld()) {
            dev.loveeev.astratowny.data.TownyMapData mapData = getWildernessMapDataMap().get(worldCoord);
            symbol = mapData.getSymbol();
            // Cached TownyMapData is either not present or was considered old.
        } else {
            symbol = "-";
            TextComponent hover = Component.text("pds", NamedTextColor.DARK_RED).append(Component.text(" (" + worldCoord.getX() + ", " + worldCoord.getZ() + ")", NamedTextColor.WHITE));
            getWildernessMapDataMap().put(worldCoord, new TownyMapData(worldCoord, symbol, hover, "/towny:townyworld"));

            Bukkit.getScheduler().runTaskLaterAsynchronously(Core.getInstance(), () -> {
                getWildernessMapDataMap().computeIfPresent(worldCoord, (key, cachedData) -> cachedData.isOld() ? null : cachedData);
            }, 20L * 35); // Задержка в 35 секундах (20 тиков в секунду)
        }

        map[y][x] += symbol;
    }

    private static Map<WorldCoord, TownyMapData> getWildernessMapDataMap() {
        return new ConcurrentHashMap<>();
    }

    private static void writeMapToBoard(Scoreboard board, String[][] map) {
        for (int my = 0; my < lineHeight; my++) {
            StringBuilder line = new StringBuilder();
            for (int mx = lineWidth - 1; mx >= 0; mx--)
                line.append(map[mx][my]);

            board.getTeam(TEAM_MAP_PREFIX + my).setSuffix(line.toString());
        }
    }
}