package com.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import java.io.FileOutputStream;

public class FileServer {

    public static void main(String args[]) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {

                            ChannelPipeline p = channel.pipeline();
                            p.addLast(new LoggingHandler());
        //                    p.addLast(new ChunkedWriteHandler());
                            p.addLast(new FileServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(1221).sync();
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    static class FileServerHandler extends ChannelInboundHandlerAdapter {

        FileOutputStream fileOutputStream;
        FileServerHandler() throws Exception{
            fileOutputStream = new FileOutputStream("/tmp/test");
        }
        @Override
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
            ByteBuf byteBuf = (ByteBuf)obj;
            byteBuf.readBytes(fileOutputStream, byteBuf.readableBytes());

     //       channelHandlerContext.writeAndFlush(val);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {
            System.out.println("read complete");
//            channelHandlerContext.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable e) {
            System.out.println("Exception");
            e.printStackTrace();
            //           channelHandlerContext.close();
//            channelHandlerContext.flush();
        }

    }
}
