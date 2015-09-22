package com.example.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.ByteBuffer;
import java.util.List;

public class LengthBasedFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> list) throws Exception {
        ByteBuf byteBuf = Unpooled.buffer(4 + msg.readableBytes());
        byteBuf.writeBytes(ByteBuffer.allocate(4).putInt(msg.readableBytes()).array());
        byteBuf.writeBytes(msg);
        list.add(byteBuf);
    }
}

