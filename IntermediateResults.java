import java.io.Serializable;

public class IntermediateResults implements Serializable {

    String user;
    String route;
    double total_distance;
    long total_time;
    float total_ascent;
    double average_speed;

    public IntermediateResults(String user, String route, double total_distance, long total_time, float total_ascent, double average_speed) {
        this.user = user;
        this.route = route;
        this.total_distance = total_distance;
        this.total_time = total_time;
        this.total_ascent = total_ascent;
        this.average_speed = average_speed;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
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

    public double getAverage_speed() {
        return average_speed;
    }

    public void setAverage_speed(double average_speed) {
        this.average_speed = average_speed;
    }

}
