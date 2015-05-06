package jimjams.airmonitor.database;

import android.provider.BaseColumns;

/**
 * AirMonitor database contract.
 */
public interface AMDBContract {

    /**
     * Database path
     */
    String DB_DIR = "/data/data/jimjams.airmonitor";

    /**
     * Database name
     */
    String DB_NAME = "AirMonitor";

    /**
     * Database filename
     */
    String DB_FILENAME = DB_NAME + ".db";

    /**
     * Database version number
     */
    int DB_VERSION = 1;

    /**
     * "Create if not exist" string
     */
    String CREATE = "CREATE TABLE IF NOT EXISTS ";

    /**
     * "Drop table" string
     */
    String DROP = "DROP TABLE IF EXISTS ";

    /**
     * <p>Database table containing Profile data. Note that the id column will <b>not</b> be
     *    incremented; it will initially be 0 and may be updated at a later time if server-side
     *    data storage is implemented.</p>
     * <p>Columns in the table:</p>
     * <dl>
     *     <dt>id</dt>
     *     <dd>Unique id for the record</dd>
     *     <dt>conditions</dt>
     *     <dd>Existing conditions, formatted as a semicolon-separated String</dd>
     * </dl>
     */
    final class ProfileTable implements BaseColumns {

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
     *     <dt>id</dt>
     *     <dd>Unique id for the record</dd>
     *     <dt>indoors</dt>
     *     <dd>true if the user indicates that s/he is indoors; false otherwise</dd>
     *     <dt>reportedLocation</dt>
     *     <dd>reportedLocation</dd>
     *     <dt>The user's self-reported location. Not to be confused with the Snapshot's location
     *         field</dt>
     *     <dd>activity</dd>
     *     <dt>The user's self-reported activity at the time of the Snapshot</dt>
     *     <dd>companions</dd>
     *     <dt>The user's report of who s/he is with at the time of the Snapshot, as a
     *         semicolon-separated String</dt>
     *     <dd>airQuality</dd>
     *     <dt>The user's subjective report of the current air quality, on a scale of 1 to 10</dt>
     *     <dd>belief</dd>
     *     <dt>The user's belief that the current environment will hurt his/her health, on a scale
     *         of 1 to 10</dt>
     *     <dd>intention</dd>
     *     <dt>Likelihood the user will relocate for cleaner air, on a scale of 1 to 10</dt>
     *     <dd>behavior</dd>
     *     <dt>true if the user has changed location for better air since the last report</dt>
     *     <dd>barrier</dd>
     *     <dt>The user's report of what prevented him/her from relocating.</dt>
     * </dl>
     */
    final class EMATable implements BaseColumns {

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
     *     <dt>id</dt>
     *     <dd>Unique id for the record</dd>
     *     <dt>userId</dt>
     *     <dd>ID of the user creating the Snapshot</dd>
     *     <dt>timestamp</dt>
     *     <dd>The time when the Snapshot was taken. Stored as a long</dd>
     *     <dt>latitude</dt>
     *     <dd>Latitude where the Snapshot was taken</dd>
     *     <dt>longitude</dt>
     *     <dd>Longitude where the Snapshot was taken</dd>
     *     <dt>altitude</dt>
     *     <dd>Altitude at which the Snapshot was taken</dd>
     *     <dt>accuracy</dt>
     *     <dd>The estimated accuracy of this location, in meters</dd>
     *     <dt>provider</dt>
     *     <dd>The Provider of the Location</dd>
     *     <dt>sensorDataSensor</dt>
     *     <dd>Sensor Data at the time when the Snapshot was taken. This is a reference to the IDs
     *         of the corresponding records in the SensorData table.</dd>
     *     <dt>conditions</dt>
     *     <dd>Semicolon-separated Strings representing existing conditions reported by the user at
     *         the time of the Snapshot</dd>
     *     <dt>ema</dt>
     *     <dd>Reference to the ID of the corresponding EMA in the EMATable</dd>
     * </dl>
     */
    final class SnapshotTable implements BaseColumns {

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
            { "latitude", "REAL" },
            { "longitude", "REAL" },
            { "altitude", "REAL" },
            { "accuracy", "REAL" },
            { "provider", "TEXT"},
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
     *     <dt>id</dt>
     *     <dd>Unique id for the record</dd>
     *     <dt>displayName</dt>
     *     <dd>Display name for the data</dd>
     *     <dt>shortName</dt>
     *     <dd>Short name for the data</dd>
     *     <dt>value</dt>
     *     <dd>Numerical value of the data</dd>
     *     <dt>displayValue</dt>
     *     <dd>Display value of the reading</dd>
     * </dl>
     */
    final class SensorDataTable implements BaseColumns {

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

    /**
     * <p>Database containing current Sensor data. This is updated in real time as data is read
     *    from the sensors. It is used to populate the sensor data for a new Snapshot.</p>
     * <p>Columns in the table:</p>
     * <dl>
     *     <dt>displayName</dt>
     *     <dd>Display name for the data</dd>
     *     <dt>shortName</dt>
     *     <dd>Short name for the data</dd>
     *     <dt>value</dt>
     *     <dd>Numerical value of the data</dd>
     *     <dt>displayValue</dt>
     *     <dd>Display value of the reading</dd>
     * </dl>
     */
    final class CurrentDataTable implements BaseColumns {

        /**
         * Name of the table
         */
        public static final String TABLE_NAME = "currentData";

        /**
         * Columns in the table
         */
        public static final String[][] COLUMNS = {
            { "displayName", "TEXT PRIMARY KEY" },
            { "shortName", "TEXT" },
            { "value", "DOUBLE" },
            { "displayValue", "TEXT" }
        };
    }

    /**
     * <p>Database used to store the Bluetooth device name.</p>
     * <p>Columns in the table:</p>
     * <dl>
     *     <dt>id</dt>
     *     <dd>Unique id for the record</dd>
     *     <dt>name</dt>
     *     <dd>Name of the device</dd>
     * </dl>
     */
    final class BluetoothDeviceName implements BaseColumns {

        /**
         * Name of the table
         */
        public static final String TABLE_NAME = "bluetoothDevice";

        /**
         * Row ID
         */
        public static final long BLUETOOTH_ID = 1L;

        /**
         * Columns in the table
         */
        public static final String[][] COLUMNS = {
            { "id", "INTEGER PRIMARY KEY" },
            { "name", "TEXT" },
        };
    }

    /**
     * <p>Database used to store the Bluetooth device name.</p>
     * <p>Columns in the table:</p>
     * <dl>
     *     <dt>id</dt>
     *     <dd>Unique id for the record</dd>
     *     <dt>name</dt>
     *     <dd>Name of the device</dd>
     * </dl>
     */
    final class CalibrationLevel implements BaseColumns {

        /**
         * Name of the table
         */
        public static final String TABLE_NAME = "calibrationLevel";

        /**
         * Row ID
         */
        public static final long CALIBRATION_LEVEL_ID = 1L;

        /**
         * Columns in the table
         */
        public static final String[][] COLUMNS = {
            { "id", "INTEGER PRIMARY KEY" },
            { "value", "REAL" },
        };
    }
}