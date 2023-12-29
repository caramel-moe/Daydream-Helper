package moe.caramel.daydream.helper.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * 월드 유틸리티 클래스
 */
@SuppressWarnings("unused")
public final class CoreWorldUtil {

    private CoreWorldUtil() { throw new UnsupportedOperationException(); }

    // ================================

    /**
     * 특정 범위 내에 블록이 있는지 확인합니다.
     *
     * @param world 대상 월드
     * @param box 확인할 범위
     * @return 만약 범위 내에 블록이 있다면 {@code false}를 반환
     */
    public static boolean noBlocksAround(@NotNull World world, @NotNull BoundingBox box) {
        final int minX = Mth.floor(box.getMinX());
        final int minY = Mth.floor(box.getMinY());
        final int minZ = Mth.floor(box.getMinZ());
        final int maxX = Mth.floor(box.getMaxX());
        final int maxY = Mth.floor(box.getMaxY());
        final int maxZ = Mth.floor(box.getMaxZ());

        final ServerLevel level = ((CraftWorld) world).getHandle();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int x = minX; x <= maxX; ++x) {
                    final BlockState type = level.getBlockStateIfLoaded(pos.set(x, y, z));
                    if (type != null && !type.isAir()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // ================================
}
