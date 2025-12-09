//package at.fhtw.ctfbackend.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//// TerminalWebSocketConfig.java
//@Configuration
//@EnableWebSocket
//public class TerminalWebSocketConfig implements WebSocketConfigurer {
//
//    private final TerminalWebSocketHandler handler;
//
//    public TerminalWebSocketConfig(TerminalWebSocketHandler handler) {
//        this.handler = handler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(handler, "/ws/terminal/{instanceId}")
//                .setAllowedOrigins("*"); // Adjust as needed for security
//    }
//}