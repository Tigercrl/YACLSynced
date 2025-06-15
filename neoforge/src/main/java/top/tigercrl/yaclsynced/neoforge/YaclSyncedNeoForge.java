package top.tigercrl.yaclsynced.neoforge;

import top.tigercrl.yaclsynced.YaclSynced;
import top.tigercrl.yaclsynced.client.ui.ConfigMismatchScreen;
import top.tigercrl.yaclsynced.network.payloads.S2CConfigSyncFinishPayload;
import top.tigercrl.yaclsynced.network.payloads.S2CConfigSyncPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Mod(YaclSynced.MOD_ID)
@EventBusSubscriber(modid = YaclSynced.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class YaclSyncedNeoForge {

    public YaclSyncedNeoForge() {
        // Run our common setup.
        YaclSynced.init();
    }

    @SubscribeEvent
    private static void configureModdedClient(RegisterConfigurationTasksEvent event) {
        event.register(new ICustomConfigurationTask() {

            @Override
            public void run(@NotNull Consumer<CustomPacketPayload> consumer) {
                for (ResourceLocation id : YaclSynced.configHandlers.keySet()) {
                    consumer.accept(S2CConfigSyncPayload.create(id));
                }
                consumer.accept(new S2CConfigSyncFinishPayload());
                event.getListener().finishCurrentTask(type());
            }

            @Override
            @NotNull
            public Type type() {
                return new ConfigurationTask.Type(ResourceLocation.fromNamespaceAndPath("yacl", "sync_config"));
            }
        });
    }

    @SubscribeEvent
    private static void handlePayload(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
                .configurationToClient(S2CConfigSyncPayload.TYPE, S2CConfigSyncPayload.STREAM_CODEC, (payload, context) ->
                        YaclSynced.handleSyncConfig(payload)
                )
                .configurationToClient(S2CConfigSyncFinishPayload.TYPE, S2CConfigSyncFinishPayload.STREAM_CODEC, (payload, context) ->
                        ConfigMismatchScreen.checkMismatch()
                );
    }
}
