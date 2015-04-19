package jimjams.airmonitor.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.location.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import jimjams.airmonitor.datastructure.EcologicalMomentaryAssessment;
import jimjams.airmonitor.datastructure.Profile;
import jimjams.airmonitor.datastructure.Snapshot;
import jimjams.airmonitor.sensordata.SensorData;

/**
 * Allows access to the database
 */
public class DBAccess implements AMDBContract {

    /**
     * The current instance of this class
     */
    private static DBAccess access = null;

    /**
     * The SQLiteDatabase containing saved data for the AirMonitor app
     */
    private SQLiteDatabase database;

    /**
     * Used to identify source class for log
     */
    private String className = getClass().getSimpleName();

    /**
     * Used to separate values in an array stored as a String
     */
    private static final String ARRAY_SEPARATOR = ";";

    /**
     * Constructor. Creates the database if it does not already exist, and populates it with the
     * necessary tables if they do not exist.
     */
    private DBAccess() {

        File path = new File(DB_DIR);

        // Database file
        File dbFile = new File(path, DB_FILENAME);

        // Log.d(className, "File: " + dbFile.getAbsolutePath());
        database = SQLiteDatabase.openOrCreateDatabase(dbFile, null);

        // Make sure the necessary tables exist
        // Profile table
        database.execSQL(getTableCreateString(ProfileTable.TABLE_NAME, ProfileTable.COLUMNS));

        // EMA table
        database.execSQL(getTableCreateString(EMATable.TABLE_NAME, EMATable.COLUMNS));

        // Snapshot table
        database.execSQL(getTableCreateString(SnapshotTable.TABLE_NAME, SnapshotTable.COLUMNS));

        // SensorData table
        database.execSQL(getTableCreateString(SensorDataTable.TABLE_NAME, SensorDataTable.COLUMNS));

        // AirQualityInset sensor data table. This table should be dropped when the app starts, then
        // repopulated from scratch
        database.execSQL(DROP + CurrentDataTable.TABLE_NAME);
        database.execSQL(getTableCreateString(CurrentDataTable.TABLE_NAME,
                CurrentDataTable.COLUMNS));
    }

    /**
     * Generates a String to be passed to generate a table in the database.
     * @param tableName The name of the table
     * @param cols Array of paired column names and types
     * @return String to be passed to generate a table in the database
     */
    private static String getTableCreateString(String tableName, String[][] cols) {
        String result = CREATE + tableName + "(";
        for(int i = 0; i < cols.length; i++) {
            if(i != 0) {
                result += ", ";
            }
            result += cols[i][0] + " " + cols[i][1];
        }
        result += ");";
        return result;
    }

    /**
     * Gets the current instance of DBAccess.
     * @return The current DBAccess
     */
    public static DBAccess getDBAccess() {
        if(access == null) {
            access = new DBAccess();
        }
        return access;
    }

    /**
     * Updates the ProfileTable based on current Profile.
     */
    public void updateProfile() {
        Profile profile = Profile.getProfile();
        ContentValues cv = new ContentValues(2);
        cv.put("id", profile.getId());
        ArrayList<String> conditions = profile.getConditions();
        cv.put("conditions", flatten(conditions));
        database.insertWithOnConflict(ProfileTable.TABLE_NAME, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Saves a set of SensorData to the database.
     * @param data The set of SensorData to be saved
     * @return IDs of the records inserted into the table
     */
    public ArrayList<Long> saveSensorData(ArrayList<SensorData> data) {
        // Create array of IDs to be used for this set of data
        ArrayList<Long> ids = new ArrayList<>(data.size());

        // Insert a new row into the table
        for(int i = 0; i < data.size(); i++) {
            SensorData datum = data.get(i);
            ContentValues cv = new ContentValues(SensorDataTable.COLUMNS.length - 1);
            cv.put("displayName", datum.getDisplayName());
            cv.put("shortName", datum.getShortName());
            cv.put("value", datum.getValue());
            cv.put("displayValue", datum.getDisplayValue());
            ids.add(database.insert(SensorDataTable.TABLE_NAME, null, cv));
        }
        // Return the IDs of the inserted rows
        return ids;
    }

    /**
     * Saves an EcologicalMomentaryAssessment to the database.
     * @param ema The EMA to be saved
     */
    public long saveEMA(EcologicalMomentaryAssessment ema) {
        // Populate the column values
        ContentValues cv = new ContentValues(EMATable.COLUMNS.length - 1);
        cv.put("indoors", ema.getIndoors());
        cv.put("reportedLocation", ema.getReportedLocation());
        cv.put("activity", ema.getActivity());
        cv.put("companions", flatten(ema.getCompanions()));
        cv.put("airQuality", ema.getAirQuality());
        cv.put("belief", ema.getBelief());
        cv.put("intention", ema.getIntention());
        cv.put("behavior", ema.getBehavior());
        cv.put("barrier", ema.getBarrier());

        // Return the id of the inserted row
        return database.insert(EMATable.TABLE_NAME, null, cv);
    }

    public long saveSnapshot(Snapshot snapshot) {
        // Save data structures from Snapshot
        ArrayList<SensorData> data = snapshot.getData();

        // Save this set of data to the database and get their IDs
        ArrayList<Long> sensorIds = saveSensorData(data);
        String sensorIdString = flatten(sensorIds);

        // Get existing conditions as a String
        ArrayList<String> conditions = snapshot.getConditions();
        String conditionString = flatten(conditions);

        // Save the EMA and get its ID
        EcologicalMomentaryAssessment ema = snapshot.getEma();
        // Save the EMA to the database
        long emaId = access.saveEMA(ema);

        // Populate the column values
        ContentValues cv = new ContentValues(SnapshotTable.COLUMNS.length - 1);
        cv.put("userId", Profile.getProfile().getId());
        cv.put("timestamp", snapshot.getTimestamp().getTime());
        Location loc = snapshot.getLocation();
        if(loc != null) {
            cv.put("latitude", loc.getLatitude());
            cv.put("longitude", loc.getLongitude());
            cv.put("provider", loc.getProvider());
        }
        cv.put("sensorData", sensorIdString);
        cv.put("conditions", conditionString);
        cv.put("ema", emaId);

        // Return the id of the inserted row
        return database.insert(SnapshotTable.TABLE_NAME, null, cv);
    }

    /**
     * Converts a List into a String of semicolon-separated values.
     * @param list The List
     * @return List, as a single String
     */
    private static String flatten(ArrayList list) {
        String result = "";
        for(int i = 0; i < list.size(); i++) {
            if(i > 0) {
                result += ARRAY_SEPARATOR;
            }
            result += list.get(i).toString();
        }
        return result;
    }

    /**
     * Gets the ID from the current Profile. If no Profile exists, 0 is returned. If more than one
     * Profile exists, an SQLiteFullException is thrown, as the database should not contain more
     * than one Profile.
     * @return The ID of the current Profile, or 0 if no Profile exists
     * @throws SQLiteFullException if the database contains more than one Profile
     */
    public long getProfileId() throws SQLiteFullException {
        Cursor cursor = database.rawQuery("SELECT id FROM " + ProfileTable.TABLE_NAME, null);
        cursor.moveToFirst();
        long result;
        int count = cursor.getCount();
        // Log.d(className, "cursor count: " + count);
        if(count == 0) {
            result = 0;
        }
        else if(count == 1) {
            result = cursor.getInt(0);
        }
        else {
            throw new SQLiteFullException("Too many Profiles in database.");
        }
        cursor.close();
        return result;
    }

    /**
     * Attempts to populate a List of existing conditions from the Profile table. If no Profile
     * exists, an empty List is returned. If more than one Profile exists, an SQLiteFullException is
     * thrown, as there should not be more than one Profile in the database.
     * @return A List of existing conditions
     * @throws SQLiteFullException
     */
    public ArrayList<String> getProfileConditions() throws SQLiteFullException {
        Cursor cursor = database.rawQuery("SELECT conditions FROM " +
                ProfileTable.TABLE_NAME, null);
        cursor.moveToFirst();
        ArrayList<String> conditions;
        if(cursor.getCount() == 0) {
            conditions = new ArrayList<>();
            // Log.d(className, "No profile in database.");
        }
        else if(cursor.getCount() == 1) {
            String conditionString = cursor.getString(0);
            String[] conditionStrings = conditionString.split(ARRAY_SEPARATOR);

            // Populate manually to ensure you have an ArrayList, as not all List implementations
            // support the remove(E) method
            conditions = new ArrayList<>(conditionStrings.length);
            for(String condString: conditionStrings) {
                conditions.add(condString);
            }
        }
        else {
            throw new SQLiteFullException("Too many Profiles in database.");
        }
        cursor.close();
        return conditions;
    }

    /**
     * Returns a String representation of the specified table in the database.
     * @param dbName The table to be represented
     * @return String representation of the table
     */
    public String toString(String dbName) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + dbName, null);
        String[] cols = cursor.getColumnNames();
        String result = dbName + ":\n";
        for(String col: cols) {
            result += col + "   ";
        }
        result += "\n";
        while(cursor.moveToNext()) {
            for(int i = 0; i < cursor.getColumnCount(); i++) {
                result += cursor.getString(i) + "   ";
            }
            result += "\n";
        }
        cursor.close();
        return result;
    }

    /**
     * Returns a List of all Snapshots associated with a user in the database.
     * @param userId The ID of the user
     * @return All Snapshots in the database
     */
    public ArrayList<Snapshot> getSnapshots(long userId) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + SnapshotTable.TABLE_NAME +
                " WHERE userId = " + userId, null);
        ArrayList<Snapshot> snaps = new ArrayList<>(cursor.getCount());
        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                snaps.add(getSnapshot(cursor.getInt(cursor.getColumnIndex("id"))));
            }
        }
        cursor.close();
        return snaps;
    }

    /**
     * Retrieves a Snapshot from the database. null is returned if there is no Snapshot with the
     * specified ID.
     * @param id The ID of the Snapshot to be retrieved
     * @return The specified Snapshot, or null
     */
    public Snapshot getSnapshot(long id) {
        Snapshot snap = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + SnapshotTable.TABLE_NAME +
                " WHERE id = " + id, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            long userId = cursor.getLong(cursor.getColumnIndex("userId"));
            Date timestamp = new Date(cursor.getLong(cursor.getColumnIndex("timestamp")));

            // Construct the Location, if possible
            Location location = null;

            try {
                int provIndex = cursor.getColumnIndex("provider");
                if(provIndex > -1) {
                    location = new Location(cursor.getString(provIndex));
                }

                int latIndex = cursor.getColumnIndex("latitude");
                if(latIndex > -1) {
                    location.setLatitude(cursor.getDouble(latIndex));
                }

                int longIndex = cursor.getColumnIndex("longitude");
                if(longIndex > -1) {
                    location.setLongitude(cursor.getDouble(longIndex));
                }

                int altIndex = cursor.getColumnIndex("altitude");
                if(altIndex > -1) {
                    location.setAltitude(cursor.getDouble(altIndex));
                }

                int accIndex = cursor.getColumnIndex("accuracy");
                if(accIndex > -1) {
                    location.setAccuracy(cursor.getFloat(accIndex));
                }

                location.setTime(timestamp.getTime());
            }
            catch(Exception e) {
                location = null;
            }

            // Get sensor data. This involves getting a semicolon-separated String from the DB,
            // converting it into a List of Longs, and constructing individual SensorData objects
            // from the sensorData table using these IDs.
            String flatSensorData = cursor.getString(cursor.getColumnIndex("sensorData"));
            ArrayList<Long> sensorDataIds = inflateLong(flatSensorData);
            ArrayList<SensorData> data = getSensorData(sensorDataIds);
            String flatConditions = cursor.getString(cursor.getColumnIndex("conditions"));
            ArrayList<String> conditions = inflateString(flatConditions);
            long emaId = cursor.getInt(cursor.getColumnIndex("ema"));
            EcologicalMomentaryAssessment ema = getEMA(emaId);

            snap = new Snapshot(userId, timestamp, location, data, conditions, ema);
        }
        cursor.close();
        return  snap;
    }

    /**
     * Retrieves an EcologicalMomentaryAssessment from the database. null is returned if there is no
     * EcologicalMomentaryAssessment with the specified ID.
     * @param id The ID of the EcologicalMomentaryAssessment to be retrieved
     * @return The specified EcologicalMomentaryAssessment, or null
     */
    private EcologicalMomentaryAssessment getEMA(long id) {
        EcologicalMomentaryAssessment ema = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + EMATable.TABLE_NAME + " WHERE id = " +
                id, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            boolean indoors =
                    Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("indoors")));
            String reportedLocation = cursor.getString(cursor.getColumnIndex("reportedLocation"));
            String activity = cursor.getString(cursor.getColumnIndex("activity"));
            String flatCompanions = cursor.getString(cursor.getColumnIndex("companions"));
            ArrayList<String> companions = inflateString(flatCompanions);
            int airQuality = cursor.getInt(cursor.getColumnIndex("airQuality"));
            int belief = cursor.getInt(cursor.getColumnIndex("belief"));
            int intention = cursor.getInt(cursor.getColumnIndex("intention"));
            boolean behavior =
                    Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("behavior")));
            String barrier = cursor.getString(cursor.getColumnIndex("barrier"));

            ema = new EcologicalMomentaryAssessment(indoors, reportedLocation, activity, companions,
                    airQuality, belief, intention, behavior, barrier);
        }
        cursor.close();
        return ema;
    }

    /**
     * Returns a List of SensorData based on the IDs passed.
     * @param ids The IDs of the desired SensorData records in the database
     * @return List of SensorData
     */
    private ArrayList<SensorData> getSensorData(ArrayList<Long> ids) {
        ArrayList<SensorData> data = new ArrayList<>(ids.size());
        for(Long id: ids) {
            data.add(getSensorData(id));
        }
        return data;
    }

    /**
     * Returns a SensorData built from the record with the specified ID. If the id does not exist,
     * null is returned.
     * @param id The ID of the desired record
     * @return SensorData built from the specified record
     */
    private SensorData getSensorData(long id) {
        SensorData data = null;
        String[] selectionArgs = { "id = " + id };
        Cursor cursor = database.rawQuery("SELECT * FROM " + SensorDataTable.TABLE_NAME +
                " WHERE id = " + id, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            String displayName = cursor.getString(cursor.getColumnIndex("displayName"));
            String shortName = cursor.getString(cursor.getColumnIndex("shortName"));
            double value = cursor.getDouble(cursor.getColumnIndex("value"));
            String displayValue = cursor.getString(cursor.getColumnIndex("displayValue"));

            data = new SensorData(displayName, shortName, value, displayValue);
        }
        cursor.close();
        return data;
    }

    /**
     * Expands a semicolon-separated String into a List of Strings
     * @param str The semicolon-separated input String
     * @return The input, as a List
     */
    private ArrayList<String> inflateString(String str) {
        String[] strings = str.split(ARRAY_SEPARATOR);
        ArrayList<String> list = new ArrayList<>(strings.length);
        for(String string: strings) {
            list.add(string);
        }
        return list;
    }

    /**
     * Expands a semicolon-separated String into a List of Longs
     * @param str The semicolon-separated input String
     * @return The input, as a List
     */
    private ArrayList<Long> inflateLong(String str) {
        String[] strings = str.split(ARRAY_SEPARATOR);
        ArrayList<Long> list = new ArrayList<>(strings.length);
        for(String string: strings) {
            list.add(Long.valueOf(string));
        }
        return list;
    }

    /**
     * Stores current sensor data to the CurrentDataTable.
     * @param data ArrayList of SensorData
     */
    public void updateCurrentData(ArrayList<SensorData> data) {
        for(SensorData  sd: data) {
            ContentValues cv = new ContentValues(CurrentDataTable.COLUMNS.length);
            cv.put("displayName", sd.getDisplayName());
            cv.put("shortName", sd.getShortName());
            cv.put("value", sd.getValue());
            cv.put("displayValue", sd.getDisplayValue());
            database.insertWithOnConflict(CurrentDataTable.TABLE_NAME, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * Returns the latest sensor data readings. Used to populate new Snapshots.
     * @return Current sensor data
     */
    public ArrayList<SensorData> getCurrentData() {
        Cursor cursor = database.rawQuery("SELECT * FROM " + CurrentDataTable.TABLE_NAME, null);
        int size = cursor.getCount();
        ArrayList<SensorData> data = new ArrayList<>(size);
        if(size > 0) {
            cursor.moveToFirst();
            for(int i = 0; i < size; i++) {
                String displayName = cursor.getString(cursor.getColumnIndex("displayName"));
                String shortName = cursor.getString(cursor.getColumnIndex("shortName"));
                double value = cursor.getDouble(cursor.getColumnIndex("value"));
                String displayValue = cursor.getString(cursor.getColumnIndex("displayValue"));
                data.add(new SensorData(displayName, shortName, value, displayValue));
            }
        }
        cursor.close();
        return data;
    }
}