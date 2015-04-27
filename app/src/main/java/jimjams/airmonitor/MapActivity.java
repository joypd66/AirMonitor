package jimjams.airmonitor;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import jimjams.airmonitor.database.DBAccess;
import jimjams.airmonitor.datastructure.EcologicalMomentaryAssessment;
import jimjams.airmonitor.datastructure.Snapshot;
import jimjams.airmonitor.sensordata.SensorData;

/*

    Quick link to the GoogleMap API reference:
    http://developer.android.com/reference/com/google/android/gms/maps/GoogleMap.html

    Quick link to the Location API reference:
    http://developer.android.com/reference/android/location/Location.html

 */


public class MapActivity extends ActionBarActivity
        implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    /*
        Local variables
     */
    private MapFragment mMapFragment;                   // The MapFragment object for the window's map
    private GoogleMap mainMap;                          // The GoogleMap object itself
    private DBAccess dbAccess = DBAccess.getDBAccess(); // DB Access
    private Hashtable<Marker, Snapshot> markerTable;            // The map used for marker related stuff


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Acuqire the MapFragment from resources
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        // Who gets updated when the GoogleMap object is ready?
        // this object does.
        mapFragment.getMapAsync(this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        // The view has been reloaded. Get the map ready again.
        Log.v(getLocalClassName(), "onRestart() mapActivity");
        prepareMap();
    }

    /*
        Custom methods
     */

    // Prepares the map for viewing upon creating the view
    private void prepareMap() {
        System.out.println("prepareMap()");
        long profileID = dbAccess.getProfileId();

        // A list of the snapshots, for getting the locations
        List<Snapshot> snapshots = dbAccess.getSnapshots(profileID);

        // Now we just need to get ourselves a list of the locations
        ArrayList<Location> locations = new ArrayList<>();

        if (this.mainMap == null) {
            // Something went seriously wrong. This should never be called
            // But I'm paranoid so why not add it...
            System.out.println("The mainMap was null.. we should just quit and get it together...");
            System.exit(34);
        }

        // We add the pins/markers to the map

        // Marker test code
        // No longer needed, kept for legacy
        /*
        MarkerOptions testoptions = new MarkerOptions();
        testoptions.title("Test");
        testoptions.flat(false);
        testoptions.draggable(false);
        testoptions.position(new LatLng(0.0, 0.0));
        mainMap.addMarker(testoptions);
        */

        for (Snapshot wSnapshot : snapshots) {
            // Get the location from the snapshot
            Location wLocation = wSnapshot.getLocation();

            // Create the MarkerOptions object
            MarkerOptions wOptions = new MarkerOptions();

            // Set the position of the marker
            wOptions.position(new LatLng(wLocation.getLatitude(), wLocation.getLongitude()));
            Date snapshotDate = wSnapshot.getTimestamp();

            // This is just to test the title
            wOptions.title(snapshotDate.toString());

            // Finally, add the marker to the mainMap
            Marker wMarker = mainMap.addMarker(wOptions);

            // But now we need to make sure we know which snapshot is associated with
            // each marker. How can we do this? A Hashtable! Use the marker as a key,
            // and the snapshot will be the associated object.
            markerTable.put(wMarker, wSnapshot);

        }

        // zoom in to the most populated area of markers

        // Add all of the markers to a set, and add the positions of the
        // markers to a LatLngBounds builder.
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Set<Marker> markerSet = markerTable.keySet();
        for (Marker wMarker : markerSet) {
            builder.include(wMarker.getPosition());
        }

        // Build the actual LatLngBounds
        // If no points exist, an IllegalStateException is thrown.
        // It is caught here.
        try {
            LatLngBounds bounds = builder.build();

            // Build a CameraUpdate for moving the map to position
            int padding = 500;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            mainMap.moveCamera(cameraUpdate);
        } catch (IllegalStateException e) {
            // No points exist, add no points.
            Log.d(this.getLocalClassName(), "No points found, catching the exception", e);
        }




    }

    /*
        The method that is called when the map is ready to be displayed
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v(null, "onMapReady(GoogleMap)");

        // Set the mainMap object the ready googleMap
        this.mainMap = googleMap;

        // Set the mainMap's InfoWindowAdapter to this
        this.mainMap.setInfoWindowAdapter(this);

        // Create the Hashtable for the markers
        this.markerTable = new Hashtable<Marker, Snapshot>();


        // Prepare the map using the internal prepareMap() function
        prepareMap();


    }

    /*
        InfoWindowAdapter implemented methods
     */

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.v(null, "getInfoContents()");

        // Get the Snapshot for this Marker from the Hashtable
        Snapshot workingSnapshot = markerTable.get(marker);

        // Create the view from the resource layout
        View infoView = getLayoutInflater().inflate(R.layout.custom_info_window, null);

        // Create object instances for the TextViews in the infoView
        TextView dateLabel = (TextView) infoView.findViewById(R.id.dateLabel);
        TextView temperatureLabel = (TextView) infoView.findViewById(R.id.temperatureLabel);
        TextView humidityLabel = (TextView) infoView.findViewById(R.id.humidityLabel);
        TextView locationLabel = (TextView) infoView.findViewById(R.id.locationLabel);

        // Make the top bold label text the date it was captured.
        // Get the Date from the Snapshot and set it as the dateLabel text.
        Date dDate = workingSnapshot.getTimestamp();
        dateLabel.setText(dDate.toString());

        // There's a label for the location.
        EcologicalMomentaryAssessment wEMA = workingSnapshot.getEma();

        if (wEMA.getReportedLocation().length() > 18) {
            StringBuffer strBuff = new StringBuffer();
            for (int i = 0; i < 15; i++) {
                strBuff.append(wEMA.getReportedLocation().charAt(i));
            }
            strBuff.append("...");
            locationLabel.setText(strBuff.toString());
        } else {
            locationLabel.setText(wEMA.getReportedLocation());
        }




        // Start getting data from the Snapshot.
        // First, get the data ArrayList from the Snapshot.
        ArrayList<SensorData> dataList = workingSnapshot.getData();

        // Next, iterate through the list of data, looking for the "temp" and "humid" elements
        for (SensorData wData : dataList) {
            if (wData.getShortName().equals("temp")) {
                // We found the temperature! Get the data and put it in the view.
                temperatureLabel.setText(wData.getDisplayValue());
                continue;
            }

            if (wData.getShortName().equals("humid")) {
                // We found the humidity! Get the data and put it in the view.
                humidityLabel.setText(wData.getDisplayValue());
                continue;
            }
        }


        return infoView;
    }
} // End class bracket
