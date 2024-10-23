import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class App extends Thread {
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    Socket requestSocket = null;
    String server_ip;

    /* Gets the IP address of the Server */
    public static void main(String[] args){
        String ipArg = "";
        if (args.length == 1) {
            try {
                ipArg = args[0];
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be the IP address of the Server.");
                System.exit(1);
            }
        }
        else {
            System.err.println("You must give the IP address of the server.");
            return;
        }
        new App(ipArg).start();
    }

    public App(String server_ip) {
        this.server_ip = server_ip;
    }

    public void run() {

        try {
            /* Create socket for contacting the server on port 4321 */
            requestSocket = new Socket(server_ip, 4321);
            System.out.println("Connected to sever.");

            /* Create the streams to send and receive data from server */
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());


            // ------------------------------------- GET FILE & SEND IT TO MASTER -------------------------------------
            Scanner keyboard = new Scanner(System.in);
            System.out.println("Please write the name of the file: ");
            String GPXName = keyboard.nextLine();

            uploadFile(GPXName);


            // ------------------------------------------------- MENU -------------------------------------------------
            while (true) {
                System.out.println("Menu");
                System.out.println("----");
                System.out.println("Type \"1\" if you want to upload a new file");
                System.out.println("Type \"2\" if you want to see your results");
                System.out.println("Type \"3\" if you want to leave the app");
                int choice = keyboard.nextInt();
                keyboard.nextLine();
                if (choice == 1) {
                    out.writeInt(choice);
                    System.out.println("Please write the name of the file: ");
                    GPXName = keyboard.nextLine();

                    uploadFile(GPXName);
                }
                else if (choice == 2) {
                    out.writeInt(choice);
                    out.flush();
                    printResults();
                }
                else if (choice == 3){
                    out.writeInt(choice);
                    out.flush();
                    break;
                }
                else {
                    System.out.println("This is not an available option. Please try again.");
                }
            }
        } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (requestSocket != null) {
                    requestSocket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /* Send file to Master */
    private void uploadFile(String GPXName) {
        try {
            String GPXPath = System.getProperty("user.dir") + "\\xml_files\\";

            out.writeUTF(GPXName);
            out.flush();

            File route_file = new File(GPXPath + GPXName);
            FileInputStream fileInputStream = new FileInputStream(route_file);

            int bytes;
            out.writeLong(route_file.length());

            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytes);
                out.flush();
            }

            fileInputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /* Receive and print the results */
    private void printResults() {
        try {
            Route route = (Route) in.readObject();
            System.out.println("Getting your results..\n");

            System.out.println("Your results on this route: ");
            System.out.println("Your total distance: " + Math.floor((route.getTotal_distance() / 1000) * 100) / 100 + " km");
            System.out.println("Your average speed: " + (int) route.getAverage_speed() + " m/min");
            System.out.println("Your total ascent: " + Math.floor(route.getTotal_ascent() * 100) / 100 + " m");
            System.out.println("Your total time: " + route.getTotal_time() / 1000 / 60 + " min\n");

            User user = (User) in.readObject();
            System.out.println("Your results on all routes: ");
            System.out.println("Your total time: " + user.getTotal_time() / 1000 / 60 + " min");
            System.out.println("Your total distance: " + Math.floor((user.getTotal_distance() / 1000) * 100) / 100 + " km");
            System.out.println("Your total ascent: " + Math.floor(user.getTotal_ascent() * 100) / 100 + " m");
            System.out.println("Your average time: " + user.getAverage_time() / 1000 / 60 + " min");
            System.out.println("Your average distance: " + Math.floor((user.getAverage_distance() / 1000) * 100) / 100 + " m");
            System.out.println("Your average ascent: " + Math.floor(user.getAverage_ascent() * 100) / 100 + " m\n");

            if (user.getPercentage_time() < 0) {
                System.out.println("You rαn " + Math.abs(user.getPercentage_time()) + "% less minutes than the average");
            }
            else {
                System.out.println("You rαn " + user.getPercentage_time() + "% more minutes than the average");
            }
            if (user.getPercentage_distance() < 0) {
                System.out.println("You rαn " + Math.abs(user.getPercentage_distance()) + "% less distance than the average");
            }
            else {
                System.out.println("You rαn " + user.getPercentage_distance() + "% more distance than the average");
            }
            if (user.getPercentage_ascent() < 0) {
                System.out.println("You ascended " + Math.abs(user.getPercentage_ascent()) + "% less than the average");
            }
            else {
                System.out.println("You ascended " + user.getPercentage_ascent() + "% more than the average\n\n\n");
            }
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }
}
