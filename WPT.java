public class WPT {
    double latitude;
    double longitude;
    float elevation;
    long time;

    public WPT(double lat, double lon, float elev, long t) {
        this.latitude = lat;
        this.longitude = lon;
        this.elevation = elev;
        this.time = t;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    public float getElevation() {
        return this.elevation;
    }

    public void setElevation(float elev) {
        this.elevation = elev;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long t) {
        this.time = t;
    }

}
