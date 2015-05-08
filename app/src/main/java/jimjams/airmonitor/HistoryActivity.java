package jimjams.airmonitor;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import jimjams.airmonitor.database.DBAccess;
import jimjams.airmonitor.datastructure.EcologicalMomentaryAssessment;
import jimjams.airmonitor.datastructure.Profile;
import jimjams.airmonitor.datastructure.Snapshot;
import jimjams.airmonitor.sensordata.SensorData;

public class HistoryActivity extends ActionBarActivity {

    /**
     * Font unit. This is a scaled pixel type; it will scale with the user's font preferences
     */
    final private static int FONT_UNIT = android.util.TypedValue.COMPLEX_UNIT_SP;

    /**
     * Font size for clickable labels
     */
    final private static float LABEL_FONT_SIZE = 20;

    /**
     * Font size for data tables
     */
    final private static float TABLE_FONT_SIZE = 14;

    /**
     * Value used to indent a subcategory under its parent
     */
    private static final int INDENT = 30;

    /**
     * Used to format latitude and longitude
     */
    private DecimalFormat decFormat = new DecimalFormat("0.00");

    /**
     * Used to format date for expando labels
     */
    private DateFormat dateFormat;

    /**
     * Used to format date for expando labels
     */
    private DateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Instantiate date, time formatters
        dateFormat = android.text.format.DateFormat.getMediumDateFormat(this);
        timeFormat = android.text.format.DateFormat.getTimeFormat(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Override the default onStart() to populate the table based on Profile.
     */
    @Override
    public void onStart() {
        super.onStart();

        // Populate the contents of the history
        populateHistory();
    }

    /**
     * <p>Populates the screen with an expandable/collapsible data table for each of the user's
     *    Snapshots.</p>
     * <p>A note on the use of tags in this method: TextViews describing Snapshots and their
     *    subcategories act as buttons which can expand and collapse their subcategories. Tags
     *    allow the OnClickListener to manage this. The tag on the TextView is a Boolean indicating
     *    whether the node is currently expanded. The tag on the containing layout is a List of
     *    expandable/collapsible child elements. When the TextView is clicked, the Boolean tag is
     *    checked, and the child elements in the parent's tag are either hidden or shown, and the
     *    Boolean tag is toggled.</p>
     */
    private void populateHistory() {
        // The user's Profile
        Profile profile = Profile.getProfile();
        long userId = profile.getId();

        // The LinearLayout which will hold everything
        LinearLayout layout = (LinearLayout)findViewById(R.id.history_layout);

        // Add a row for each Snapshot
        ArrayList<Snapshot> snapshots = DBAccess.getDBAccess().getSnapshots(userId);

        // LinearLayout containing a row for each Snapshot
        LinearLayout mainSnapshotLayout = new LinearLayout(this);
        mainSnapshotLayout.setOrientation(LinearLayout.VERTICAL);
        mainSnapshotLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Set up listener for table expansion
        // Listener that expands or collapses categories in the history screen
        View.OnClickListener listener = new View.OnClickListener() {
            /**
             * Expands or collapses categories in the history screen.
             * @param v The View that was clicked
             */
            public void onClick(View v) {
                Boolean tag = (Boolean)v.getTag();
                LinearLayout parent = (LinearLayout)v.getParent();
                ArrayList<View> kids = (ArrayList<View>)parent.getTag();
                if(tag) {
                    // Snapshot is expanded; must be collapsed
                    for(View kid: kids) {
                        parent.removeView(kid);
                    }
                }
                else {
                    // Snapshot is collapsed; must be expanded
                    for(View kid: kids) {
                        parent.addView(kid);
                    }
                }
                // Toggle the boolean tag
                v.setTag(!tag);
            }
        };

        // Each Snapshot gets another LinearLayout
        for(Snapshot snapshot: snapshots) {
            // Add to parent
            mainSnapshotLayout.addView(getIndividualSnapshotLayout(snapshot, listener));
        }
        layout.addView(mainSnapshotLayout);
    }

    /**
     * Creates a LinearLayout for a single Snapshot's data.
     * @param snapshot The Snapshot
     * @param listener The click listener used to expand/collapse data
     * @return LinearLayout for a single Snapshot's data
     */
    private LinearLayout getIndividualSnapshotLayout(Snapshot snapshot,
            View.OnClickListener listener) {
        LinearLayout snapshotLayout = new LinearLayout(this);
        snapshotLayout.setOrientation(LinearLayout.VERTICAL);
        snapshotLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Tag is a list of expandable/collapsible child component
        ArrayList<View> snapshotTag = new ArrayList<>();
        snapshotLayout.setTag(snapshotTag);

        // Add date/time as label. This TextView will also serve as an expand button
        Date timestamp = snapshot.getTimestamp();
        TextView snapshotLabel = new TextView(this);
        snapshotLabel.setText(formatDate(timestamp));
        snapshotLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
        snapshotLayout.addView(snapshotLabel);

        // Tag is true if following siblings are displayed
        snapshotLabel.setTag(false);

        // Create a LinearLayout for each category under Snapshot
        // Layout for location
        snapshotTag.add(getLocationLayout(listener, snapshot.getLocation()));

        // Layout for sensor data
        snapshotTag.add(getSensorLayout(listener, snapshot.getData()));

        // Layout for EMA
        snapshotTag.add(getEmaLayout(listener, snapshot.getEma()));

        // Layout for existing conditions
        snapshotTag.add(getConditionLayout(listener, snapshot.getConditions()));

        snapshotLabel.setOnClickListener(listener);
        return snapshotLayout;
    }

    /**
     * Creates a LinearLayout for a snapshot's location data.
     * @param listener The click listener used to expand/collapse data
     * @param location The location
     * @return LinearLayout for a snapshot's location data
     */
    private LinearLayout getLocationLayout(View.OnClickListener listener, Location location) {
        LinearLayout locationLayout = new LinearLayout(this);
        locationLayout.setOrientation(LinearLayout.VERTICAL);

        // Add padding to indent
        locationLayout.setPadding(locationLayout.getPaddingLeft() + INDENT,
                locationLayout.getPaddingTop(), locationLayout.getPaddingRight(),
                locationLayout.getPaddingBottom());

        // Tag is a list of expandable/collapsible child components
        locationLayout.setTag(new ArrayList<View>());

        // Label for the location data
        TextView locationLabel = new TextView(this);
        locationLabel.setText(R.string.history_screen_location_label);
        locationLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
        locationLayout.addView(locationLabel);

        // Tag is true if following siblings are displayed
        locationLabel.setTag(false);

        // Set listener
        locationLabel.setOnClickListener(listener);

        // TableLayout to display location data
        TableLayout locationTable = new TableLayout(this);
        locationTable.setPadding(locationTable.getPaddingLeft() + INDENT,
                locationTable.getPaddingTop(), locationTable.getPaddingRight(),
                locationTable.getPaddingBottom());

        // Add this layout to the parent's tag
        ((ArrayList<View>)locationLayout.getTag()).add(locationTable);

        if(location == null) {
            TableRow tr = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText(R.string.history_screen_no_data);
            tv.setTextSize(FONT_UNIT, TABLE_FONT_SIZE);
            tr.addView(tv);
            locationTable.addView(tr);
        }
        else {
            // Populate the table
            locationTable.addView(makeTableRow("latitude",
                    decFormat.format(location.getLatitude())));
            locationTable.addView(makeTableRow("longitude",
                    decFormat.format(location.getLongitude())));
        } // if location == null
        return locationLayout;
    }

    /**
     * Creates a LinearLayout for a snapshot's sensor data.
     * @param listener The click listener used to expand/collapse data
     * @param sensorData Sensor data
     * @return LinearLayout for a snapshot's sensor data
     */
    private LinearLayout getSensorLayout(View.OnClickListener listener,
            ArrayList<SensorData> sensorData) {
        LinearLayout sensorLayout = new LinearLayout(this);
        sensorLayout.setOrientation(LinearLayout.VERTICAL);

        // Add padding to indent
        sensorLayout.setPadding(sensorLayout.getPaddingLeft() + INDENT,
                sensorLayout.getPaddingTop(), sensorLayout.getPaddingRight(),
                sensorLayout.getPaddingBottom());

        // Tag is a list of expandable/collapsible child components
        sensorLayout.setTag(new ArrayList<View>());

        // Label for the sensor data
        TextView sensorLabel = new TextView(this);
        sensorLabel.setText(R.string.history_screen_sensor_label);
        sensorLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
        sensorLayout.addView(sensorLabel);

        // Tag is true if following siblings are displayed
        sensorLabel.setTag(false);

        // Set listener
        sensorLabel.setOnClickListener(listener);

        // TableLayout to display sensor data
        TableLayout sensorTable = new TableLayout(this);
        sensorTable.setPadding(sensorTable.getPaddingLeft() + INDENT,
                sensorTable.getPaddingTop(), sensorTable.getPaddingRight(),
                sensorTable.getPaddingBottom());

        // Add this layout to the parent's tag
        ((ArrayList<View>)sensorLayout.getTag()).add(sensorTable);

        // Populate the table
        if(sensorData.size() == 0) {
            TableRow tr = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText(R.string.history_screen_no_data);
            tv.setTextSize(FONT_UNIT, TABLE_FONT_SIZE);
            tr.addView(tv);
            sensorTable.addView(tr);
        }
        else {
            for(SensorData sd: sensorData) {
                sensorTable.addView(makeTableRow(sd.getDisplayName(), sd.getDisplayValue()));
            }
        }
        return sensorLayout;
    }

    /**
     * Creates a LinearLayout for a snapshot's EMA data.
     * @param listener The click listener used to expand/collapse data
     * @param ema Ecological Momentary Assessment
     * @return LinearLayout for a snapshot's EMA data
     */
    private LinearLayout getEmaLayout(View.OnClickListener listener,
            EcologicalMomentaryAssessment ema) {
        LinearLayout emaLayout = new LinearLayout(this);
        emaLayout.setOrientation(LinearLayout.VERTICAL);

        // Add padding to indent
        emaLayout.setPadding(emaLayout.getPaddingLeft() + INDENT, emaLayout.getPaddingTop(),
                emaLayout.getPaddingRight(), emaLayout.getPaddingBottom());

        // Tag is a list of expandable/collapsible child components
        emaLayout.setTag(new ArrayList<View>());

        // Label for the EMA
        TextView emaLabel = new TextView(this);
        emaLabel.setText(R.string.history_screen_ema_label);
        emaLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
        emaLayout.addView(emaLabel);

        // Tag is a Boolean which is true when the layout is expanded
        emaLabel.setTag(false);

        // Set listener
        emaLabel.setOnClickListener(listener);

        // TableLayout to display ema data
        TableLayout emaTable = new TableLayout(this);
        emaTable.setPadding(emaTable.getPaddingLeft() + INDENT, emaTable.getPaddingTop(),
                emaTable.getPaddingRight(), emaTable.getPaddingBottom());

        // Add this layout to the parent's tag
        ((ArrayList<View>)emaLayout.getTag()).add(emaTable);

        // Populate the table
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_indoors_label), String.valueOf(ema.getIndoors())));
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_reportedLocation_label), ema.getReportedLocation()));
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_activity_label), ema.getActivity()));
        ArrayList<String> companions = ema.getCompanions();
        String companionString = "";
        for(int i = 0; i < companions.size(); i++) {
            if(i > 0) {
                companionString += "\n";
            }
            companionString += companions.get(i);
        }
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_companion_label), companionString));
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_airQuality_label),
                String.valueOf(ema.getAirQuality())));
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_belief_label), String.valueOf(ema.getBelief())));
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_intention_label), String.valueOf(ema.getIntention())));
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_behavior_label), String.valueOf(ema.getBehavior())));
        emaTable.addView(makeTableRow(getResources().getString(
                R.string.history_screen_ema_barrier_label), String.valueOf(ema.getBarrier())));
        return emaLayout;
    }

    /**
     * Creates a LinearLayout for a snapshot's existing conditions.
     * @param listener The click listener used to expand/collapse data
     * @param conditions Snapshot's existing conditions
     * @return LinearLayout for a snapshot's existing conditions
     */
    private LinearLayout getConditionLayout(View.OnClickListener listener,
            ArrayList<String> conditions) {
        LinearLayout conditionLayout = new LinearLayout(this);
        conditionLayout.setOrientation(LinearLayout.VERTICAL);

        // Add padding to indent
        conditionLayout.setPadding(conditionLayout.getPaddingLeft() + INDENT,
                conditionLayout.getPaddingTop(), conditionLayout.getPaddingRight(),
                conditionLayout.getPaddingBottom());

        // Tag is a list of expandable/collapsible child components
        conditionLayout.setTag(new ArrayList<View>());

        // Label for the existing conditions
        TextView conditionLabel = new TextView(this);
        conditionLabel.setText(R.string.history_screen_condition_label);
        conditionLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
        conditionLayout.addView(conditionLabel);

        // Tag is a Boolean which is true when the layout is expanded
        conditionLabel.setTag(false);

        // Set listener
        conditionLabel.setOnClickListener(listener);

        // LinearLayout to display existing conditions data. For consistency with other categories,
        // I'm calling it a table
        LinearLayout conditionTable = new TableLayout(this);
        conditionTable.setPadding(conditionTable.getPaddingLeft() + INDENT,
                conditionTable.getPaddingTop(), conditionTable.getPaddingRight(),
                conditionTable.getPaddingBottom());

        // Add this layout to the parent's tag
        ((ArrayList<View>)conditionLayout.getTag()).add(conditionTable);

        // Populate the layout
        if(conditions.size() == 0) {
            // If no conditions, display message
            TextView condView = new TextView(this);
            condView.setText(R.string.history_screen_no_conds);
            condView.setTextSize(FONT_UNIT, TABLE_FONT_SIZE);
            conditionTable.addView(condView);
        }
        else {
            for(String cond: conditions) {
                TextView condView = new TextView(this);
                condView.setText(cond);
                condView.setTextSize(FONT_UNIT, TABLE_FONT_SIZE);
                conditionTable.addView(condView);
            }
        }
        return conditionLayout;
    }

    /**
     * Generates a 2-column table row. Right column is right-aligned. Used to display profile data.
     * @param col1 Contents of left column
     * @param col2 Contents of right column
     * @return TableRow containing two columns
     */
    private TableRow makeTableRow(String col1, String col2) {
        TableRow tr = new TableRow(this);
        TextView c1 = new TextView(this), c2 = new TextView(this);
        c1.setText(col1);
        c1.setTextSize(FONT_UNIT, TABLE_FONT_SIZE);
        c1.setPadding(2, 2, 15, 2);
        c2.setText(col2);
        c2.setTextSize(FONT_UNIT, TABLE_FONT_SIZE);
        c2.setPadding(15, 2, 2, 2);
        c2.setGravity(Gravity.END);
        tr.addView(c1);
        tr.addView(c2);
        return tr;
    }

    /**
     * Formats a date to be used as an expando label.
     * @param date The date to be formatted
     * @return Formatted date
     */
    private String formatDate(Date date) {
        return dateFormat.format(date) + ", " + timeFormat.format(date);
    }
}