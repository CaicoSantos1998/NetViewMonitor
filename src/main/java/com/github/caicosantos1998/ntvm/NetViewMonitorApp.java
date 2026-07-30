package main.java.com.github.caicosantos1998.ntvm;

import main.java.com.github.caicosantos1998.ntvm.model.PingResult;
import main.java.com.github.caicosantos1998.ntvm.repository.CsvRepository;
import main.java.com.github.caicosantos1998.ntvm.service.LinuxPingParser;
import main.java.com.github.caicosantos1998.ntvm.service.MetricsTracker;
import main.java.com.github.caicosantos1998.ntvm.service.PingParser;
import main.java.com.github.caicosantos1998.ntvm.service.WindowsPingParser;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// The main class responsible for orchestrate the entire monitoring system
public class NetViewMonitorApp {
    // Internal record to group the context of each monitored target.
    private record MonitorContext(
            PingParser pingParser,
            MetricsTracker tracker,
            CsvRepository csv,
            String hostTarget,
            String fileName
    ) {}

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final ScheduledExecutorService scheduled;
    private final List<MonitorContext> monitors;

    public NetViewMonitorApp() {
        this.monitors = new ArrayList<>();
        // Configures the connections to be tracked simultaneously.
        configMonitor("1.1.1.1", "internet_geral.csv");
        configMonitor("gateway.discord.gg", "ping_discord.csv");
        configMonitor("://ea.com", "ping_ea_sports.csv");
        configMonitor("steampowered.com", "ping_counter_strike2.csv");
        configMonitor("account-public-service-prod03.ol.epicgames.com",
                "ping_rocket_league.csv");
        this.scheduled = Executors.newScheduledThreadPool(monitors.size());
    }

    // Start the background monitoring loop.
    public void start() {
        System.out.println("=====================================================");
        System.out.println("NET VIEW MONITOR STARTED");
        System.out.println("=====================================================");
        for (MonitorContext context : monitors) {
            System.out.println(" -> Monitoring: " + context.hostTarget() + " |Saving in: " + context.fileName());
            // Execute the 'executeLoop' method every 1 second.
            scheduled.scheduleAtFixedRate(() -> executeLoop(context), 0, 1, TimeUnit.SECONDS);
        }
        System.out.println("Press Ctrl+C to exit.");
        System.out.println("=====================================================");
    }

    // Method to detect the user's SO and inject the correct interface
    private PingParser initializeParserSO(String hostTarget) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return new WindowsPingParser(hostTarget);
        } else {
            return new LinuxPingParser(hostTarget);
        }
    }

    // Single execution: get ping, calculate metrics and save
    private void executeLoop(MonitorContext context) {
        try {
            PingResult result = context.pingParser().runPing();
            context.tracker().register(result);
            context.csv().log(result, context.tracker());
        } catch (Exception e) {
            System.err.println("Unexpected error in monitoring " + context.hostTarget() + ": " + e.getMessage());
        }
    }

    // Initializes the individual components for a specific address and stores them in the list.
    private void configMonitor(String hostTarget, String fileName) {
        PingParser parser = initializeParserSO(hostTarget);
        MetricsTracker tracker = new MetricsTracker();
        CsvRepository repository = new CsvRepository(fileName, DATE_FORMAT);

        monitors.add(new MonitorContext(parser, tracker, repository, hostTarget, fileName));
    }

    public static void main(String[] args) {
        new NetViewMonitorApp().start();
    }
}