package com.example.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class EchoClient {

    static String msg = "HELLO_WORLD_HELLO_WORLD_HELLO_WORLD_HELLO_WORLD_HELLO_WORLD_HELLO_WORLD_HELLO_WORLD_HELLO_WORLD";

    public static void main(String args[]) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new LengthFieldBasedFrameDecoder(2000, 0, 4, 0, 4));
                            p.addLast(new StringDecoder());
                            p.addLast(new LengthBasedFrameEncoder());
                            p.addLast(new StringEncoder());
                            p.addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture future = b.connect("localhost", 1221).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    static class EchoClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            GenerateTraffic gt = new GenerateTraffic(ctx);
            gt.start();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println((String) msg);
        }
    }

    static class GenerateTraffic {
        ChannelHandlerContext ctx;
        Executor executor;
        AtomicInteger i = new AtomicInteger(0);
        GenerateTraffic(ChannelHandlerContext ctx){
            this.ctx = ctx;
            executor = Executors.newFixedThreadPool(10);
        }

        public void start() {
            executor.execute(() -> {
                while(true) {
                    ctx.writeAndFlush(msg + i.get());
                    i.incrementAndGet();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                }
            });
        }

    }


}
