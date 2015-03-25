package jimjams.airmonitor.datastructure;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.Date;
import java.util.List;

import jimjams.airmonitor.sensordata.SensorData;

/**
 * Captures instantaneous environmental data and associates it with a personal
 * Ecological Momentary Assessment (EMA).
 */
public class Snapshot {

   private String className = getClass().getSimpleName();

   /**
    * The ID of the user to whom this Snapshot belongs
    */
   long userId;

   /**
    * The time when the Snapshot was taken
    */
   private Date timestamp;

   /**
    * Location where the Snapshot was taken
    */
   private Location location;

   /**
    * List of SensorData at the time of the Snapshot
    */
   private List<SensorData> data;

   /**
    * List of Existing conditions at the time of the Snapshot
    */
   private List<String> conditions;

   /**
    * EMA at the time of the Snapshot
    */
   private EcologicalMomentaryAssessment ema;

   /**
    * Constructor. The location will be set to null if it cannot be resolved via the Context.
    * @param data Array of SensorData at the time of the Snapshot
    * @param conditions Existing conditions at the time of the Snapshot
    * @param ema EMA at the time of the Snapshot
    * @param context The context to be used to fetch the location. Generally, this is just the
    *                invoking Activity.
    */
   public Snapshot(List<SensorData> data, List<String> conditions,
         EcologicalMomentaryAssessment ema, Context context) {
      this.userId = Profile.getProfile().getId();
      this.data = data;
      this.conditions = conditions;
      this.ema = ema;
      this.timestamp = new Date();
      try {
         LocationManager manager =
               (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
         List<String> providers = manager.getAllProviders();
         // Just take the first one on the list...
         String provider = providers.get(0);
         location = manager.getLastKnownLocation(provider);
         timestamp = new Date(location.getTime());
      }
      catch(NullPointerException | IndexOutOfBoundsException | SecurityException |
            IllegalArgumentException e) {
         location = null;
         timestamp = new Date();
      }
   }

   /**
    * Constructor to be used by DBAccess to build Snapshot from database.
    */
    public Snapshot(long userId, Date timestamp, Location location, List<SensorData> data,
         List<String> conditions, EcologicalMomentaryAssessment ema) {
      this.userId = userId;
      this.timestamp = timestamp;
      this.location = location;
      this.data = data;
      this.conditions = conditions;
      this.ema = ema;
   }


   /**
    * Returns the user ID for this Snapshot.
    * @return The user ID for this Snapshot
    */
   public long userId() {
      return userId;
   }

   /**
    * Returns the Date containing timestamp information for this Snapshot.
    * @return Timestamp for this Snapshot
    */
   public Date getTimestamp() {
      return timestamp;
   }

   /**
    * Returns the existing conditions at the time of the Snapshot.
    * @return Existing conditions
    */
   public List<String> getConditions() {
      return conditions;
   }

   /**
    * Returns sensor data at the time of this Snapshot
    * @return Sensor data
    */
   public List<SensorData> getData() {
      return data;
   }

   /**
    * Returns the location of this Snapshot. Note that this may be null if the location could not be
    * resolved at the time the Snapshot was taken.
    * @return Location of the Snapshot
    */
   public Location getLocation() {
      return location;
   }

   /**
    * Returns the Ecological Momentary Assessment taken with the Snapshot.
    * @return The Snapshot's EMA
    */
   public EcologicalMomentaryAssessment getEma() {
      return ema;
   }

   /**
    * Returns a String representation of the Snapshot.
    * @return String representation of the Snapshot
    */
   public String toString() {
      String result = "timestamp: " + timestamp + "; ";
      result += "location: " + location + "; ";
      result += "conditions: ";
      if(conditions.size() == 0) {
         result += "none";
      }
      for(int i = 0; i < conditions.size(); i++) {
         if(i != 0) {
            result += ", ";
         }
         result += conditions.get(i);
      }
      result += "; ";
      result += ema;
      return result;
   }
}