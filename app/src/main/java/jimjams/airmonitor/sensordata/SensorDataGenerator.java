package jimjams.airmonitor.sensordata;

import java.util.ArrayList;

import jimjams.airmonitor.database.DBAccess;

/**
 * Generates random sensor sensor data to test app. This class uses the singleton pattern and a
 * private constructor. Use {@link #getInstance()} to access the SensorDataGenerator object.
 */
public class SensorDataGenerator {

    /**
     * The current instance of SensorDataGenerator
     */
    private static SensorDataGenerator instance = null;

    /**
     * Access to the database
     */
    private DBAccess access = DBAccess.getDBAccess();

    /**
     * Constructor. Private to prevent unauthorized instantiation.
     */
    private SensorDataGenerator() {
    }

    /**
     * Returns the current instance of SensorDataGenerator.
     * @return The current instance of SensorDataGenerator
     */
    public static SensorDataGenerator getInstance() {
        if(instance == null) {
            instance = new SensorDataGenerator();
        }
        return instance;
    }

    /**
     * Returns sensor data as an array of SensorData objects. Categories for which there is no data
     * are not returned.
     * @return Sensor data
     */
    public ArrayList<SensorData> getData(String sensorData) {
        ArrayList<SensorData> data = new ArrayList<>();
        // PARSE string with ';' as the delimiter between different sensors (set on Arduino code) JPM
        String[] tokens = sensorData.split(";");

        //for(DataCategory dataCat: dataCats) {
        for (String value : tokens) {
            //FURTHER parse tokens in to bits using ':' delimiter breaking single sensor
            // data in to name, value and units (array 0, 1 and 2 respectively [set in Arduino code])
            String[] bits = value.split(":");
            // ADD data to List by INSTANTIATING SensorData object
            data.add(new SensorData(bits[0], bits[0], Double.parseDouble(bits[1]), 0, bits[2]));
        }
        // RETURN ArrayList containing sensor data
        return data;

    }

    /**
     * Returns sensor data as an array of SensorData objects. Categories for which there is no data
     * are not returned.
     * @return Sensor data
     */
    public ArrayList<SensorData> getData() {
        return access.getCurrentData();
    }
}