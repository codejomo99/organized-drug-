package com.side.drug.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LogWebSocketHandler extends TextWebSocketHandler {

	private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.add(session);
		log.info("WebSocket 연결됨: {}", session.getId());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessions.remove(session);
		log.info("WebSocket 연결 종료: {}", session.getId());
	}

	public void broadcast(String log) {
		TextMessage message = new TextMessage(log + "\n");

		for (WebSocketSession session : sessions) {
			if (!session.isOpen()) continue;
			try {
				synchronized (session) { // flush 타이밍 보장
					session.sendMessage(message);
				}
			} catch (IOException e) {
				sessions.remove(session);
			}
		}
	}

	public void sendProgress(long processed, long total) {
		Map<String,Object> payload = Map.of(
			"type",      "progress",
			"processed", processed,
			"total",     total
		);
		String json;
		try { json = objectMapper.writeValueAsString(payload); }
		catch (JsonProcessingException e) { return; }
		TextMessage msg = new TextMessage(json);
		sessions.forEach(s -> {
			if (s.isOpen()) {
				try { s.sendMessage(msg); }
				catch (IOException ignored) {}
			}
		});
	}
}

