package moe.caramel.daydream.helper.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import org.jetbrains.annotations.NotNull;

/**
 * 서버 유틸리티 클래스
 */
@SuppressWarnings("unused")
public final class CoreServerUtil {

    private CoreServerUtil() { throw new UnsupportedOperationException(); }

    // ================================

    private static final RegistryOps<JsonElement> BUILTIN_OPS = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

    /**
     * 서버 레지스트리를 JSON으로 변환하여 가져옵니다.
     *
     * @return 변환된 JSON 레지스트리
     */
    @NotNull
    public static JsonObject getRegistryToJson() {
        final LayeredRegistryAccess<RegistryLayer> registries = MinecraftServer.getServer().registries();
        final Frozen frozen = new ImmutableRegistryAccess(RegistrySynchronization.networkedRegistries(registries)).freeze();

        return Util.getOrThrow(RegistrySynchronization.NETWORK_CODEC.encodeStart(BUILTIN_OPS, frozen), message -> {
            return new RuntimeException("Failed to encode: " + message + " " + frozen);
        }).getAsJsonObject();
    }

    // ================================
}
