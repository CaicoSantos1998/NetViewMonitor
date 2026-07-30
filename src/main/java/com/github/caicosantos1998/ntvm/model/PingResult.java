package main.java.com.github.caicosantos1998.ntvm.model;

import java.time.LocalDateTime;

public record PingResult(
        LocalDateTime timeStamp,
        ConnectionStatus status,
        long latencyMs
) {
}
