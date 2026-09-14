package com.ashokleyland.wms.config;

import com.ashokleyland.wms.websocket.WarehouseUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WarehouseUpdateHandler warehouseUpdateHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(warehouseUpdateHandler, "/ws/warehouse-updates")
                .setAllowedOrigins("*");
    }
}
