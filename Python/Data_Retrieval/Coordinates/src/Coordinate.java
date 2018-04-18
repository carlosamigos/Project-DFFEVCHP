public class Coordinate {

    public double longitude;
    public double latitude;
    public int id;


    public Coordinate(){

    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {

        this.id = id;
    }
}
