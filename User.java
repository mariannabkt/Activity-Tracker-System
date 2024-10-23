import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    String username;
    double total_distance;
    long total_time;
    float total_ascent;
    ArrayList<Route> routes;
    long average_time;
    double average_distance;
    float average_ascent;
    int percentage_time;
    int percentage_distance;
    int percentage_ascent;

    public User(String username) {
        this.username = username;
        this.total_distance = 0;
        this.total_time = 0;
        this.total_ascent = 0;
        this.routes = new ArrayList<>();
        this.average_time = 0;
        this.average_distance = 0;
        this.average_ascent = 0;
        this.percentage_time = 0;
        this.percentage_distance = 0;
        this.percentage_ascent = 0;
    }

    public String getUsername() {
        return username;
    }

    public double getTotal_distance() {
        return total_distance;
    }

    public void setTotal_distance(double total_distance) {
        this.total_distance = total_distance;
    }

    public long getTotal_time() {
        return total_time;
    }

    public void setTotal_time(long total_time) {
        this.total_time = total_time;
    }

    public float getTotal_ascent() {
        return total_ascent;
    }

    public void setTotal_ascent(float total_ascent) {
        this.total_ascent = total_ascent;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public void addRoute(Route route) {
        this.routes.add(route);
    }

    public long getAverage_time() {
        return average_time;
    }

    public void setAverage_time(long average_time) {
        this.average_time = average_time;
    }

    public double getAverage_distance() {
        return average_distance;
    }

    public void setAverage_distance(double average_distance) {
        this.average_distance = average_distance;
    }

    public float getAverage_ascent() {
        return average_ascent;
    }

    public void setAverage_ascent(float average_ascent) {
        this.average_ascent = average_ascent;
    }

    public int getPercentage_time() {
        return percentage_time;
    }

    public void setPercentage_time(int percentage_time) {
        this.percentage_time = percentage_time;
    }

    public int getPercentage_distance() {
        return percentage_distance;
    }

    public void setPercentage_distance(int percentage_distance) {
        this.percentage_distance = percentage_distance;
    }

    public int getPercentage_ascent() {
        return percentage_ascent;
    }

    public void setPercentage_ascent(int percentage_ascent) {
        this.percentage_ascent = percentage_ascent;
    }
}
