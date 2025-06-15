package top.tigercrl.yaclsynced.mixin;

import io.netty.channel.ChannelFuture;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConnectScreen.class)
public interface ConnectScreenMixin {
    @Accessor("channelFuture")
    ChannelFuture getChannelFuture();

    @Accessor("channelFuture")
    void setChannelFuture(ChannelFuture channelFuture);

    @Accessor("aborted")
    boolean isAborted();

    @Accessor("aborted")
    void setAborted(boolean aborted);

    @Accessor("connection")
    Connection getConnection();
}
