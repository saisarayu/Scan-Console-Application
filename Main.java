import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ScanController scanController =
                new ScanController();

        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Scan Application Started.");
            System.out.println("Available commands:");
            System.out.println("add:<id>, <name>, <duration>, <pause>");
            System.out.println("view");
            System.out.println("start");
            System.out.println("stop");
            System.out.println("remove:<id>");
            System.out.println("exit");
            System.out.println();

            while (true) {
                String command = sc.nextLine();
                scanController.handleCommand(command);
            }
        }
    }
}