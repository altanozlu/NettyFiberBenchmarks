package net.redboxgames.ecev;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.*;
import io.netty.handler.codec.http.*;
import org.bson.Document;
import org.mariadb.jdbc.MariaDbPoolDataSource;


import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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

class WorkerFiberFactory implements ThreadFactory {
    private int counter = 0;
    private String prefix = "";

    public Thread newThread(Runnable r) {
        Fiber.schedule(r);
        return null;
    }
}


public class NettyHttpServer {
    private ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    public static boolean testFiber = false;
    public static boolean testMysql = false;
    public static boolean testMongoDB = true;
    public static String mysqlUrl = "jdbc:mariadb://localhost:3306/bench?useSSL=false&user=root";
    public static MongoCollection<Document> collection;
    public static MariaDbPoolDataSource ds;

    public NettyHttpServer() {

        masterGroup = new NioEventLoopGroup(8);

        slaveGroup = new NioEventLoopGroup(8,new FiberPerTaskExecutor(new FiberFactory()));


    }

    public void start() // #1
    {
        if (testMysql) {
            if (testFiber) {
                try {
                    Class.forName("org.mariadb.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                ds = new MariaDbPoolDataSource();
                try {
                    ds.setUrl(mysqlUrl);
                    ds.setMaxPoolSize(120);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }

        if (testMongoDB) {
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

    }

    public static void main(String[] args) {
        new NettyHttpServer().start();
    }
}