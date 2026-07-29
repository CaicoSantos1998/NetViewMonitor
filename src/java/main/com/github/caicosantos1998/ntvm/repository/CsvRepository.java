package java.main.com.github.caicosantos1998.ntvm.repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.main.com.github.caicosantos1998.ntvm.model.PingResult;
import java.main.com.github.caicosantos1998.ntvm.service.MetricsTracker;
import java.time.format.DateTimeFormatter;

// The class responsible for writing the monitoring data to the CSV file.
public class CsvRepository {
    private final String filePath;
    private final DateTimeFormatter dateTimeFormatter;

    public CsvRepository(String filePath, DateTimeFormatter dateTimeFormatter) {
        this.filePath = filePath;
        this.dateTimeFormatter = dateTimeFormatter;
        initializeHeader();
    }

    // Create the file header if it does not already exist
    private void initializeHeader() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
                writer.println("Hour,Status,Ping_ms,min_Ping,max_Ping,average_Ping");
            } catch (IOException e) {
                System.err.println("Failed to Initialize the CSV file :" + e.getMessage());
            }
        }
    }

    // Records a new log entry containing the ping data and current metrics.
    public void log(PingResult result, MetricsTracker tracker) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            String timeStampFormate = result.timeStamp().format(dateTimeFormatter);
            String row = String.format("%s,%s,%d,%d,%d,%d", timeStampFormate, result.status().name(),
                    result.latencyMs(), tracker.getMinPing(), tracker.getMaxPing(), tracker.getAveragePing());
            writer.println(row);
        } catch (IOException e) {
            System.err.println("Error writing data to the file: " + e.getMessage());
        }
    }
}
