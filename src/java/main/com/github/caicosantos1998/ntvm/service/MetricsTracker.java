package java.main.com.github.caicosantos1998.ntvm.service;

import java.main.com.github.caicosantos1998.ntvm.model.ConnectionStatus;
import java.main.com.github.caicosantos1998.ntvm.model.PingResult;

public class MetricsTracker {
    private long minPing = Long.MAX_VALUE;
    private long maxPing = 0;
    private long totalPingSum = 0;
    private long pingCount = 0;

    // The program runs in the background.
    // The method needs to be thread-safe, ensuring that JAVA handles the queuing.
    public synchronized void register(PingResult pResult) {
        if(pResult.status() != ConnectionStatus.ONLINE) {
            return;
        }
        long latency = pResult.latencyMs();
        pingCount++;
        totalPingSum += latency;

        if(latency<minPing) {
            minPing = latency;
        }
        if(latency>maxPing) {
            maxPing = latency;
        }
    }

    public long getMinPing() {
        return minPing == Long.MAX_VALUE ? 0 : minPing;
    }

    public long getMaxPing() {
        return maxPing;
    }

    public long getAveragePing() {
        return pingCount == 0 ? 0 : totalPingSum / pingCount;
    }
}
