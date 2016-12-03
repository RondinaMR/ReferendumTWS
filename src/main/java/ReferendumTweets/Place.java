package ReferendumTweets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by marco on 28/11/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place {
    private GeoLocation[][] boundingBoxCoordinates;
    private String boundingBoxType;
    private Place cointainedWithIn;
    private String country;
    private String countryCode;
    private String fullName;
    private GeoLocation[][] geometryCoordinates;
    private String geometryType;
    private String id;
    private String name;
    private String placeType;
    private String streetAddress;
    private String URL;

    public Place() {
    }

    public Place(twitter4j.Place place) {

        this.country = place.getCountry();
        this.countryCode = place.getCountryCode();
        this.fullName = place.getFullName();

        this.id = place.getId();
        this.name = place.getName();
        this.placeType = place.getPlaceType();
        this.streetAddress = place.getStreetAddress();
        //Importing cointainedWithin
        if(place.getContainedWithIn()!=null){
            importingCointainedPlace(place);
        }
        //Importing boundingBoxCoordinates
        if(place.getBoundingBoxCoordinates()!=null){
            this.boundingBoxType = place.getBoundingBoxType();
            this.boundingBoxCoordinates = importingGeoLocation(place.getBoundingBoxCoordinates());
        }
        //Importing GeometryCoordinates
        if(place.getGeometryCoordinates()!=null){
            this.geometryCoordinates = importingGeoLocation(place.getGeometryCoordinates());
            this.geometryType = place.getGeometryType();
        }
        if(place.getURL()!=null){
            this.URL = place.getURL();
        }
    }

    private Place importingCointainedPlace(twitter4j.Place place){
        Place p = new Place(place);
        return p;
    }

    private GeoLocation[][] importingGeoLocation(twitter4j.GeoLocation[][] gl){
        int v = gl.length;
        GeoLocation m[][] = new GeoLocation[v][];
        int h;
        int i = 0;
        int k = 0;
        for(twitter4j.GeoLocation[] gv : gl){
            h = gv.length;
            m[i] = new GeoLocation[h];
            for(twitter4j.GeoLocation g : gv){
                m[i][k] = new GeoLocation(g.getLatitude(),g.getLongitude());
                k++;
            }
            k=0;
            i++;
        }
        return m;
    }

    public GeoLocation[][] getBoundingBoxCoordinates() {
        return boundingBoxCoordinates;
    }

    public String getBoundingBoxType() {
        return boundingBoxType;
    }

    public Place getCointainedWithIn() {
        return cointainedWithIn;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getFullName() {
        return fullName;
    }

    public GeoLocation[][] getGeometryCoordinates() {
        return geometryCoordinates;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlaceType() {
        return placeType;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getURL() {
        return URL;
    }


}
