package io.github.tigercrl.yaclsynced.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigSerializer;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.impl.ConfigClassHandlerImpl;
import dev.isxander.yacl3.config.v2.impl.ConfigFieldImpl;
import dev.isxander.yacl3.config.v2.impl.ReflectionFieldAccess;
import io.github.tigercrl.yaclsynced.YaclSynced;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.function.Function;

public class SyncedConfigClassHandler<T> extends ConfigClassHandlerImpl<T> {
    public SyncedConfigClassHandler(Class<T> configClass, ResourceLocation id, Function<ConfigClassHandler<T>, ConfigSerializer<T>> serializerFactory) {
        super(configClass, id, serializerFactory);
        YaclSynced.configHandlers.put(id, this);
    }

    @Override
    public T instance() {
        return Minecraft.getInstance().level == null ? localInstance() : remoteInstance();
    }

    @Environment(EnvType.CLIENT)
    public T localInstance() {
        return super.instance();
    }

    @Environment(EnvType.CLIENT)
    public T remoteInstance() {
        return (T) YaclSynced.remoteConfigs.get(id());
    }

    @Override
    public ConfigFieldImpl<?>[] fields() {
        if (Minecraft.getInstance().level == null) return super.fields();
        T instance = remoteInstance();
        SerialEntry classSerialEntry = configClass().getAnnotation(SerialEntry.class);
        return Arrays.stream(configClass().getDeclaredFields())
                .peek((field) -> field.setAccessible(true))
                .filter((field) -> classSerialEntry != null || field.isAnnotationPresent(SerialEntry.class) || field.isAnnotationPresent(AutoGen.class))
                .map((field) -> new ConfigFieldImpl<T>(
                        new ReflectionFieldAccess<>(field, instance),
                        new ReflectionFieldAccess<>(field, defaults()),
                        this,
                        field.getAnnotation(SerialEntry.class),
                        classSerialEntry,
                        field.getAnnotation(AutoGen.class)
                )).toArray(ConfigFieldImpl[]::new);
    }

    public static class BuilderImpl<T> implements Builder<T> {
        private final Class<T> configClass;
        private ResourceLocation id;
        private Function<ConfigClassHandler<T>, ConfigSerializer<T>> serializerFactory;

        public BuilderImpl(Class<T> configClass) {
            this.configClass = configClass;
        }

        @Override
        public Builder<T> id(ResourceLocation id) {
            this.id = id;
            return this;
        }

        @Override
        public Builder<T> serializer(Function<ConfigClassHandler<T>, ConfigSerializer<T>> serializerFactory) {
            this.serializerFactory = serializerFactory;
            return this;
        }

        @Override
        public ConfigClassHandler<T> build() {
            Validate.notNull(serializerFactory, "serializerFactory must not be null");
            Validate.notNull(configClass, "configClass must not be null");

            return new SyncedConfigClassHandler<>(configClass, id, serializerFactory);
        }
    }

    public static <T> Builder<T> createBuilder(Class<T> configClass) {
        return new SyncedConfigClassHandler.BuilderImpl<>(configClass);
    }
}
