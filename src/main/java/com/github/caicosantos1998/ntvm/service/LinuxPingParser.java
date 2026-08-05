package com.github.caicosantos1998.ntvm.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.github.caicosantos1998.ntvm.model.ConnectionStatus;
import com.github.caicosantos1998.ntvm.model.PingResult;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinuxPingParser implements PingParser {
    private static final Pattern PATTERN_TIME = Pattern.compile("time=(?<tempo>\\d+(\\.\\d+)?)");
    private final String targetHost;

    public LinuxPingParser(String targetHost) {
        this.targetHost = targetHost;
    }

    @Override
    public PingResult runPing() {
        LocalDateTime now = LocalDateTime.now();
        try {
            ProcessBuilder builder = new ProcessBuilder("ping", "-c", "1", "-W", "1" ,targetHost);
            Process process = builder.start();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = PATTERN_TIME.matcher(line.toLowerCase());
                    if (matcher.find()) {
                        double latency = Double.parseDouble(matcher.group("tempo"));
                        return new PingResult(now, ConnectionStatus.ONLINE, Math.round(latency));
                    }
                }
            }
            int exitCode = process.waitFor();
            return exitCode == 0 ? new PingResult(now, ConnectionStatus.TIMEOUT, 0) :
                    new PingResult(now, ConnectionStatus.OFFLINE, 0);
        } catch (Exception e) {
            return new PingResult(now, ConnectionStatus.OFFLINE, 0);}
        }
}
