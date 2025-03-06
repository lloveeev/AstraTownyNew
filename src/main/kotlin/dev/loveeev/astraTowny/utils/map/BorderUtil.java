package dev.loveeev.astraTowny.utils.map;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.objects.townblocks.WorldCoord;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astraTowny.utils.Translatable;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris H (Zren / Shade)
 *         Date: 4/15/12
 */
public class BorderUtil {

    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    @ApiStatus.Internal
    public static @NotNull FloodfillResult getFloodFillableCoords(final @NotNull Town town, final @NotNull WorldCoord origin) {
        World originWorld = origin.getBukkitWorld();
        if (origin.hasTownBlock())
            return FloodfillResult.fail(Translatable.of("msg_err_floodfill_not_in_wild"));

        // Filter out any coords not in the same world
        final Set<WorldCoord> coords = new HashSet<>(town.getTownBlocks().keySet());
        coords.removeIf(coord -> {
            assert originWorld != null;
            return !originWorld.equals(coord.getBukkitWorld());
        });
        if (coords.isEmpty())
            return FloodfillResult.fail(null);

        int minX = origin.getX();
        int maxX = origin.getX();
        int minZ = origin.getZ();
        int maxZ = origin.getZ();

        // Establish a min and max X & Z to avoid possibly looking very far
        for (final WorldCoord coord : coords) {
            minX = Math.min(minX, coord.getX());
            maxX = Math.max(maxX, coord.getX());
            minZ = Math.min(minZ, coord.getZ());
            maxZ = Math.max(maxZ, coord.getZ());
        }

        final Set<WorldCoord> valid = new HashSet<>();
        final Set<WorldCoord> visited = new HashSet<>();

        final Queue<WorldCoord> queue = new LinkedList<>();
        queue.offer(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            final WorldCoord current = queue.poll();

            valid.add(current);

            for (final int[] direction : DIRECTIONS) {
                final int xOffset = direction[0];
                final int zOffset = direction[1];

                final WorldCoord candidate = current.add(xOffset, zOffset);

                if (!coords.contains(candidate) && (candidate.getX() >= maxX || candidate.getX() <= minX || candidate.getZ() >= maxZ || candidate.getZ() <= minZ)) {
                    return FloodfillResult.oob();
                }

                final TownBlocks townBlock = TownManager.getInstance().getTownBlock(candidate);

                // Fail if we're touching another town
                if (townBlock != null && townBlock.getTown() != null && !town.equals(townBlock.getTown())) {
                    return FloodfillResult.fail(Translatable.of("msg_err_floodfill_cannot_contain_towns"));
                }

                if (townBlock == null && !visited.contains(candidate) && !coords.contains(candidate)) {
                    queue.offer(candidate);
                    visited.add(candidate);
                }
            }
        }

        return FloodfillResult.success(valid);
    }

    public record FloodfillResult(@NotNull Type type, @Nullable Translatable feedback, @NotNull Collection<WorldCoord> coords) {
        public enum Type {
            SUCCESS,
            FAIL,
            OUT_OF_BOUNDS
        }

        static FloodfillResult fail(final @Nullable Translatable feedback) {
            return new FloodfillResult(Type.FAIL, feedback, Collections.emptySet());
        }

        static FloodfillResult oob() {
            return new FloodfillResult(Type.OUT_OF_BOUNDS, Translatable.of("msg_err_floodfill_out_of_bounds"), Collections.emptySet());
        }

        static FloodfillResult success(final Collection<WorldCoord> coords) {
            return new FloodfillResult(Type.SUCCESS, null, coords);
        }
    }
}
