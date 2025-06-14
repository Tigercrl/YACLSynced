package io.github.tigercrl.yaclsynced.mixin;

import dev.isxander.yacl3.config.v2.impl.serializer.GsonConfigSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(value = GsonConfigSerializer.class, remap = false)
public interface GsonConfigSerializerMixin {
    @Accessor("path")
    Path getPath();
}
