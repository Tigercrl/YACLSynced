package top.tigercrl.yaclsynced;

import top.tigercrl.yaclsynced.config.ConfigUtils;
import top.tigercrl.yaclsynced.config.SyncedConfigClassHandler;
import top.tigercrl.yaclsynced.network.payloads.S2CConfigSyncPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class YaclSynced {
    public static final String MOD_ID = "yaclsynced";

    public static final Map<ResourceLocation, SyncedConfigClassHandler<?>> configHandlers = new HashMap<>();
    public static Map<ResourceLocation, Object> remoteConfigs = new HashMap<>();

    public static void init() {
        // Write common init code here.
    }

    public static void handleSyncConfig(S2CConfigSyncPayload payload) {
        YaclSynced.remoteConfigs.put(payload.id(), ConfigUtils.deserializeJson(payload.config(), YaclSynced.configHandlers.get(payload.id()).configClass()));
    }
}
