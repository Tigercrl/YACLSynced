package io.github.tigercrl.yaclsynced.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.impl.serializer.GsonConfigSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;

import java.awt.*;

public class ConfigUtils {
    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .serializeNulls()
            .registerTypeHierarchyAdapter(Component.class, new Component.SerializerAdapter(RegistryAccess.EMPTY))
            .registerTypeHierarchyAdapter(Style.class, /*? if >=1.20.4 {*/new GsonConfigSerializer.StyleTypeAdapter()/*?} else {*//*new Style.Serializer()*//*?}*/)
            .registerTypeHierarchyAdapter(Color.class, new GsonConfigSerializer.ColorTypeAdapter())
            .registerTypeHierarchyAdapter(Item.class, new GsonConfigSerializer.ItemTypeAdapter())
            .create();

    public static String serializeToJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T deserializeJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static String camelToSnake(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
