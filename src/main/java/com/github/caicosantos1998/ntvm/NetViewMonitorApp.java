package com.github.caicosantos1998.ntvm;

import com.github.caicosantos1998.ntvm.model.PingResult;
import com.github.caicosantos1998.ntvm.repository.CsvRepository;
import com.github.caicosantos1998.ntvm.service.LinuxPingParser;
import com.github.caicosantos1998.ntvm.service.MetricsTracker;
import com.github.caicosantos1998.ntvm.service.PingParser;
import com.github.caicosantos1998.ntvm.service.WindowsPingParser;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// The main class responsible for orchestrate the entire monitoring system
public class NetViewMonitorApp {
    private static final String HOST_TARGET;
    private static final String FILE_NAME;

    // Method for assigning values to and validating constants
    static {
        HOST_TARGET = System.getenv("HOST_TARGET");
        FILE_NAME = System.getenv("FILE_NAME");

        if (HOST_TARGET == null || HOST_TARGET.isBlank()) {
            throw new IllegalStateException("ERROR: The 'HOST_TARGET' environment variable is not defined.");
        }
        if (FILE_NAME == null || FILE_NAME.isBlank()) {
            throw new IllegalStateException("ERROR: The 'FILE_NAME' environment variable is not defined.");
        }
    }

    private final PingParser pingParser;
    private final MetricsTracker metricsTracker;
    private final CsvRepository csvRepository;
    private final ScheduledExecutorService scheduled;

    private static final DateTimeFormatter DATE_FORMATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public NetViewMonitorApp() {
        this.pingParser = initializeParserSO();
        this.metricsTracker = new MetricsTracker();
        this.csvRepository = new CsvRepository(FILE_NAME, DATE_FORMATE);
        this.scheduled = Executors.newSingleThreadScheduledExecutor();
    }

    // Start the background monitoring loop.
    public void start() {
        System.out.println("=====================================================");
        System.out.println("NET VIEW MONITOR STARTED");
        System.out.println("The file save in: " + FILE_NAME);
        System.out.println("Press Ctrl+C to exit.");
        System.out.println("=====================================================");
        // Execute the 'executeLoop' method every 1 second.
        scheduled.scheduleAtFixedRate(this::executeLoop, 0, 1, TimeUnit.SECONDS);
    }

    // Method to detect the user's SO and inject the correct interface
    private PingParser initializeParserSO() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return new WindowsPingParser(HOST_TARGET);
        } else {
            return new LinuxPingParser(HOST_TARGET);
        }
    }

    // Single execution: get ping, calculate metrics and save
    private void executeLoop() {
        try {
            PingResult result = pingParser.runPing();
            metricsTracker.register(result);
            csvRepository.log(result, metricsTracker);
        } catch (Exception e) {
            System.err.println("Unexpected error in monitoring " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new NetViewMonitorApp().start();
    }
}
