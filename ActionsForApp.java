import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ActionsForApp extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    public static ArrayList<File> route_list = new ArrayList<>();
    String user;
    String route_file_name;
    int files_sent = 0;


    public ActionsForApp(Socket connection) {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            receiveFile();
            while (true) {
                int choice = in.readInt();
                if (choice == 1) {
                    receiveFile();
                }
                else if (choice == 2){
                    sendResults();
                }
                else {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /* Receive file from App */
    private void receiveFile() {
        try {
            route_file_name = in.readUTF();

            int bytes;
            FileOutputStream fileOutputStream = new FileOutputStream(route_file_name);

            long size = in.readLong(); // read file size
            byte[] buffer = new byte[4 * 1024];
            while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {

                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes; // read up to file size
            }

            System.out.println("File received from App");
            fileOutputStream.close();

            files_sent++;

            File route_file = new File(route_file_name);

            Scanner gpx = new Scanner(route_file);
            // Find user
            gpx.nextLine();
            String data = gpx.nextLine();
            int user_index = data.indexOf("creator");
            user = data.substring(user_index + 9, data.length() - 2);

            gpx.close();

            route_list.add(route_file);

            System.out.println("Sent file to Actions for Workers");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* Send the results to App */
    private void sendResults() {
        try {
            // accept results from ActionsForWorkers
            boolean returned = false;
            while (!returned) {
                for (User each_user : ActionsForWorkers.getUsersList()) {
                    if (each_user.getUsername().equals(user) && each_user.getRoutes().size() == files_sent) {
                        for (Route route : each_user.getRoutes()) {
                            if ((route.getRoute_name() + ".gpx").equals(route_file_name)) {
                                out.writeObject(route);
                                out.flush();

                                out.reset();

                                out.writeObject(each_user);
                                out.flush();

                                returned = true;
                                break;
                            }
                        }
                    }
                }
            }
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /* Get the first file on the list and remove it */
    public synchronized static File getFile() {
        if (!route_list.isEmpty()) {
            File file = route_list.get(0);
            route_list.remove(0);
            return file;
        }
        else {
            return null;
        }
    }
}
