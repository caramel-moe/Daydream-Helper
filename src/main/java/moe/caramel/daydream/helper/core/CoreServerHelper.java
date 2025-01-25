package moe.caramel.daydream.helper.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * 서버 헬퍼 클래스
 */
@ApiStatus.NonExtendable
@SuppressWarnings("unused")
public interface CoreServerHelper {

    /**
     * 서버 레지스트리를 JSON으로 변환하여 가져옵니다.
     *
     * @return 변환된 JSON 레지스트리
     */
    @VisibleForTesting
    static JsonObject getRegistryToJson() {
        final LayeredRegistryAccess<RegistryLayer> registries = MinecraftServer.getServer().registries();
        final DynamicOps<JsonElement> ops = registries.compositeAccess().createSerializationContext(JsonOps.INSTANCE);
        final Frozen frozen = new ImmutableRegistryAccess(RegistrySynchronization.networkedRegistries(registries)).freeze();

        final JsonObject json = new JsonObject();
        RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach(data -> {
            final String location = locationToString(data.key().location());
            json.add(location, legacyRegistrySerializer(frozen, ops, location, data));
        });

        return json;
    }

    //<editor-fold desc="Registry Serializer" defaultstate="collapsed">
    private static <T> JsonObject legacyRegistrySerializer(final RegistryAccess registries,
                                                           final DynamicOps<JsonElement> ops,
                                                           final String serializedLocation,
                                                           final RegistryDataLoader.RegistryData<T> data
    ) {
        final JsonObject json = new JsonObject();
        final JsonArray values = new JsonArray();
        json.addProperty("type", serializedLocation);
        json.add("value", values);

        registries.lookup(data.key()).ifPresent(registry -> registry.entrySet().forEach(holder -> {
            final JsonObject value = new JsonObject();
            value.addProperty("name", locationToString(holder.getKey().location()));
            value.addProperty("id", registry.getId(holder.getValue()));
            value.add("element", data.elementCodec().encodeStart(ops, holder.getValue()).getOrThrow());
            values.add(value);
        }));

        return json;
    }

    // for ResourceLocation compressor
    private static String locationToString(final ResourceLocation location) {
        return (String) ResourceLocation.CODEC.encodeStart(JavaOps.INSTANCE, location).getOrThrow();
    }
    //</editor-fold>
}
