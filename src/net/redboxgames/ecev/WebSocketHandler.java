package net.redboxgames.ecev;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
       //Fiber.schedule(()->{
           if (msg instanceof WebSocketFrame) {
               if (msg instanceof BinaryWebSocketFrame) {
                   System.out.println("BinaryWebSocketFrame Received : ");
                   System.out.println(((BinaryWebSocketFrame) msg).content());
               } else if (msg instanceof TextWebSocketFrame) {
                   ctx.channel().writeAndFlush(
                           new TextWebSocketFrame(((TextWebSocketFrame) msg).text()));
               } else if (msg instanceof PingWebSocketFrame) {
                   System.out.println("PingWebSocketFrame Received : ");
                   System.out.println(((PingWebSocketFrame) msg).content());
               } else if (msg instanceof PongWebSocketFrame) {
                   System.out.println("PongWebSocketFrame Received : ");
                   System.out.println(((PongWebSocketFrame) msg).content());
               } else if (msg instanceof CloseWebSocketFrame) {
                   ctx.channel().close();
               } else {
                   System.out.println("Unsupported WebSocketFrame");
               }
           }
      // });
    }
}