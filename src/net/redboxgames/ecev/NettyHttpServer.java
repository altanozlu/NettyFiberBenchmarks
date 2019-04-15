package net.redboxgames.ecev;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.*;
import io.netty.handler.codec.http.*;
import com.mongodb.ConnectionString;
import org.bson.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;

class WorkerThreadFactory implements ThreadFactory {
    private int counter = 0;
    private String prefix = "";

    public WorkerThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    public Thread newThread(Runnable r) {
        //  System.out.println(prefix);
        System.out.println(r);
        return new Thread(r, prefix + "-" + counter++);
    }
}


public class NettyHttpServer {
    private ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    public static boolean testFiber = true;
    public static boolean testWithMongo = true;
    public static MongoCollection<Document> collection;

    public NettyHttpServer() {

        masterGroup = new NioEventLoopGroup(8);
        if (testFiber) slaveGroup = new NioEventLoopGroup(8, new FiberPerTaskExecutor(new FiberFactory()));
        else {
            slaveGroup = new NioEventLoopGroup(8);

        }

    }

    public void start() // #1
    {
        if (testWithMongo) {
            MongoClient mongoClient = MongoClients.create();
            MongoDatabase database = mongoClient.getDatabase("bench");
            collection = database.getCollection("test");
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        try {
            // #3
            final ServerBootstrap bootstrap =
                    new ServerBootstrap()
                            .group(masterGroup, slaveGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() // #4
                            {
                                @Override
                                public void initChannel(final SocketChannel ch)
                                        throws Exception {
                                    ch.pipeline().addLast("codec", new HttpServerCodec());
                                    ch.pipeline().addLast("aggregator",
                                            new HttpObjectAggregator(512 * 1024));

                                    ch.pipeline().addLast("httpHandler", new HttpServerHandler());
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 100000)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("Listening");
            channel = bootstrap.bind(8080).sync();
        } catch (final InterruptedException e) {
        }
    }

    public void shutdown() // #2
    {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();

        try {
            channel.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        new NettyHttpServer().start();
    }
}