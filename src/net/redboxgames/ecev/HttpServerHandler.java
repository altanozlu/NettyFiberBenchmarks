package net.redboxgames.ecev;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.bson.Document;

import java.nio.charset.StandardCharsets;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

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


                if (NettyHttpServer.testWithMongo) {
                    if (NettyHttpServer.testFiber) {
                        Document document = NettyHttpServer.collection.find().first();
                        final String responseMessage = document.toJson();
                        write(ctx, HttpResponseStatus.OK, responseMessage);
                    } else {
                      /*  NettyHttpServer.collection.find().first((Document document, Throwable err) -> {

                            final String responseMessage = document.toJson();
                            write(ctx, HttpResponseStatus.OK, responseMessage);
                        });*/ //Can't have both of them at the same time
                    }
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
        write(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, cause.getMessage());
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