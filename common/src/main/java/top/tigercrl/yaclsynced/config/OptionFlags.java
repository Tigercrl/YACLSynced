package top.tigercrl.yaclsynced.config;

import dev.isxander.yacl3.api.OptionFlag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface OptionFlags {
    FlagType[] value();

    enum FlagType {
        GAME_RESTART,
        RELOAD_CHUNKS,
        WORLD_RENDER_UPDATE,
        ASSET_RELOAD;

        @Environment(EnvType.CLIENT)
        public OptionFlag getFlag() {
            return switch (this) {
                case GAME_RESTART -> OptionFlag.GAME_RESTART;
                case RELOAD_CHUNKS -> OptionFlag.RELOAD_CHUNKS;
                case WORLD_RENDER_UPDATE -> OptionFlag.WORLD_RENDER_UPDATE;
                case ASSET_RELOAD -> OptionFlag.ASSET_RELOAD;
            };
        }
    }
}
