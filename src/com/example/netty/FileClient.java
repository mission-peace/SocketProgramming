package com.example.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;

public class FileClient {

    public static void main(String args[]) throws Exception{
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(new LoggingHandler());
                            p.addLast(new ChunkedWriteHandler());
                            p.addLast(new FileClientHandler());
                        }
                    });

            ChannelFuture f = b.connect("localhost", 1221).sync();
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    static class FileClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception{
            ctx.writeAndFlush(new ChunkedFile(new File("/Users/tushar_v_roy/Desktop/interview/interview.iws")));
        }

        @Override
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
            String val = (String)obj;
            System.out.println(val);

            channelHandlerContext.writeAndFlush(val);
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable e) {
            System.out.println("Exception");
 //           channelHandlerContext.close();
//            channelHandlerContext.flush();
        }
    }
}
