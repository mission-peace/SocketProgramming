package com.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class EchoServer {

    public static void main(String args[]) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(new LengthFieldBasedFrameDecoder(2000, 0, 4, 0, 4));
                            p.addLast(new StringDecoder());
                            p.addLast(new LengthBasedFrameEncoder());
                            p.addLast(new StringEncoder());
                            p.addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(1221).sync();
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    static class EchoServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
            String val = (String)obj;
            System.out.println(val);

            channelHandlerContext.writeAndFlush(val);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {
//            channelHandlerContext.flush();
        }
    }
}
