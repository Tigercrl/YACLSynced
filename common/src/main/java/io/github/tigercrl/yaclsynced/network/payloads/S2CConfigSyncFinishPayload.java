package io.github.tigercrl.yaclsynced.network.payloads;

import io.github.tigercrl.yaclsynced.YaclSynced;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record S2CConfigSyncFinishPayload() implements CustomPacketPayload {
    public static final Type<S2CConfigSyncFinishPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YaclSynced.MOD_ID, "config_sync_finish"));
    public static final StreamCodec<ByteBuf, S2CConfigSyncFinishPayload> STREAM_CODEC = StreamCodec.unit(new S2CConfigSyncFinishPayload());

    @Override
    public @NotNull Type<S2CConfigSyncFinishPayload> type() {
        return TYPE;
    }
}
