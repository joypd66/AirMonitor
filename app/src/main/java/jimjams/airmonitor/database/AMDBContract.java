package jimjams.airmonitor.database;

import android.provider.BaseColumns;

/**
 * AirMonitor database contract.
 */
public interface AMDBContract {

   /**
    * Database path
    */
   public final static String DB_DIR = "/data/data/jimjams.airmonitor";

   /**
    * Database name
    */
   public final static String DB_NAME = "AirMonitor";

   /**
    * Database filename
    */
   public final static String DB_FILENAME = DB_NAME + ".db";

   /**
    * Database version number
    */
   public final static int DB_VERSION = 1;

   /**
    * "Create if no exist" string
    */
   public final static String CREATE = "CREATE TABLE IF NOT EXISTS ";

   /**
    * Primary key instruction
    */
   // public static final String PRIMARY_KEY = "PRIMARY_KEY(" + "id" + ");";
   public static final String PRIMARY_KEY = "";

   /**
    * <p>Database table containing Profile data. Note that the id column will <b>not</b> be
    * incremented; it will initially be 0 and may be updated at a later time if server-side data
    * storage is implemented.</p>
    * <p>Columns in the table:</p>
    * <dl>
    *    <dt>id</dt>
    *    <dd>Unique id for the record</dd>
    *    <dt>conditions</dt>
    *    <dd>Existing conditions, formatted as a semicolon-separated String</dd>
    * </dl>
    */
   public static final class ProfileTable implements BaseColumns {

      /**
       * Name of the table
       */
      public static final String TABLE_NAME = "profile";

      /**
       * Columns in the table
       */
      public static final String[][] COLUMNS = {
         { "id", "INTEGER PRIMARY KEY" },
         { "conditions", "TEXT" }
      };
   }

   /**
    * <p>Database containing EMA data</p>
    * <p>Columns in the table:</p>
    * <dl>
    *    <dt>id</dt>
    *    <dd>Unique id for the record</dd>
    *    <dt>indoors</dt>
    *    <dd>true if the user indicates that s/he is indoors; false otherwise</dd>
    *    <dt>reportedLocation</dt>
    *    <dd>reportedLocation</dd>
    *    <dt>The user's self-reported location. Not to be confused with the Snapshot's location
    *        field</dt>
    *    <dd>activity</dd>
    *    <dt>The user's self-reported activity at the time of the Snapshot</dt>
    *    <dd>companions</dd>
    *    <dt>The user's report of who s/he is with at the time of the Snapshot, as a
    *        semicolon-separated String</dt>
    *    <dd>airQuality</dd>
    *    <dt>The user's subjective report of the current air quality, on a scale of 1 to 10</dt>
    *    <dd>belief</dd>
    *    <dt>The user's belief that the current environment will hurt his/her health, on a scale
    *        of 1 to 10</dt>
    *    <dd>intention</dd>
    *    <dt>Likelihood the user will relocate for cleaner air, on a scale of 1 to 10</dt>
    *    <dd>behavior</dd>
    *    <dt>true if the user has changed location for better air since the last report</dt>
    *    <dd>barrier</dd>
    *    <dt>The user's report of what prevented him/her from relocating.</dt>
    * </dl>
    */
   public static final class EMATable implements BaseColumns {

      /**
       * Name of the table
       */
      public static final String TABLE_NAME = "ema";

      /**
       * Columns in the table
       */
      public static final String[][] COLUMNS = {
         { "id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
         { "indoors", "BOOLEAN" },
         { "reportedLocation", "TEXT" },
         { "activity", "TEXT" },
         { "companions", "TEXT" },
         { "airQuality", "INTEGER" },
         { "belief", "INTEGER" },
         { "intention", "INTEGER" },
         { "behavior", "BOOLEAN" },
         { "barrier", "TEXT" },
      };
   }

   /**
    * <p>Database containing Snapshot data</p>
    * <p>Columns in the table:</p>
    * <dl>
    *    <dt>id</dt>
    *    <dd>Unique id for the record</dd>
    *    <dt>userId</dt>
    *    <dd>ID of the user creating the Snapshot</dd>
    *    <dt>timestamp</dt>
    *    <dd>The time when the Snapshot was taken. Stored as a long</dd>
    *    <dt>location</dt>
    *    <dd>Location where the Snapshot was taken</dd>
    *    <dt>sensorDataSensor</dt>
    *    <dd>Sensor Data at the time when the Snapshot was taken. This is a reference to the IDs
    *        of the corresponding records in the SensorData table.</dd>
    *    <dt>conditions</dt>
    *    <dd>Semicolon-separated Strings representing existing conditions reported by the user at
    *        the time of the Snapshot</dd>
    *    <dt>ema</dt>
    *    <dd>Reference to the ID of the corresponding EMA in the EMATable</dd>
    * </dl>
    */
   public static final class SnapshotTable implements BaseColumns {

      /**
       * Name of the table
       */
      public static final String TABLE_NAME = "snapshot";

      /**
       * Columns in the table
       */
      public static final String[][] COLUMNS = {
         { "id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
         { "userId", "INTEGER" },
         { "timestamp", "INTEGER" },
         { "location", "TEXT" },
         { "sensorData", "TEXT" },
         { "conditions", "TEXT" },
         { "ema", "INTEGER" }
      };
   }

   /**
    * <p>Database containing Sensor data. Each record contains data from a single sensor at the time
    * of a single Snapshot.</p>
    * <p>Columns in the table:</p>
    * <dl>
    *    <dt>id</dt>
    *    <dd>Unique id for the record</dd>
    *    <dt>displayName</dt>
    *    <dd>Display name for the data</dd>
    *    <dt>shortName</dt>
    *    <dd>Short name for the data</dd>
    *    <dt>value</dt>
    *    <dd>Numerical value of the data</dd>
    *    <dt>getDisplayValue</dt>
    *    <dd>Display value of the reading</dd>
    * </dl>
    */
   public static final class SensorDataTable implements BaseColumns {

      /**
       * Name of the table
       */
      public static final String TABLE_NAME = "sensor";

      /**
       * Columns in the table
       */
      public static final String[][] COLUMNS = {
         { "id", "INTEGER PRIMARY KEY AUTOINCREMENT" },
         { "displayName", "TEXT" },
         { "shortName", "TEXT" },
         { "value", "DOUBLE" },
         { "displayValue", "TEXT" }
      };
   }
}