

//AIzaSyCHP0qbsd9diH0ix7Z_PFw2xJYlkQbMvDc
import com.oracle.javafx.jmx.json.JSONException;
import com.sun.rowset.internal.Row;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import static java.lang.System.out;
import static java.lang.System.setOut;


public class DrivingTimeMatrix {

   static String initialFile = "cordFile.txt";
   static ArrayList<Coordinate> coordinateList = new ArrayList<>();



    public DrivingTimeMatrix() throws FileNotFoundException {

    }

    public static void main(String[] args) throws IOException , JSONException {
        lookUpCoordinates();
        getDrivingTimes();
        out.println("Driving Matrix successfully created");
    }

    public static void lookUpCoordinates() throws FileNotFoundException {
        File inputFile = new File(initialFile);
        Scanner in = new Scanner(inputFile);
        int id = 0;
        while (in.hasNextLine()){
            String line = in.nextLine();
            String[] lineInput = line.split(",");
            Coordinate cord = new Coordinate();
            cord.setLatitude(Double.parseDouble(lineInput[0]));
            cord.setLongitude(Double.parseDouble(lineInput[1]));
            cord.setId(id);
            id++;
            coordinateList.add(cord);
        }
        in.close();
    }

    public static void getDrivingTimes() throws IOException, JSONException {
        int numberOfQueries10sek = 0;

        //Start fra input
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("testFile.txt", true));
            PrintWriter out = new PrintWriter(writer))
        {
            ArrayList<ArrayList<Double>> list = new ArrayList<>();
            int counterWriter = 0;
            for (Coordinate origin : coordinateList) {
                int counter = 0;
                if(counterWriter <= 25){
                    counterWriter += 1;
                    System.out.println(counterWriter);
                    continue;
                }
                ArrayList<Double> distanceList = new ArrayList<>();
                for (Coordinate destination : coordinateList) {
                    if (origin.id == destination.id) {
                        distanceList.add(0.0);
                    } else {
                        if (numberOfQueries10sek > 99) {
                            try {
                                System.out.println("Execution sleeps for 1 seconds");
                                Thread.currentThread().sleep(1 * 1000);
                                numberOfQueries10sek = 0;

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //out.println(counter);
                        counter++;
                        int drivingTimeSek = getDrivingTimeBetweenCoordinates(origin, destination);
                        numberOfQueries10sek++;
                        double drivingTimeMin = ((double) drivingTimeSek) / 60;
                        drivingTimeMin = Math.round(drivingTimeMin * 100.0) / 100.0;
                        distanceList.add(drivingTimeMin);

                    }
                }
                list.add(distanceList);
                System.out.println("Writes to file");
                out.println(distanceList.toString());
                counterWriter++;
                System.out.println(counterWriter);

            }
        }
        catch(IOException e){
            System.out.println(e);
        } catch (JSONException e){
            System.out.println(e);
        }
    }



    private static int getDrivingTimeBetweenCoordinates(Coordinate origin, Coordinate destination) throws IOException, JSONException, org.json.JSONException {
        double originLongitude = origin.getLongitude();
        double originLatitude = origin.getLatitude();
        double destinationLongitude = destination.getLongitude();
        double destinationLatitude = destination.getLatitude();
        String urlRequestGoogleMaps = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + originLatitude + "," + originLongitude + "&destinations=" + destinationLatitude + "," + destinationLongitude + "&mode=bicycling&language=en-EN&key=AIzaSyAxdMRQLj-4T6EoYL34zgVEet8G3UGb0kc";
        JSONObject json = readJsonFromUrl(urlRequestGoogleMaps);
        return getDrivingDurationAsInt(json);
    }

    private static JSONObject readJsonFromUrl(String urlRequestGoogleMaps) throws IOException, JSONException, org.json.JSONException {
        InputStream is = new URL(urlRequestGoogleMaps).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static int getDrivingDurationAsInt(JSONObject json) throws JSONException, org.json.JSONException {

        JSONArray rows = json.getJSONArray("rows");
        JSONObject firstObjectRows = rows.getJSONObject(0);
        JSONArray elements = firstObjectRows.getJSONArray("elements");
        JSONObject firstObjectElements = elements.getJSONObject(0);
        JSONObject duration = firstObjectElements.getJSONObject("duration");
        return Integer.parseInt(duration.get("value").toString());
    }



}
