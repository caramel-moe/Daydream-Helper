package moe.caramel.daydream.helper.core;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
     * @param player 플레이어
     * @param targets 새로고침을 수행할 대상 플레이어 목록
     * @param action 대상 플레이어별 추적 해제 이후 실행할 작업
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

    /**
     * 플레이어의 리스트 이름을 정직하게 가져옵니다.
     *
     * @param player 대상 플레이어
     * @return 플레이어 리스트 이름
     */
    @Nullable
    public static Component playerListName(@NotNull Player player) {
        final ServerPlayer sPlayer = ((CraftPlayer) player).getHandle();
        return sPlayer.getTabListDisplayName() == null ? null : PaperAdventure.asAdventure(sPlayer.listName);
    }

    /**
     * 플레이어의 리스트 이름을 업데이트 없이 변경합니다.
     *
     * @param player 대상 플레이어
     * @param name 리스트 이름
     */
    public static void playerListNameNonUpdate(@NotNull Player player, @Nullable Component name) {
        final ServerPlayer sPlayer = ((CraftPlayer) player).getHandle();
        sPlayer.listName = (name == null) ? null : PaperAdventure.asVanilla(name);
    }
}
