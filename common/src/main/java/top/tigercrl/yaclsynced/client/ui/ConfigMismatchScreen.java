package top.tigercrl.yaclsynced.client.ui;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import top.tigercrl.yaclsynced.YaclSynced;
import top.tigercrl.yaclsynced.config.ConfigUtils;
import top.tigercrl.yaclsynced.config.OptionFlags;
import top.tigercrl.yaclsynced.config.SyncedConfigClassHandler;
import top.tigercrl.yaclsynced.mixin.ConnectScreenMixin;
import top.tigercrl.yaclsynced.mixin.GsonConfigSerializerMixin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ConfigMismatchScreen extends Screen {
    private static final Component TITLE = Component.translatable("yacl.restart.title");
    private static final Component MESSAGE = Component.translatable("yaclsynced.restart.message");
    private static final Component YES = Component.translatable("yaclsynced.restart.yes");
    private static final Component NO = Component.translatable("menu.disconnect");
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, Set<FieldMismatchInfo>> mismatches;

    private ConfigMismatchScreen(Map<ResourceLocation, Set<FieldMismatchInfo>> mismatches) {
        super(TITLE);
        this.mismatches = mismatches;
    }

    @Override
    protected void init() {
        LinearLayout layout = LinearLayout.vertical();
        layout.defaultCellSetting().alignHorizontallyCenter();
        AtomicBoolean isFirst = new AtomicBoolean(true);
        mismatches.forEach((id, infos) -> {
            SyncedConfigClassHandler<?> handler = YaclSynced.configHandlers.get(id);
            if (isFirst.get()) isFirst.set(false);
            else layout.addChild(new MultiLineTextWidget(Component.literal(" "), font));
            layout.addChild(new MultiLineTextWidget(
                    Component.translatable(
                            "yaclsynced.restart.subtitle",
                            Component.literal(
                                    handler.serializer() instanceof GsonConfigSerializerMixin serializer ?
                                            serializer.getPath().getFileName().toString() :
                                            id.toString()
                            ).withStyle(ChatFormatting.YELLOW)
                    ),
                    font
            ).setCentered(true).setMaxWidth(width - 50));
            infos.forEach(info ->
                    layout.addChild(new MultiLineTextWidget(
                            Component.translatable(
                                    "yaclsynced.restart.info",
                                    Component.literal(handler.serializer() instanceof GsonConfigSerializerMixin ?
                                            ConfigUtils.camelToSnake(info.field.getName()) :
                                            info.field.getName()
                                    ).withStyle(ChatFormatting.YELLOW),
                                    info.localValue,
                                    info.remoteValue
                            ), font).setCentered(true).setMaxWidth(width - 50))
            );
        });
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        FrameLayout.centerInRectangle(layout, getRectangle());
        addRenderableWidget(Button.builder(YES, button -> {
            mismatches.forEach((id, infos) -> {
                SyncedConfigClassHandler<?> handler = YaclSynced.configHandlers.get(id);
                infos.forEach(info -> {
                    try {
                        info.field.set(handler.localInstance(), info.remoteValue);
                    } catch (IllegalAccessException e) {
                        LOGGER.error(
                                "Failed to set field '{}' in config '{}' to value '{}'.",
                                info.field.getName(), handler.id(), info.remoteValue
                        );
                    }
                });
                handler.save();
            });
            minecraft.stop();
        }).bounds(width / 2 - 150, height - 30, 148, 20).build());
        addRenderableWidget(Button.builder(NO, button ->
                minecraft.setScreen(null)
        ).bounds(width / 2 + 2, height - 30, 148, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(font, TITLE, width / 2, 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, MESSAGE, width / 2, 10 + font.lineHeight, 0xFFFFFF);
    }

    public static void checkMismatch() {
        Map<ResourceLocation, Set<FieldMismatchInfo>> mismatches = new HashMap<>();
        Set<OptionFlags.FlagType> actions = new HashSet<>();

        for (SyncedConfigClassHandler<?> handler : YaclSynced.configHandlers.values()) {
            Set<FieldMismatchInfo> mismatchSet = new HashSet<>();

            Set<Field> fields = Arrays.stream(handler.configClass().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(OptionFlags.class))
                    .collect(Collectors.toSet());
            fields.forEach(field -> {
                try {
                    field.setAccessible(true);
                    if (!field.get(handler.remoteInstance()).equals(field.get(handler.localInstance()))) {
                        mismatchSet.add(new FieldMismatchInfo(
                                field,
                                field.get(handler.localInstance()),
                                field.get(handler.remoteInstance()),
                                field.getAnnotation(OptionFlags.class).value())
                        );
                        actions.addAll(Arrays.asList(field.getAnnotation(OptionFlags.class).value()));
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error(
                            "Failed to check mismatch for field '{}' in config '{}'",
                            field.getName(), handler.id(), e
                    );
                }
            });

            if (!mismatchSet.isEmpty()) {
                mismatches.put(handler.id(), ImmutableSet.copyOf(mismatchSet));
            }
        }

        Map<ResourceLocation, Set<FieldMismatchInfo>> restartMismatches = mismatches.entrySet().stream()
                .peek(entry -> entry.setValue(
                        entry.getValue().stream().filter(info -> {
                            actions.addAll(Arrays.asList(info.flags));
                            return Arrays.stream(info.flags)
                                    .anyMatch(flag -> flag == OptionFlags.FlagType.GAME_RESTART);
                        }).collect(Collectors.toSet()))
                )
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Minecraft minecraft = Minecraft.getInstance();
        if (!restartMismatches.isEmpty()) {
            minecraft.execute(() -> {
                if (minecraft.screen instanceof ConnectScreenMixin screen) {
                    screen.setAborted(true);
                    if (screen.getChannelFuture() != null) {
                        screen.getChannelFuture().cancel(true);
                        screen.setChannelFuture(null);
                    }

                    if (screen.getConnection() != null) {
                        screen.getConnection().disconnect(ConnectScreen.ABORT_CONNECTION);
                    }
                }
                minecraft.disconnect(new ConfigMismatchScreen(restartMismatches));
            });
        } else {
            actions.stream().filter(flag -> flag != OptionFlags.FlagType.GAME_RESTART)
                    .forEach(flag -> flag.getFlag().accept(minecraft));
        }
    }

    private record FieldMismatchInfo(Field field,
                                     Object localValue,
                                     Object remoteValue,
                                     OptionFlags.FlagType[] flags) {
    }
}
