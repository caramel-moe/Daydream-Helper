package moe.caramel.daydream.helper.core;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * 엔티티를 제어하는 유틸리티 클래스
 */
@SuppressWarnings("unused")
public final class CorePlayerUtil {

    private CorePlayerUtil() { throw new UnsupportedOperationException(); }

    /**
     * 플레이어를 새로고침 합니다.
     *
     * @param player 대상 플레이어
     * @param targets 새로고침을 수행할 플레이어 목록
     * @param action 플레이어별 추적 해제 이후 실행할 작업
     */
    public static void refreshPlayer(@NotNull Player player, @NotNull Collection<? extends Player> targets, @NotNull Consumer<Player> action) {
        final ServerPlayer sPlayer = ((CraftPlayer) player).getHandle();
        final ChunkMap tracker = sPlayer.getLevel().getChunkSource().chunkMap;
        for (final Player target : targets) {
            final ChunkMap.TrackedEntity entry = tracker.entityMap.get(target.getEntityId());

            // 언트래킹
            if (entry != null) {
                entry.removePlayer(sPlayer);
            }

            // 개발자의 작업 수행
            action.accept(target);

            // 리트래킹
            if (entry != null && !entry.seenBy.contains(sPlayer.connection)) {
                entry.updatePlayer(sPlayer);
            }
        }
    }
}
