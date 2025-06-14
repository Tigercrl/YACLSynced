package io.github.tigercrl.yaclsynced.network.payloads;

import io.github.tigercrl.yaclsynced.YaclSynced;
import io.github.tigercrl.yaclsynced.config.ConfigUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record S2CConfigSyncPayload(ResourceLocation id, String config) implements CustomPacketPayload {
    public static final Type<S2CConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YaclSynced.MOD_ID, "config_sync"));
    public static final StreamCodec<ByteBuf, S2CConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            S2CConfigSyncPayload::id,
            ByteBufCodecs.STRING_UTF8,
            S2CConfigSyncPayload::config,
            S2CConfigSyncPayload::new
    );

    public static S2CConfigSyncPayload create(ResourceLocation id) {
        return new S2CConfigSyncPayload(id, ConfigUtils.serializeToJson(YaclSynced.configHandlers.get(id).instance()));
    }

    @Override
    public @NotNull Type<S2CConfigSyncPayload> type() {
        return TYPE;
    }
}
