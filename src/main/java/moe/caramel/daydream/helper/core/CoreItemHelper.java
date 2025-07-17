package moe.caramel.daydream.helper.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.jetbrains.annotations.ApiStatus;
import java.util.Optional;

/**
 * 아이템 헬퍼 클래스
 */
@Deprecated(forRemoval = true)
@ApiStatus.NonExtendable
@SuppressWarnings("unused")
public interface CoreItemHelper {

    /**
     * 데이터 버전
     *
     * @see org.bukkit.UnsafeValues#getDataVersion()
     */
    int DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version();

    /**
     * {@link org.bukkit.inventory.ItemStack}을 {@link JsonElement}로 변환합니다.
     *
     * @param item 대상 아이템
     * @return 직렬화된 이이템 데이터
     * @see org.bukkit.UnsafeValues#serializeItemAsJson(org.bukkit.inventory.ItemStack)
     */
    static Optional<JsonElement> serializeToJson(final org.bukkit.inventory.ItemStack item) {
        final ItemStack nms = CraftItemStack.unwrap(item);
        if (nms.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(CraftMagicNumbers.INSTANCE.serializeItemAsJson(item));
    }

    /**
     * {@link JsonElement}를 {@link org.bukkit.inventory.ItemStack}로 변환합니다.
     *
     * @param json 직렬화된 아이템 데이터
     * @return 아이템
     * @see org.bukkit.UnsafeValues#deserializeItemFromJson(JsonObject)
     */
    static org.bukkit.inventory.ItemStack deserializeFromJson(final JsonElement json) {
        return CraftMagicNumbers.INSTANCE.deserializeItemFromJson((JsonObject) json);
    }
}
