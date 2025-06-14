# YACL Synced

![Enviroment](https://img.shields.io/badge/Enviroment-Client%20&%20Server-orange)
[![Modrinth](https://img.shields.io/modrinth/dt/MaPrjRfi?color=00AF5C&label=downloads&logo=modrinth)](https://modrinth.com/mod/yacl-synced)

An addon for YetAnotherConfigLib with server-client config synchronization

## Why synced?

YACL is a config lib that helps developers create a config with beautiful config screen in Minecraft.
But it doesn't support server-client config synchronization.
YACL Synced is an addon for YACL that adds the feature.

## For developers

### Install

```groovy
// build.gradle
maven {
    name = "Modrinth"
    url = "https://api.modrinth.com/maven"
}

dependencies {
    modImplementation "maven.modrinth:yacl-synced:<version>-<fabric/neoforge>"
}
```

### Usage

You can define a config in the same way as YACL but with `SyncedConfigClassHandler`.

```java
public class CommonConfig {
    public static final ConfigClassHandler<CommonConfig> HANDLER = SyncedConfigClassHandler.createBuilder(CommonConfig.class)
            .id(YACLPlatform.rl("yacl3-test", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("yacl-test-v2.json5"))
                    .setJson5(true)
                    .build())
            .build();

    // ...
}
```

On both sides, you can use `HANDLER.instance()` in the same way to get the config instance.

On client side, you can also use `HANDLER.localInstance()` and `HANDLER.remoteInstance()` to get the local and remote config instances in a server.

### Config mismatch detect

You can use `@OptionFlags` to define option flags for an autogen config

It's same as using `Option.Builder.flag(...OptionFlag flag)` in YACL

```java
public class CommonConfig {
    @AutoGen(category = "test", group = "test")
    @SerialEntry(comment = "This option requires game restart")
    @OptionFlags({OptionFlags.FlagType.GAME_RESTART})
    @BooleanFlag
    public boolean myOption = true;
}
```

But by using `@OptionFlags`, YACL Synced will detect the config mismatch when joining a server and show a warning screen.