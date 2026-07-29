# NetViewMonitor
This is a project to monitor network performance.
Thie application runs in the background. Developed in Java 21.
The focus of this project is to collect accurate latency(ping) metrics - to identify packet loss or brief connection drops - and generate reports in CSV format for analysis.

## The Problem & Motivation
During online gaming sessions (specifically in EA Sports 26) between 5 PM and 10 PM, I noticed frequent disconnections and severe fluctuations in match stability. To investigate whether the issue lay with the ISP's routing or the central servers, I developed this continuous monitoring tool. Unlike standard web-based speed tests, this application sends high-precision pings at strict one-second intervals, making it possible to detect minute drops and micro-interruptions that traditional web tools miss.

## Architecture and Best Practices (S.O.L.I.D.)
The project was built from scratch with a focus on industry standards and clean architecture: Single Responsibility Principle (SRP): Decoupled classes where the network parser, mathematical aggregator, and data layer perform strictly isolated tasks. Polymorphism & Extensibility: Use of interfaces (e.g., `PingParser`) to enable native operation on both Windows and Linux by detecting the host OS at runtime. Type Safety: Implementation of Enums and Java Records for immutable and type-safe data modeling. Dependency Inversion: Date formats and file outputs are injected via the main orchestrator class. Thread Safety: Use of `ScheduledExecutorService` for precise timing loops (avoiding the inaccuracy of `Thread.sleep`) and concurrent data synchronization.

## Packege structure
netviewmonitor/

├── model/

│     ├── ConnectionStatus.java   # State Enum (ONLINE, TIMEOUT, OFFLINE).

│     └── PingResult.java      # Record immutable of coolected data.

├── repository/

│     └── CsvRepository.java   # Log file persistence and management

├── service/

│     ├── PingParser.java      # Interface

│     ├── WindowsPingParser.java

│     ├── LinuxPingParser.java

│     └── MetricsTracker.java  # Calculation engine (Average, Maximum, and Minimum)

└── NetworkMonitorApp.java   # System entry point and orchestrator

## Output format (CSV)
Time, Status, Ping_ms, Lowest_Ping, Highest_Ping, Average_Ping

2026-07-29 17:01:05.123,ONLINE,14,14,14,14

2026-07-29 17:01:06.125,ONLINE,18,14,18,16

2026-07-29 17:01:07.130,TIMEOUT,0,14,18,16

2026-07-29 17:01:08.122,OFFLINE,0,14,18,16
