import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

public class Mapper extends Thread{
    final ObjectOutputStream out;
    String fileName;
    String filePath;
    boolean completed = false;

    Mapper(ObjectOutputStream out, String fileName, String filePath) {
        this.out = out;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public void run() {

        try {
            File chunk = new File(System.getProperty("user.dir") + "\\worker_directory\\" + fileName);

            String chunk_name = chunk.getName();
            String chunk_abs_path = chunk.getAbsolutePath();
            int chunk_name_index = chunk_abs_path.indexOf(chunk_name);
            String chunk_path = chunk_abs_path.substring(0, chunk_name_index);

            IntermediateResults wpt_list = Map(chunk_name, chunk_path);

            System.out.println("Map completed");

            synchronized (out) {
                out.writeObject(wpt_list);
                out.flush();
                completed = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IntermediateResults Map(String file_name, String file_path) {
        // find user
        int user_index = file_name.indexOf("user");
        int route_index = file_name.indexOf("route");
        String user = file_name.substring(user_index, route_index - 1);

        String route_name = file_name.substring(route_index, file_name.length() - 4);

        ArrayList<WPT> waypoint_chunk = new ArrayList<>();
        try {
            // Read file
            File gpx_file = new File(file_path + file_name);
            Scanner gpx = new Scanner(gpx_file);

            // Read waypoints
            while (gpx.hasNextLine()) {
                String data = gpx.nextLine();

                // Get Latitude and Longitude
                int lat_index = data.indexOf("lat");
                int lon_index = data.indexOf("lon");

                double lat = Double.parseDouble(data.substring(lat_index + 5, lon_index - 2));
                double lon = Double.parseDouble(data.substring(lon_index + 5, data.length() - 2));

                // Get Elevation
                data = gpx.nextLine();
                int ele_index = data.indexOf("ele");

                float ele = Float.parseFloat(data.substring(ele_index + 4, data.length() - 6));

                // Get Time
                data = gpx.nextLine();
                int time_index = data.indexOf("time");

                String time = data.substring(time_index + 5, data.length() - 7);
                String new_time = time.replace("T", " ").replace("Z", "");
                Timestamp timestamp = Timestamp.valueOf(new_time);
                long final_time = timestamp.getTime();

                // Create save the data in a wpt object
                WPT rec = new WPT(lat, lon, ele, final_time);
                waypoint_chunk.add(rec);

                // End of Waypoint
                gpx.nextLine();
            }
            gpx.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return calculate_inter_results(user, route_name, waypoint_chunk);
    }

    private IntermediateResults calculate_inter_results(String user, String key, ArrayList<WPT> wpt_list) {
        double total_distance = calculate_distance(wpt_list);
        long total_time = calculate_time(wpt_list);
        float total_ascent = calculate_ascent(wpt_list);
        double average_speed = calculate_aver_speed(total_distance, total_time);

        return new IntermediateResults(user, key, total_distance, total_time, total_ascent, average_speed);
    }

    private double calculate_distance(ArrayList<WPT> wpt_list) {
        double total_distance = 0;
        for (int i = 0; i < wpt_list.size() - 1; i++) {
            total_distance += distance(wpt_list.get(i).getLatitude(), wpt_list.get(i + 1).getLatitude(), wpt_list.get(i).getLongitude(), wpt_list.get(i + 1).getLongitude(), wpt_list.get(i).getElevation(), wpt_list.get(i + 1).getElevation());
        }
        return total_distance;
    }

    private static double distance(double latitude1, double latitude2, double longitude1, double longitude2, float elev1, float elev2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(latitude2 - latitude1);
        double lonDistance = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        float height = elev1 - elev2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private long calculate_time(ArrayList<WPT> wpt_list) {
        long total_time = 0;

        for (int i = 0; i < wpt_list.size() - 1; i++) {
            total_time += wpt_list.get(i + 1).getTime() - wpt_list.get(i).getTime();
        }

        return total_time;
    }

    private float calculate_ascent(ArrayList<WPT> wpt_list) {
        float total_ascent = 0;

        for (int i = 0; i < wpt_list.size() - 1; i++) {
            total_ascent += ascent(wpt_list.get(i).getElevation(), wpt_list.get(i + 1).getElevation());
        }

        return total_ascent;
    }

    private float ascent(float elev1, float elev2) {
        if (elev2 > elev1) {
            return elev2 - elev1;
        }
        return 0;
    }

    private double calculate_aver_speed(double total_distance,long total_time) {
        double time_m = (double) total_time / (1000 * 60); // convert from milliseconds to minutes
        return total_distance / time_m;
    }

    public boolean isCompleted() {
        return completed;
    }

}
