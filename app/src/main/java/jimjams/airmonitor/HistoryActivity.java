package jimjams.airmonitor;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jimjams.airmonitor.database.AMDBContract;
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
    * Used to identify source class for log
    */
   private String className = getClass().getSimpleName();

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_history);
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

      //noinspection SimplifiableIfStatement
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
    * <p>A note on the use of tags in this method: TextViews describing Snapshots and their
    *    subcategories act as buttons which can expand and collapse their subcategories. Tags allow
    *    OnClickListeners to manage this. The tag on the TextView is a Boolean indicating whether
    *    the node is currently expanded. The tag on the containing layout is a List of
    *    expandable/collapsible child elements. When the TextView is clicked, the Boolean tag is
    *    checked, and the child elements in the parent's tag are either hidden or shown, and the
    *    Boolean tag is toggled.</p>
    * <p>This method is needlessly long and badly needs to be broken down into smaller pieces, but
    *    it's late and I'm tired.</p>
    */
   private void populateHistory() {

      Log.d(className, DBAccess.getDBAccess().toString(AMDBContract.SnapshotTable.TABLE_NAME));

      // The user's Profile
      Profile profile = Profile.getProfile();
      long userId = profile.getId();

      // The LinearLayout which will hold everything
      LinearLayout layout = (LinearLayout)findViewById(R.id.history_layout);

      // Add user id row
      TextView userIdRow = new TextView(this);
      userIdRow.setText("User ID: " + userId);
      userIdRow.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
      layout.addView(userIdRow);

      // Add a row for each Snapshot
      List<Snapshot> snapshots = DBAccess.getDBAccess().getSnapshots(userId);
      Log.d(className, snapshots.size() + " Snapshots loaded.");
      for(Snapshot snap: snapshots) {
         Log.d(className, snap.toString());
      }

      // LinearLayout containing a row for each Snapshot
      LinearLayout mainSnapshotLayout = new LinearLayout(this);
      mainSnapshotLayout.setOrientation(LinearLayout.VERTICAL);
      mainSnapshotLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));

      // Each Snapshot gets another LinearLayout
      for(Snapshot snapshot: snapshots) {
         LinearLayout individualSnapshotLayout = new LinearLayout(this);
         individualSnapshotLayout.setOrientation(LinearLayout.VERTICAL);
         individualSnapshotLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
               ViewGroup.LayoutParams.WRAP_CONTENT));

         // Tag is a list of expandable/collapsible child component
         individualSnapshotLayout.setTag(new ArrayList<View>());

         // Add date/time as label This TextView will also serve as an expand button
         Date timestamp = snapshot.getTimestamp();
         TextView individualSnapshotLabel = new TextView(this);
         individualSnapshotLabel.setText(timestamp.toString());
         individualSnapshotLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
         individualSnapshotLayout.addView(individualSnapshotLabel);

         // Tag is true if following siblings are displayed
         individualSnapshotLabel.setTag(false);

         //*****************************************************************************************

         // Create a LinearLayout for each category under Snapshot
         // Layout for sensor data
         LinearLayout sensorLayout = new LinearLayout(this);
         sensorLayout.setOrientation(LinearLayout.VERTICAL);

         // Add padding to indent
         sensorLayout.setPadding(individualSnapshotLayout.getPaddingLeft() + INDENT,
               individualSnapshotLayout.getPaddingTop(), individualSnapshotLayout.getPaddingRight(),
               individualSnapshotLayout.getPaddingBottom());

         // Tag is a list of expandable/collapsible child component
         sensorLayout.setTag(new ArrayList<View>());

         // Add this layout to the parent's tag
         ((List<View>)individualSnapshotLayout.getTag()).add(sensorLayout);

         // Label for the sensor data
         TextView sensorLabel = new TextView(this);
         sensorLabel.setText(R.string.history_screen_sensor_label);
         sensorLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
         sensorLayout.addView(sensorLabel);

         // Tag is true if following siblings are displayed
         sensorLabel.setTag(false);

         // TableLayout to display sensor data
         TableLayout sensorTable = new TableLayout(this);
         sensorTable.setPadding(sensorTable.getPaddingLeft() + INDENT, sensorTable.getPaddingTop(),
               sensorTable.getPaddingRight(), sensorTable.getPaddingBottom());

         // Add this layout to the parent's tag
         ((List<View>)sensorLayout.getTag()).add(sensorTable);

         // Get the sensor data
         List<SensorData> sensorData = snapshot.getData();

         // Populate the table
         if(sensorData.size() == 0) {
            TableRow tr = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText(R.string.history_screen_no_data);
            tv.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
            tr.addView(tv);
            sensorTable.addView(tr);
         }
         else {
            for(SensorData sd: sensorData) {
               sensorTable.addView(makeTableRow(sd.getDisplayName(), sd.getDisplayValue()));
            }
         }

         //*****************************************************************************************

         // Layout for EMA
         LinearLayout emaLayout = new LinearLayout(this);
         emaLayout.setOrientation(LinearLayout.VERTICAL);

         // Add padding to indent
         emaLayout.setPadding(individualSnapshotLayout.getPaddingLeft() + INDENT,
               individualSnapshotLayout.getPaddingTop(), individualSnapshotLayout.getPaddingRight(),
               individualSnapshotLayout.getPaddingBottom());

         // Tag is a list of expandable/collapsible child component
         emaLayout.setTag(new ArrayList<View>());

         // Add this layout to the parent's tag
         ((List<View>)individualSnapshotLayout.getTag()).add(emaLayout);

         // Label for the EMA
         TextView emaLabel = new TextView(this);
         emaLabel.setText(R.string.history_screen_ema_label);
         emaLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
         emaLayout.addView(emaLabel);

         // Tag is a Boolean which is true when the layout is expanded
         emaLabel.setTag(false);

         // TableLayout to display ema data
         TableLayout emaTable = new TableLayout(this);
         emaTable.setPadding(emaTable.getPaddingLeft() + INDENT, emaTable.getPaddingTop(),
               emaTable.getPaddingRight(), emaTable.getPaddingBottom());

         // Add this layout to the parent's tag
         ((List<View>)emaLayout.getTag()).add(emaTable);

         // Get the ema data
         EcologicalMomentaryAssessment ema = snapshot.getEma();

         // Populate the table
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_indoors_label),
               String.valueOf(ema.getIndoors())));
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_reportedLocation_label),
               ema.getReportedLocation()));
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_activity_label),
               ema.getActivity()));
         List<String> companions = ema.getCompanions();
         String companionString = "";
         for(int i = 0; i < companions.size(); i++) {
            if(i > 0) {
               companionString += "\n";
            }
            companionString += companions.get(i);
         }
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_companion_label),
               companionString));
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_airQuality_label),
               String.valueOf(ema.getAirQuality())));
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_belief_label),
               String.valueOf(ema.getBelief())));
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_behavior_label),
               String.valueOf(ema.getBehavior())));
         emaTable.addView(makeTableRow(getResources().getString(R.string.history_screen_ema_barrier_label),
               String.valueOf(ema.getBarrier())));

         //*****************************************************************************************

         // Layout for existing conditions
         LinearLayout conditionLayout = new LinearLayout(this);
         conditionLayout.setOrientation(LinearLayout.VERTICAL);

         // Add padding to indent
         conditionLayout.setPadding(individualSnapshotLayout.getPaddingLeft() + INDENT,
               individualSnapshotLayout.getPaddingTop(), individualSnapshotLayout.getPaddingRight(),
               individualSnapshotLayout.getPaddingBottom());

         // Tag is a list of expandable/collapsible child component
         conditionLayout.setTag(new ArrayList<View>());

         // Add this layout to the parent's tag
         ((List<View>)individualSnapshotLayout.getTag()).add(conditionLayout);

         // Label for the existing conditions
         TextView conditionLabel = new TextView(this);
         conditionLabel.setText(R.string.history_screen_condition_label);
         conditionLabel.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
         conditionLayout.addView(conditionLabel);

         // Tag is a Boolean which is true when the layout is expanded
         conditionLabel.setTag(false);

         // LinearLayout to display existing conditions data. For consistency with other categories,
         // I'm calling it a table
         LinearLayout conditionTable = new TableLayout(this);
         conditionTable.setPadding(conditionTable.getPaddingLeft() + INDENT,
               conditionTable.getPaddingTop(), conditionTable.getPaddingRight(),
               conditionTable.getPaddingBottom());

         // Add this layout to the parent's tag
         ((List<View>)conditionLayout.getTag()).add(conditionTable);

         // Get the existing conditions data
         List<String> conditions = snapshot.getConditions();

          // Populate the layout
         if(conditions.size()  == 0) {
            // If no conditions, display message
            TextView condView = new TextView(this);
            condView.setText(R.string.history_screen_no_conds);
            condView.setTextSize(FONT_UNIT, LABEL_FONT_SIZE);
            conditionTable.addView(condView);
         }
         else {
            for (String cond: conditions) {
               TextView condView = new TextView(this);
               condView.setText(cond);
               condView.setTextSize(FONT_UNIT, TABLE_FONT_SIZE);
               conditionTable.addView(condView);
            }
         }

         // Set up listener for table expansion
         ExpansionListener listener = new ExpansionListener();

         individualSnapshotLabel.setOnClickListener(listener);
         sensorLabel.setOnClickListener(listener);
         emaLabel.setOnClickListener(listener);
         conditionLabel.setOnClickListener(listener);

         // Add to parent
         mainSnapshotLayout.addView(individualSnapshotLayout);
      }

      layout.addView(mainSnapshotLayout);
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
    * Listener that expands or collapses categories in the history screen
    */
   private class ExpansionListener implements View.OnClickListener {

      /**
       * Expands or collapses categories in the history screen.
       * @param v The View that was clicked
       */
      public void onClick(View v) {
         Boolean tag = (Boolean)v.getTag();
         LinearLayout parent = (LinearLayout)v.getParent();
         List<View> kids = (List<View>)parent.getTag();
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
   }
}