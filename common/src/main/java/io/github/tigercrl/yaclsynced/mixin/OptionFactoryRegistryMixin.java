package io.github.tigercrl.yaclsynced.mixin;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.autogen.OptionAccess;
import dev.isxander.yacl3.config.v2.api.autogen.OptionFactory;
import dev.isxander.yacl3.config.v2.impl.autogen.OptionFactoryRegistry;
import io.github.tigercrl.yaclsynced.config.OptionFlags;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.BiConsumer;

@Mixin(value = OptionFactoryRegistry.class, remap = false)
public class OptionFactoryRegistryMixin {
    @WrapOperation(method = "createOption", at = @At(value = "INVOKE", target = "Ldev/isxander/yacl3/config/v2/api/autogen/OptionFactory;createOption(Ljava/lang/annotation/Annotation;Ldev/isxander/yacl3/config/v2/api/ConfigField;Ldev/isxander/yacl3/config/v2/api/autogen/OptionAccess;)Ldev/isxander/yacl3/api/Option;"))
    private static <A extends Annotation, T> Option<T> onCreateOption(OptionFactory<A, T> instance, A annotation, ConfigField<T> configField, OptionAccess optionAccess, Operation<Option<T>> original, @Local(argsOnly = true) Field field) {
        Option<T> option = original.call(instance, annotation, configField, optionAccess);
        return new Option<>() {
            @Override
            public @NotNull Component name() {
                return option.name();
            }

            @Override
            public @NotNull OptionDescription description() {
                return option.description();
            }

            @Override
            public @NotNull Component tooltip() {
                return option.tooltip();
            }

            @Override
            public @NotNull Controller<T> controller() {
                return option.controller();
            }

            @Override
            public @NotNull StateManager<T> stateManager() {
                return option.stateManager();
            }

            @Override
            public @NotNull Binding<T> binding() {
                return option.binding();
            }

            @Override
            public boolean available() {
                return option.available();
            }

            @Override
            public void setAvailable(boolean available) {
                option.setAvailable(available);
            }

            @Override
            public @NotNull ImmutableSet<OptionFlag> flags() {
                if (!field.isAnnotationPresent(OptionFlags.class))
                    return ImmutableSet.of();
                else
                    return Arrays.stream(field.getAnnotation(OptionFlags.class).value())
                            .map(OptionFlags.FlagType::getFlag)
                            .collect(ImmutableSet.toImmutableSet());
            }

            @Override
            public boolean changed() {
                return option.changed();
            }

            @Override
            public @NotNull T pendingValue() {
                return option.pendingValue();
            }

            @Override
            public void requestSet(@NotNull T value) {
                option.requestSet(value);
            }

            @Override
            public boolean applyValue() {
                return option.applyValue();
            }

            @Override
            public void forgetPendingValue() {
                option.forgetPendingValue();
            }

            @Override
            public void requestSetDefault() {
                option.requestSetDefault();
            }

            @Override
            public boolean isPendingValueDefault() {
                return option.isPendingValueDefault();
            }

            @Override
            public void addEventListener(OptionEventListener<T> listener) {
                option.addEventListener(listener);
            }

            @Override
            public void addListener(BiConsumer<Option<T>, T> changedListener) {
                option.addListener(changedListener);
            }
        };
    }
}
