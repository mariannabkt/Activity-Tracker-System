import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/* Each connection (handler) works on one file */
public class ActionsForWorkers extends Thread{
    Socket connection;
    ObjectInputStream in;
    ObjectOutputStream out;
    public static ArrayList<User> users_list = new ArrayList<>();
    GPXFile current_file;
    static long users_average_time;
    static double users_average_distance;
    static float users_average_ascent;
    int chunks_sent;
    boolean rr_finished = false;

    public ActionsForWorkers(Socket connection) {
        try {
            this.connection = connection;
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            int worker_id = in.readInt();
            Master.setConnections_list(worker_id, this);

            /* While a GPXFile has not been assigned to the Handler */
            while (current_file == null) {
                File file = getRouteFile();

                /* If GPXFile has been assigned */
                if (file != null) {
                    System.out.println("Got file from ActionsForApp");

                    String route_file_name = file.getName();
                    current_file = new GPXFile(route_file_name);

                    System.out.println("Received: " + current_file.getFile_name());

                    /* Assign the file to other handlers and keep the indexes of the handlers that work on the file */
                    int[] connection_index = new int[Master.getWorkersNum()];
                    for (int i = 0; i < Master.getWorkersNum(); i++) {
                        if (i == worker_id) {
                            connection_index[i] = Master.getConnections_list().get(i).size() - 1;
                        }
                        else {
                            synchronized (this) {
                                while (true) {
                                    ActionsForWorkers checking = Master.getConnections_list().get(i).get(Master.getConnections_list().get(i).size() - 1);
                                    if (!checking.hasGPXFile()) {
                                        checking.current_file = this.current_file;
                                        connection_index[i] = Master.getConnections_list().get(i).size() - 1;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    String route_path = file.getAbsolutePath();
                    int route_name_index = route_path.indexOf(route_file_name);
                    String route_file_path = route_path.substring(0, route_name_index);

                    System.out.println("Creating chunks...");

                    List<File> chunks = CreateChunks(route_file_name, route_file_path);
                    current_file.setChunks_num(chunks.size());

                    /* Send the file to the Workers with RoundRobin */
                    int remaining_files = chunks.size();
                    int i = 0;
                    int loop = 0;
                    while (remaining_files > 0) {
                        for (int j = 0; j < Master.getWorkersNum(); j++) {
                            if (remaining_files == 0) {
                                break;
                            }

                            /* Finds the Handler for each Worker */
                            ActionsForWorkers current = Master.getConnections_list().get(j).get(connection_index[j]);

                            if (loop == 0) {
                                current.setSentChunks(1);
                            }
                            else {
                                current.setSentChunks(current.getSentChunks() + 1);
                            }

                            current.out.writeInt(current.getSentChunks());
                            current.out.flush();

                            current.out.writeUTF(chunks.get(i).getName());
                            current.out.flush();

                            /* Send the Chunk file to Worker */
                            int bytes;
                            FileInputStream fileInputStream = new FileInputStream(chunks.get(i));

                            current.out.writeLong(chunks.get(i).length());
                            current.out.flush();

                            byte[] buffer = new byte[4 * 1024];
                            while ((bytes = fileInputStream.read(buffer)) != -1) {
                                current.out.write(buffer, 0, bytes);
                                current.out.flush();
                            }

                            fileInputStream.close();

                            /* Check if this is the last chunk for each Worker */
                            if (remaining_files <= Master.getWorkersNum()) {
                                current.out.writeBoolean(true);
                                current.out.flush();
                            }
                            else {
                                current.out.writeBoolean(false);
                                current.out.flush();
                            }

                            i++;
                            remaining_files--;
                        }
                        loop++;
                    }

                    System.out.println("Sent chunks.");

                    for (int j = 0; j < Master.getWorkersNum(); j++) {
                        ActionsForWorkers current = Master.getConnections_list().get(j).get(connection_index[j]);
                        current.rr_finished = true;
                    }

                    break;
                }
            }

            // ---------------------------------------- GET INTERMEDIATE RESULTS ---------------------------------------
            while (true) {
                System.out.print("");
                if (getSentChunks() != 0 && rr_finished) {
                    for (int i = 0; i < getSentChunks(); i++) {
                        IntermediateResults result = (IntermediateResults) in.readObject();
                        synchronized (current_file) {
                            current_file.getInter_res().add(result);
                        }
                    }
                    break;
                }
            }


            // -------------------------------------------- REDUCE RESULTS --------------------------------------------
            if (current_file.getInter_res().size() == current_file.getChunks_num() && current_file.getInter_res().size() != 0) {
                System.out.println("Reducing...");

                Route final_res = Reduce(current_file.getFile_name(), current_file.getInter_res());

                UpdatePersonalStats(final_res);
                UpdateStats();
                UpdatePercentages(final_res);

                System.out.println("Stats updated.");
            }

        } catch (InterruptedException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            /* When the process of the file is completed close the connection */
            try {
                in.close();
                out.close();
                connection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private synchronized File getRouteFile() throws InterruptedException {
        return ActionsForApp.getFile();
    }

    /* The file is cut into Chunks. Each Chunk contains 4 Waypoints */
    private List<File> CreateChunks(String file_name, String file_path) throws IOException {
        List<File> chunks = new ArrayList<>();

        // Read file
        File gpx_file = new File(file_path + file_name);
        Scanner gpx = new Scanner(gpx_file);

        // Find user
        gpx.nextLine();
        String data = gpx.nextLine();

        int user_index = data.indexOf("creator");
        String user = data.substring(user_index + 9, data.length() - 2);

        if (users_list.size() == 0){
            users_list.add(new User(user));
        }
        else {
            for (User users : users_list) {
                if (!users.getUsername().equals(user)) {
                    users_list.add(new User(user));
                    break;
                }
            }
        }

        // Create chunks
        boolean finished = false;
        int i = 0;
        String last_wpt = "";
        while (!finished) {
            i++;
            File file_chunk = new File("chunk" + i + "-" + user + "-" + file_name);
            FileWriter chunk = new FileWriter("chunk" + i + "-" + user + "-" + file_name);
            for (int j = 0; j < 4; j++) {
                if(!last_wpt.equals("") && j == 0) {
                    chunk.write(last_wpt);
                    j++;
                }

                if(gpx.hasNextLine()) {
                    String wpt_data = gpx.nextLine();

                    // End of file
                    if (Objects.equals(wpt_data, "</gpx>")) {
                        finished = true;
                        break;
                    }

                    String wpt_complete = wpt_data + "\n" + gpx.nextLine() + "\n" + gpx.nextLine() + "\n" + gpx.nextLine() + "\n";
                    chunk.write(wpt_complete);

                    if(j == 3) {
                        last_wpt = wpt_complete;
                    }
                }
            }

            chunk.close();
            chunks.add(file_chunk);
        }
        return chunks;
    }

    private Route Reduce(String file_name, ArrayList<IntermediateResults> all_results) {
        String route = file_name.substring(0, file_name.length() - 4);

        Route final_res = new Route(all_results.get(0).getUser(), route);

        for(IntermediateResults result : all_results) {
            final_res.setTotal_distance(final_res.getTotal_distance() + result.getTotal_distance());
            final_res.setTotal_ascent(final_res.getTotal_ascent() + result.getTotal_ascent());
            final_res.setTotal_time(final_res.getTotal_time() + result.getTotal_time());
        }

        final_res.setAverage_speed(final_res.getTotal_distance() / ((double) final_res.getTotal_time() / 1000 /60));

        return final_res;
    }

    private void UpdatePersonalStats(Route route) {
        String username = route.getUser();

        for (User user : users_list) {
            if (user.getUsername().equals(username)) {
                user.setTotal_distance(user.getTotal_distance() + route.getTotal_distance());
                user.setTotal_time(user.getTotal_time() + route.getTotal_time());
                user.setTotal_ascent(user.getTotal_ascent() + route.getTotal_ascent());

                user.addRoute(route);

                int route_count = user.getRoutes().size();

                user.setAverage_time(user.getTotal_time() / route_count);
                user.setAverage_distance(user.getTotal_distance() / route_count);
                user.setAverage_ascent(user.getTotal_ascent() / route_count);

                break;
            }
        }

    }

    private synchronized void UpdateStats() {
        long total_time = 0;
        double total_distance = 0;
        float total_ascent = 0;
        int total_users = users_list.size();

        for (User user : users_list) {
            total_time += user.getTotal_time();
            total_distance += user.getTotal_distance();
            total_ascent += user.getTotal_ascent();
        }

        users_average_time = total_time / total_users;
        users_average_distance = total_distance / total_users;
        users_average_ascent = total_ascent / total_users;
    }

    private synchronized void UpdatePercentages(Route route) {
        String username = route.getUser();

        for (User user : users_list) {
            if (user.getUsername().equals(username)) {
                user.setPercentage_time((int) ((int) (user.getTotal_time() - users_average_time) / users_average_time) * 100);
                user.setPercentage_distance((int) ((int) (user.getTotal_distance() - users_average_distance) / users_average_distance) * 100);
                user.setPercentage_ascent((int) ((int) (user.getTotal_ascent() - users_average_ascent) / users_average_ascent) * 100);
            }
        }
    }

    public static ArrayList<User> getUsersList() {
        return users_list;
    }

    private boolean hasGPXFile() {
        return current_file != null;
    }

    private int getSentChunks () {
        return chunks_sent;
    }

    private void setSentChunks (int sent_chunks) {
        this.chunks_sent = sent_chunks;
    }
}
