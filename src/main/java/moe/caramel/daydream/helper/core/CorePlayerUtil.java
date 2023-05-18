package moe.caramel.daydream.helper.core;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.BiomeManager;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        final ChunkMap tracker = sPlayer.getLevel().getChunkSource().chunkMap;
        for (final Player target : targets) {
            final int id = target.getEntityId();
            final ChunkMap.TrackedEntity entry = tracker.entityMap.get(id);
            final boolean canSee = (entry != null && entry.seenBy.contains(sPlayer.connection));
            final List<Packet<ClientGamePacketListener>> list = new ArrayList<>();

            // 언트래킹
            if (canSee) {
                list.add(new ClientboundRemoveEntitiesPacket(id));
            }

            // 개발자의 작업 수행
            action.loop(target, canSee, packet -> list.add((Packet<ClientGamePacketListener>) packet.getPacket()));

            // 리트래킹
            if (canSee) {
                entry.serverEntity.sendPairingData(list::add, sPlayer);
            }

            if (list.size() != 0) {
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
        void loop(final @NotNull Player target, final boolean canSee, final @NotNull Consumer<moe.caramel.daydream.network.Packet> packet);
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

    /**
     * 플레이어에게 로그인 패킷을 전송합니다.
     * 패킷 유틸리티가 월드 NBT에 접근해야 하는 경우 유용합니다.
     *
     * @param player 대상 플레이어
     * @param maxPlayers 최대 플레이어 수 (매직밸류로 가짜 패킷을 구분)
     */
    public static void sendLoginPacket(@NotNull Player player, final int maxPlayers) throws IllegalAccessException {
        final ServerPlayer sPlayer = ((CraftPlayer) player).getHandle();
        final ServerLevel sLevel = sPlayer.getLevel();
        final GameRules rules = sLevel.getGameRules();
        final MinecraftServer server = sLevel.getServer();

        // Get Synchronized Registries
        RegistryAccess.Frozen synchronizedRegistries = null;
        for (final Field field : PlayerList.class.getDeclaredFields()) {
            if (field.getDeclaringClass() == RegistryAccess.Frozen.class) {
                synchronizedRegistries = (RegistryAccess.Frozen) field.get(server.getPlayerList());
            }
        }

        if (synchronizedRegistries == null) {
            throw new NullPointerException();
        }

        // Send Packet
        sPlayer.connection.send(new ClientboundLoginPacket(
            sPlayer.getId(), sLevel.getLevelData().isHardcore(),
            sPlayer.gameMode.getGameModeForPlayer(), sPlayer.gameMode.getPreviousGameModeForPlayer(),
            server.levelKeys(), synchronizedRegistries, sLevel.dimensionTypeId(), sLevel.dimension(),
            BiomeManager.obfuscateSeed(sLevel.getSeed()), maxPlayers,
            sLevel.getChunkSource().chunkMap.playerChunkManager.getTargetSendDistance(),
            sLevel.getChunkSource().chunkMap.playerChunkManager.getTargetTickViewDistance(),
            rules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO),
            !rules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN),
            sLevel.isDebug(), sLevel.isFlat(),
            sPlayer.getLastDeathLocation()
        ));
    }

    // ================================
}
