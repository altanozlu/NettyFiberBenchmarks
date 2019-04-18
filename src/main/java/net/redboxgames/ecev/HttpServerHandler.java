package net.redboxgames.ecev;

import com.github.jasync.sql.db.mysql.MySQLConnection;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.github.jasync.sql.db.pool.ConnectionPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;


import java.sql.*;
import java.util.Random;

import static io.netty.buffer.Unpooled.copiedBuffer;


public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    public Fiber fiber;
    WebSocketServerHandshaker handshaker;
    static Random random = new Random();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (fiber != null) {
            fiber.cancel();
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        if (NettyHttpServer.testFiber) {
            fiber = Fiber.schedule(() -> {
                readBlock(ctx, msg);
            });
        } else {

            readBlock(ctx, msg);
        }

    }

    public void readBlock(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {

            HttpRequest httpRequest = (HttpRequest) msg;


            HttpHeaders headers = httpRequest.headers();

            if ("Upgrade".equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
                    "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE))) {

                //Adding new handler to the existing pipeline to handle WebSocket Messages
                ctx.pipeline().replace(this, "websocketHandler", new WebSocketHandler());

                //Do the Handshake to upgrade connection from HTTP to WebSocket protocol
                handleHandshake(ctx, httpRequest);
            } else {

                if (NettyHttpServer.testMysql) {
                    if (NettyHttpServer.testFiber) {
                        try (Connection connection = NettyHttpServer.ds.getConnection();
                             PreparedStatement statement = connection.prepareStatement("select email from authors where id = ?;");) {
                            statement.setInt(1, random.nextInt(999) + 1);
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()) {
                                write(ctx, HttpResponseStatus.OK, String.valueOf(rs.getString("email")));
                            } else {

                                write(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "err");
                            }
                            //Random random = new Random();
                            //  statement.setInt(1, random.nextInt(999) + 1);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                }
                if (NettyHttpServer.testMongoDB) {
                    NettyHttpServer.collection.find().first();
                } else {
                    final String responseMessage = "Hello from Netty!";

                    write(ctx, HttpResponseStatus.OK, responseMessage);
                }
            }
        } else {
            System.out.println("Incoming request is unknown");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause);
        write(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }

    private void write(ChannelHandlerContext ctx, HttpResponseStatus status, String responseMessage) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                copiedBuffer(responseMessage.getBytes())
        );

        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
                "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
                responseMessage.length());
        ctx.writeAndFlush(response);
    }


    /* Do the handshaking for WebSocket request */
    protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    protected String getWebSocketURL(HttpRequest req) {
        String url = "ws://" + req.headers().get("Host") + req.getUri();
        return url;
    }
}