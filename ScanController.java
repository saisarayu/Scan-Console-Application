import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScanController {

    private final List<Scan> scans = new ArrayList<>();

    // Only one scan can run at a time
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private Future<?> currentTask;
    private Scan currentScan;

    public synchronized void handleCommand(String command) {

        if (command == null || command.trim().isEmpty()) {
            System.out.println("Please enter a command.");
            return;
        }

        command = command.trim();

        try {

            if (command.startsWith("add:")) {

                addScan(command.substring(4));

            } else if (command.equalsIgnoreCase("view")) {

                viewScans();

            } else if (command.equalsIgnoreCase("start")) {

                startScan();

            } else if (command.equalsIgnoreCase("stop")) {

                stopScan();

            } else if (command.startsWith("remove:")) {

                removeScan(command.substring(7));

            } else if (command.equalsIgnoreCase("exit")) {

                exit();

            } else {

                System.out.println("Unknown command.");

            }

        } catch (NumberFormatException e) {

            System.out.println("Invalid number format.");

        } catch (Exception e) {

            System.out.println("Invalid command: " + e.getMessage());
        }
    }

    
    // ADD
    

    private void addScan(String data) {

        String[] parts = data.split(",");

        if (parts.length != 4) {
            System.out.println(
                    "Invalid format. Use: add:<id>, <name>, <duration>, <pause>"
            );
            return;
        }

        int id = Integer.parseInt(parts[0].trim());

        String name = parts[1].trim();

        int duration = Integer.parseInt(parts[2].trim());

        if (duration < 0) {
            System.out.println("Duration cannot be negative.");
            return;
        }

        String pauseValue = parts[3].trim();

        boolean pause;

        if (pauseValue.equalsIgnoreCase("yes")) {
            pause = true;
        } else if (pauseValue.equalsIgnoreCase("no")) {
            pause = false;
        } else {
            System.out.println("Pause must be Yes or No.");
            return;
        }

        // Check duplicate ID
        for (Scan scan : scans) {

            if (scan.getId() == id) {
                System.out.println("Scan ID already exists.");
                return;
            }
        }

        Scan scan = new Scan(
                id,
                name,
                duration,
                pause
        );

        scans.add(scan);

        System.out.println("Added: " + scan);
    }


    // VIEW

    private void viewScans() {

        if (scans.isEmpty()) {
            System.out.println("Current Queue --> Empty");
            return;
        }

        System.out.println("Current Queue -->");

        for (Scan scan : scans) {
            System.out.println(scan);
        }
    }

    // START

    private void startScan() {

        // Check if something is already running
        if (currentScan != null &&
                currentScan.getState() == ScanState.RUNNING) {

            System.out.println(
                    "A scan is already running: "
                            + currentScan.getName()
            );

            return;
        }

        // Find the next scan that has not started
        Scan nextScan = findNextIdleScan();

        if (nextScan == null) {

            System.out.println("No IDLE scans available.");
            return;
        }

        runScan(nextScan);
    }

    // FIND NEXT IDLE SCAN

    private Scan findNextIdleScan() {

        for (Scan scan : scans) {

            if (scan.getState() == ScanState.IDLE) {
                return scan;
            }
        }

        return null;
    }

    // RUN SCAN IN BACKGROUND

    private void runScan(Scan scan) {

        currentScan = scan;

        scan.setState(ScanState.RUNNING);

        System.out.println(
                "Starting " + scan.getName()
        );

        currentTask = executor.submit(() -> {

            try {

                // Simulate scan duration
                Thread.sleep(scan.getDuration() * 1000L);

                synchronized (this) {

                    if (scan.getState() == ScanState.CANCELLED) {
                        return;
                    }

                    scan.setState(ScanState.COMPLETE);

                    System.out.println(
                            "Completed " + scan.getName()
                    );

                    currentScan = null;
                    currentTask = null;

                    if (scan.isPause()) {

                        System.out.println(
                                "Scanning paused. Enter 'start' to continue."
                        );

                        return;
                    }

                    // Automatically start next scan
                    Scan nextScan = findNextIdleScan();

                    if (nextScan != null) {

                        runScan(nextScan);

                    } else {

                        System.out.println(
                                "All scans completed."
                        );
                    }
                }

            } catch (InterruptedException e) {

                synchronized (this) {

                    if (scan.getState() == ScanState.CANCELLED) {

                        System.out.println(
                                "Cancelled " + scan.getName()
                        );
                    }
                }
            }
        });
    }

    // STOP

    private void stopScan() {

        if (currentScan == null ||
                currentScan.getState() != ScanState.RUNNING) {

            System.out.println(
                    "No scan is currently running."
            );

            return;
        }

        Scan scanToStop = currentScan;

        scanToStop.setState(ScanState.CANCELLED);

        System.out.println(
                "Stopping " + scanToStop.getName()
        );

        if (currentTask != null) {

            currentTask.cancel(true);
        }

        currentTask = null;
        currentScan = null;

        // Continue with the next IDLE scan
        Scan nextScan = findNextIdleScan();

        if (nextScan != null) {

            runScan(nextScan);

        } else {

            System.out.println(
                    "No more scans in queue."
            );
        }
    }

    // REMOVE

    private void removeScan(String value) {

        int id = Integer.parseInt(value.trim());

        for (int i = 0; i < scans.size(); i++) {

            Scan scan = scans.get(i);

            if (scan.getId() == id) {

                // Can only remove scans that haven't started
                if (scan.getState() != ScanState.IDLE) {

                    System.out.println(
                            "Cannot remove scan. "
                                    + "It has already started."
                    );

                    return;
                }

                scans.remove(i);

                System.out.println(
                        "Removed scan: " + id
                );

                return;
            }
        }

        System.out.println(
                "Scan not found: " + id
        );
    }

    // EXIT

    private void exit() {

        if (currentTask != null) {

            currentTask.cancel(true);
        }

        executor.shutdownNow();

        System.out.println(
                "Exiting application."
        );

        System.exit(0);
    }
}