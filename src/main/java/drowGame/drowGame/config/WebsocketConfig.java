package drowGame.drowGame.config;

import drowGame.drowGame.interceptor.Intercepter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;
    private final Intercepter intercepter;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("add handler : /start");
        registry.addHandler(webSocketHandler, "/start")
                .setAllowedOriginPatterns("*")
                .addInterceptors(intercepter);
    }
}
