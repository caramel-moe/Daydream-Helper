package moe.caramel.daydream.helper.core;

import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftLocation;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftRayTraceResult;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftVector;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import java.util.function.Predicate;

/**
 * 광선 추적 유틸리티 클래스
 */
@SuppressWarnings("unused")
public final class CoreRayUtil {

    private CoreRayUtil() {
        throw new UnsupportedOperationException();
    }

    // ================================

    /**
     * 발사체의 충돌 결과를 가져옵니다.
     *
     * @param world 대상 월드
     * @param position 충돌 확인 위치
     * @param velocity 발사체의 속도
     * @param bb 발사체의 경계 상자
     * @param filter 엔티티 필터
     * @return 충돌 결과
     */
    @NotNull
    public static RayTraceResult getHitResult(
        @NotNull World world, @NotNull Location position,
        @NotNull Vector velocity, @NotNull BoundingBox bb, @NotNull Predicate<Entity> filter
    ) {
        final Level nmsLevel = ((CraftWorld) world).getHandle();
        final Vec3 nmsPos = CraftLocation.toVec3D(position);
        final Vec3 nmsVel = CraftVector.toNMS(velocity);
        final AABB nmsBb = new AABB(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());

        HitResult result;
        Vec3 end = nmsPos.add(nmsVel);

        // Search Block
        final HitResult blockHitRes = nmsLevel.clip(new ClipContext(
            nmsPos, end, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, null
        ));
        result = blockHitRes;
        if (blockHitRes.getType() != HitResult.Type.MISS) {
            end = blockHitRes.getLocation();
        }

        // Search Entity
        final HitResult entityHitRes = ProjectileUtil.getEntityHitResult(
            nmsLevel, null, nmsPos, end,
            nmsBb.expandTowards(nmsVel).inflate(1.0D),
            entity -> filter.test(entity.getBukkitEntity())
        );
        if (entityHitRes != null) {
            result = entityHitRes;
        }

        return CraftRayTraceResult.fromNMS(world, result);
    }
}
