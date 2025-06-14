package io.github.tigercrl.yaclsynced.fabric;

import io.github.tigercrl.yaclsynced.YaclSynced;
import io.github.tigercrl.yaclsynced.network.payloads.S2CConfigSyncFinishPayload;
import io.github.tigercrl.yaclsynced.network.payloads.S2CConfigSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.resources.ResourceLocation;

public final class YaclSyncedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        YaclSynced.init();

        PayloadTypeRegistry.configurationS2C().register(S2CConfigSyncPayload.TYPE, S2CConfigSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.configurationS2C().register(S2CConfigSyncFinishPayload.TYPE, S2CConfigSyncFinishPayload.STREAM_CODEC);

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, S2CConfigSyncPayload.TYPE)) {
                for (ResourceLocation id : YaclSynced.configHandlers.keySet()) {
                    ServerConfigurationNetworking.send(handler, S2CConfigSyncPayload.create(id));
                }
                ServerConfigurationNetworking.send(handler, new S2CConfigSyncFinishPayload());
            }
        });
    }
}
