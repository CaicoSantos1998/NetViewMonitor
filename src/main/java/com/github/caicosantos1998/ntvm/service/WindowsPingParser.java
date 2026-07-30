package main.java.com.github.caicosantos1998.ntvm.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import main.java.com.github.caicosantos1998.ntvm.model.ConnectionStatus;
import main.java.com.github.caicosantos1998.ntvm.model.PingResult;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsPingParser implements PingParser {
    private static final Pattern PATTERN_TIME = Pattern.compile("tempo[=<](?<tempo>\\d+)ms");
    private final String targetHost;

    public WindowsPingParser(String targetHost) {
        this.targetHost = targetHost;
    }

    @Override
    public PingResult runPing() {
        LocalDateTime now = LocalDateTime.now();
        try {
            ProcessBuilder builder = new ProcessBuilder("ping", "-n", "1", "-w", "10000" ,targetHost);
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = PATTERN_TIME.matcher(line.toLowerCase());
                    if (matcher.find()) {
                        long latency = Long.parseLong(matcher.group("tempo"));
                        return new PingResult(now, ConnectionStatus.ONLINE, latency);
                    }
                }
            }
            int exitCode = process.waitFor();
            return exitCode == 0 ? new PingResult(now, ConnectionStatus.TIMEOUT, 0) :
                    new PingResult(now, ConnectionStatus.OFFLINE, 0);
        } catch (Exception e) {
            return new PingResult(now, ConnectionStatus.OFFLINE, 0);
        }
    }
}
