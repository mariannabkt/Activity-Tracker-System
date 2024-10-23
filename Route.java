import java.io.Serializable;

public class Route implements Serializable {

    String user;
    String route_name;
    double total_distance;
    long total_time;
    float total_ascent;
    double average_speed;

    public Route(String user, String route_name) {
        this.user = user;
        this.route_name = route_name;
        this.total_distance = 0;
        this.total_time = 0;
        this.total_ascent = 0;
        this.average_speed = 0;
    }

    public String getUser() {
        return user;
    }

    public String getRoute_name() {
        return route_name;
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
