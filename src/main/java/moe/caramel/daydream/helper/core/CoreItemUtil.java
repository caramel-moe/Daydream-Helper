package moe.caramel.daydream.helper.core;

import static net.minecraft.SharedConstants.DATA_VERSION_TAG;
import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

/**
 * 아이템 유틸리티 클래스
 */
@SuppressWarnings("unused")
public final class CoreItemUtil {

    private CoreItemUtil() { throw new UnsupportedOperationException(); }

    // ================================

    /**
     * 데이터 버전
     */
    public static final int DATA_VERSION = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

    /**
     * {@link org.bukkit.inventory.ItemStack}을 {@link JsonElement}로 변환합니다.
     *
     * @param item 대상 아이템
     * @return 직렬화된 이이템 데이터
     */
    @NotNull
    public static Optional<JsonElement> serializeToJson(@NotNull org.bukkit.inventory.ItemStack item) {
        final ItemStack nms = (item instanceof CraftItemStack craft) ? craft.handle : CraftItemStack.asNMSCopy(item);
        if (nms == null || nms.isEmpty()) {
            return Optional.empty();
        }

        final CompoundTag tag = (CompoundTag) nms.save(MinecraftServer.getServer().registryAccess());
        tag.putInt(DATA_VERSION_TAG, DATA_VERSION);

        return Optional.of(CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, tag).getOrThrow(message -> {
            return new RuntimeException("Failed to encode: " + message);
        }));
    }

    /**
     * {@link JsonElement}를 {@link org.bukkit.inventory.ItemStack}로 변환합니다.
     *
     * @param json 직렬화된 아이템 데이터
     * @return 아이템
     */
    @NotNull
    public static org.bukkit.inventory.ItemStack deserializeFromJson(@NotNull JsonElement json) {
        final CompoundTag tag = CompoundTag.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow(message -> {
            return new RuntimeException("Failed to decode: " + message);
        }).getFirst();

        final int version = tag.getInt(DATA_VERSION_TAG);
        MCDataConverter.convertTag(MCTypeRegistry.ITEM_STACK, tag, version, DATA_VERSION);

        return ItemStack.parse(MinecraftServer.getServer().registryAccess(), tag).orElse(ItemStack.EMPTY).asBukkitMirror();
    }

    // ================================
}
