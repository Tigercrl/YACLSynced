package io.github.tigercrl.yaclsynced.fabric.client;

import io.github.tigercrl.yaclsynced.YaclSynced;
import io.github.tigercrl.yaclsynced.client.ui.ConfigMismatchScreen;
import io.github.tigercrl.yaclsynced.network.payloads.S2CConfigSyncFinishPayload;
import io.github.tigercrl.yaclsynced.network.payloads.S2CConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;

public final class YaclSyncedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientConfigurationNetworking.registerGlobalReceiver(S2CConfigSyncPayload.TYPE, (payload, context) ->
                YaclSynced.handleSyncConfig(payload)
        );
        ClientConfigurationNetworking.registerGlobalReceiver(S2CConfigSyncFinishPayload.TYPE, (payload, context) ->
                ConfigMismatchScreen.checkMismatch()
        );

    }
}
