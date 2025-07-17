package moe.caramel.daydream.helper.core;

import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import java.util.function.Predicate;

/**
 * 광선 추적 헬퍼 클래스
 */
@ApiStatus.NonExtendable
@SuppressWarnings("unused")
public interface CoreRayHelper {

    /**
     * 발사체의 충돌 결과를 가져옵니다.
     *
     * @param position 충돌 확인 위치
     * @param velocity 발사체의 속도
     * @param bb 발사체의 경계 상자
     * @param filter 엔티티 필터
     * @return 충돌 결과
     */
    @Nullable
    static RayTraceResult getHitResult(final Location position, final Vector velocity, final BoundingBox bb,
                                       final Predicate<Entity> filter
    ) {
        return CoreRayHelper.getHitResult(position, velocity, bb, filter, 1.0D);
    }

    /**
     * 발사체의 충돌 결과를 가져옵니다.
     *
     * @param position 충돌 확인 위치
     * @param velocity 발사체의 속도
     * @param bb 발사체의 경계 상자
     * @param filter 엔티티 필터
     * @param inflate 경계 상자 확장 크기
     * @return 충돌 결과
     */
    @Nullable
    static RayTraceResult getHitResult(final Location position, final Vector velocity, final BoundingBox bb,
                                       final Predicate<Entity> filter, final double inflate
    ) {
        final Level nmsLevel = ((CraftWorld) position.getWorld()).getHandle();
        final Vec3 nmsPos = CraftLocation.toVec3(position);
        final Vec3 nmsVel = CraftVector.toVec3(velocity);
        final AABB nmsBb = new AABB(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());

        HitResult result;
        Vec3 end = nmsPos.add(nmsVel);

        // Search Block
        final HitResult blockHitRes = nmsLevel.clip(new ClipContext(
            nmsPos, end, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, CollisionContext.empty()));

        result = blockHitRes;
        if (blockHitRes.getType() != HitResult.Type.MISS) {
            end = blockHitRes.getLocation();
        }

        // Search Entity
        final HitResult entityHitRes = ProjectileUtil.getEntityHitResult(
            nmsLevel, null, nmsPos, end,
            nmsBb.expandTowards(nmsVel).inflate(inflate),
            entity -> filter.test(entity.getBukkitEntity()));

        if (entityHitRes != null) {
            result = entityHitRes;
        }

        return CraftRayTraceResult.convertFromInternal(nmsLevel, result);
    }
}
