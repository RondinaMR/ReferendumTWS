package ReferendumTweets;

/**
 * Created by marco on 28/11/2016.
 */
public class GeoLocation {
    private double latitude;
    private double longitude;

    public GeoLocation() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "GeoLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
