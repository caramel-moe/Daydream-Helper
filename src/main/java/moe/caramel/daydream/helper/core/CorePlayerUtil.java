package moe.caramel.daydream.helper.core;

import com.mojang.datafixers.DataFixer;
import io.papermc.paper.adventure.PaperAdventure;
import moe.caramel.daydream.advancement.PlayerAdvancementData;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.advancement.CraftAdvancementProgress;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 엔티티를 제어하는 유틸리티 클래스
 */
@SuppressWarnings("unused")
public final class CorePlayerUtil {

    private CorePlayerUtil() { throw new UnsupportedOperationException(); }

    // ================================

    /**
     * 플레이어를 새로고침 합니다.
     *
     * @param player 플레이어
     * @param targets 새로고침을 수행할 대상 플레이어 목록
     * @param action 대상 플레이어별 추적 해제 이후 실행할 작업
     */
    public static void refreshPlayer(@NotNull Player player, @NotNull Collection<? extends Player> targets, @NotNull Consumer<Player> action) {
        final ServerPlayer sPlayer = ((CraftPlayer) player).getHandle();
        for (final Player target : targets) {
            final TrackedEntity entry = ((CraftPlayer) target).getHandle().moonrise$getTrackedEntity();

            // 언트래킹
            entry.removePlayer(sPlayer);

            // 개발자의 작업 수행
            action.accept(target);

            // 리트래킹
            if (!entry.seenBy.contains(sPlayer.connection)) {
                entry.updatePlayer(sPlayer);
            }
        }
    }

    /**
     * 플레이어를 새로고침 합니다.
     *
     * @param player 플레이어
     * @param targets 새로고침을 수행할 대상 플레이어 목록
     * @param action 대상 플레이어별 추적 해제 이후 실행할 작업
     */
    @ApiStatus.Experimental
    @SuppressWarnings("unchecked")
    public static void daydream$refreshPlayer(@NotNull Player player, @NotNull Collection<? extends Player> targets, @NotNull RefreshLooper action) {
        final ServerPlayer sPlayer = ((CraftPlayer) player).getHandle();
        for (final Player target : targets) {
            final ServerPlayer sTarget = ((CraftPlayer) target).getHandle();
            final TrackedEntity entry = sTarget.moonrise$getTrackedEntity();
            final boolean canSee = entry.seenBy.contains(sPlayer.connection);
            final List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();

            // 언트래킹
            if (canSee) {
                list.add(new ClientboundRemoveEntitiesPacket(sTarget.getId()));
            }

            // 개발자의 작업 수행
            action.loop(target, canSee, packet -> list.add((Packet<ClientGamePacketListener>) packet.getPacket()));

            // 리트래킹
            if (canSee) {
                entry.serverEntity.sendPairingData(sPlayer, list::add);
            }

            if (!list.isEmpty()) {
                sPlayer.connection.send(new ClientboundBundlePacket(list));
            }
        }
    }

    /**
     * 새로고침 작업을 수행할 때 사용되는 함수형 인터페이스
     */
    @FunctionalInterface
    public interface RefreshLooper {

        /**
         * 대상 플레이어별 추적 해제 이후 실행할 작업
         *
         * @param target 대상 플레이어
         * @param canSee 호출 플레이어가 대상 플레이어를 볼 수 있는지의 여부
         * @param packet 번들 패킷에 추가할 패킷이 있는 경우 사용
         */
        void loop(final @NotNull Player target, final boolean canSee, final @NotNull Consumer<moe.caramel.daydream.packet.Packet> packet);
    }

    // ================================

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

    // ================================

    private static final @Nullable Constructor<PlayerAdvancements> ADVANCEMENTS_CONSTRUCTOR;
    static {
        Constructor<PlayerAdvancements> constructor = null;
        try {
            @SuppressWarnings("all")
            final Constructor<PlayerAdvancements> cached = PlayerAdvancements.class.getConstructor(DataFixer.class, PlayerList.class, ServerAdvancementManager.class, Path.class, ServerPlayer.class, boolean.class);
            constructor = cached;
        } catch (final NoSuchMethodException ignored) {
        }
        ADVANCEMENTS_CONSTRUCTOR = constructor;
    }

    /**
     * 빈 플레이어 발전과제 데이터를 생성합니다.
     *
     * @param targetUuid 대상 플레이어의 UUID
     * @return 플레이어 발전과제 데이터
     */
    @NotNull
    public static PlayerAdvancementData createEmptyPlayerAdvancementData(@Nullable UUID targetUuid) {
        if (CorePlayerUtil.ADVANCEMENTS_CONSTRUCTOR == null) {
            throw new UnsupportedOperationException("지원하지 않는 시스템입니다.");
        }

        final MinecraftServer server = MinecraftServer.getServer();
        final DataFixer dataFixer = server.getFixerUpper();
        final PlayerList playerList = server.getPlayerList();
        final ServerAdvancementManager advancementLoader = server.getAdvancements();

        final UUID uuid = (targetUuid == null) ? UUID.randomUUID() : targetUuid;
        final Path filePath = server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(uuid + ".json");

        try {
            return (PlayerAdvancementData) ADVANCEMENTS_CONSTRUCTOR.newInstance(dataFixer, playerList, advancementLoader, filePath, null, false);
        } catch (final InvocationTargetException | IllegalAccessException | InstantiationException exception) {
            throw new RuntimeException("지원하지 않는 시스템입니다.", exception);
        }
    }

    /**
     * 발전과제 진행도를 가져옵니다.
     *
     * @param data 플레이어 발전과제 데이터
     * @param advancement 대상 발전과제
     * @return 발전과제 진행도
     */
    @NotNull
    public static AdvancementProgress getAdvancementProgress(@NotNull PlayerAdvancementData data, @NotNull Advancement advancement) {
        final CraftAdvancement craft = (CraftAdvancement) advancement;
        final PlayerAdvancements playerData = (PlayerAdvancements) data;
        final net.minecraft.advancements.AdvancementProgress progress = playerData.getOrStartProgress(craft.getHandle());

        return new CraftAdvancementProgress(craft, playerData, progress);
    }
}
