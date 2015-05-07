package jimjams.airmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jimjams.airmonitor.database.DBAccess;
import jimjams.airmonitor.sensordata.SensorData;
import jimjams.airmonitor.sensordata.SensorDataGenerator;
import jimjams.airmonitor.sensordata.SoundMeter;


public class MainActivity extends ActionBarActivity {

    /**
     * Delay between sensor data updates, in milliseconds
     */
    public final static long REFRESH_DELAY = 1500;

    /**
     * Used to identify source class for log
     */
    private String className = getClass().getSimpleName();

    /**
     * Font unit. This is a scaled pixel type; it will scale with the user's font preferences
     */
    final private static int FONT_UNIT = android.util.TypedValue.COMPLEX_UNIT_SP;

    /**
     * Font size
     */
    final private static float FONT_SIZE = 22;

    /**
     * INSTANCES of required bluetooth classes JPM
     */
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    /**
     * INSTANCES of bluetooth helper classes JPM
     */
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    /**
     * SOUND METER JPM
     */
    SoundMeter sound = new SoundMeter();
    // Variable for sound calibration
    double calibration;

    /**
     * INSTANTIATE TextView used for testing JPM
     */
    TextView feedbackText;

    /**
     * Database access
     */
    private DBAccess access = DBAccess.getDBAccess();

    /**
     * Reference to the SensorGenerator used to get sensor data
     */
    SensorDataGenerator generator;

    /**
     * private Handler customHandler = new Handler();
     * @param savedInstanceState
     */

    private Handler customHandler = new Handler();

    /**
     * for timer
     * @param savedInstanceState
     */
    Timer timer;

    TimerTask timerTask;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * ASSIGN feedbackText TextView to object JPM
         */
        feedbackText = (TextView)findViewById(R.id.feedbackText);

        // SSM Added
        generator = SensorDataGenerator.getInstance();

    }

    // ONSTART to start bluetooth every time main activity is returned too JPM
    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected  void onStop(){

        sound.stop();
        try {
            closeBT();
        }catch(IOException|NullPointerException e){
            //
            //feedbackText.setText("Cannot close bt in onStop(): " + e);
        }

        super.onStop();
    }

    @Override
    protected void onResume(){
        sound.start();

        if(access.getBluetoothDeviceName() == null){
            startTimer();
            feedbackText.setText("No Bluetooth device in memory.");

        }else {
            // TRY to start bt
            try {
                findBT();
                openBT();
            }
            // CATCH and print IOException JPM
            catch (IOException | NullPointerException ex) {
                // Removed feedback text on error

                //feedbackText.setText("Error in onResume: " + ex);
                access.clearCurrentData();
                startTimer();
            }

        }

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void refreshAQInset() {
        // Get updated data
        // ADDED sensor data to getData method JPM
        ArrayList<SensorData> data = generator.getData();

        // Create and populate a table
        TableLayout aqi = (TableLayout)findViewById(R.id.mainScreen_airQualityInset);
        aqi.removeAllViews();

        // Need to use LinearLayout instead of TableRow to get spanning to work
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tv = new TextView(this);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextSize(FONT_UNIT, FONT_SIZE);

        if(data == null || data.size() == 0) {
            tv.setText(getResources().getString(R.string.mainScreen_airQualityInset_no_data));
            header.addView(tv);
            aqi.addView(header);
        }
        else {
            tv.setText(getResources().getString(R.string.mainScreen_airQualityInset_data_header));
            header.addView(tv);
            aqi.addView(header);
            for(SensorData sd: data) {
                // Log.d(className, "Adding data to AQInset.");
                TableRow tr = new TableRow(this);
                TextView label = new TextView(this), value = new TextView(this);
                label.setText(sd.getDisplayName());
                label.setPadding(1, 1, 15, 1);
                label.setTextSize(FONT_UNIT, FONT_SIZE);
                value.setText(sd.getDisplayValue());
                value.setPadding(15, 1, 1, 1);
                value.setGravity(Gravity.END);
                value.setTextSize(FONT_UNIT, FONT_SIZE);
                tr.addView(label);
                tr.addView(value);
                aqi.addView(tr);
            }
        }
    }


    /**
     * Invoked when the EMA button on the main screen is clicked.
     * @param emaBtn The EMA button on the main screen
     */
    public void on_MainScreen_EMA_button_Click(View emaBtn) {

        Intent intent = new Intent(this, EMAActivity.class);
        startActivity(intent);
    }

    /**
     * Invoked when the history button on the main screen is clicked.
     * @param histBtn The history button on the main screen
     */
    public void on_MainScreen_hist_button_Click(View histBtn) {

      Intent intent = new Intent(this, HistoryActivity.class);
      startActivity(intent);
    }

    public void on_MainScreen_map_button_Click(View mapBtn) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * Invoked when the sound calibration button is pressed
     */
    public void on_MainScreen_calibration_button_Click(View caliBtn){
        //feedbackText.setText(Integer.toString(sound.getAmplitude()));
        calibration = 60/Math.log(sound.getAmplitude());
        // SAVE calibration to database
        access.setBluetoothCalibration(calibration);

        // testing - feedbackText.setText(Double.toString(calibration));
    }

    /**
     * Invoked when the history button on the main screen is clicked.
     * @param blueBtn The history button on the main screen
     */
    public void on_MainScreen_Bluetooth_button_Click(View blueBtn) {

        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }

    /**
     * FINDBT method
     * GETS bluetooth adaptor
     * ATTEMPTS to enable bluetooth if it isn't enabled
     * FINDS correct bluetooth device
     * ASSIGNS correct bluetooth device for use
     * JPM
     */

    void findBT()
    {
        // INSTANTIATE bluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // IF no bluetooth adaptor
        if(mBluetoothAdapter == null)
        {
            // DO something ?
            //myLabel.setText("No bluetooth adapter available");
        }

        // IF bluetooth adaptor found && not enabled
        if(!mBluetoothAdapter.isEnabled())
        {
            // ATTEMPT to enable bluetooth
            // Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableBluetooth, 0);
        }

        // GET bound devices from bluetoothAdaptor
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // IF one or more devices bound
        if(pairedDevices.size() > 0)
        {
            // FOR EACH adapter bound
            for(BluetoothDevice device : pairedDevices)
            {
                // IF device equals name CHOSEN IN BLUETOOTH SCREEN

                if(device.getName().equals(access.getBluetoothDeviceName()))
                {
                    // SET this device as our AirQuality device
                    mmDevice = device;
                    // BREAK out of loop
                    break;
                }
            }
        }
        //myLabel.setText("Bluetooth Device Found");
    }

    /**
     * OPENBT
     * Bluetooth handshake to connect for transmission JPM
     * @throws IOException
     */
    void openBT() throws IOException
    {
        // GET unique UUID for standard SerialPortService ID
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        // CREATE socket to AirQuality device
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        // CONNECT
        mmSocket.connect();
        // INSTANTIATE outputStream ? not used
        mmOutputStream = mmSocket.getOutputStream();
        // INSTANTIATE inputStream
        mmInputStream = mmSocket.getInputStream();

        // START beginListeningForData thread
        beginListenForData();

        //myLabel.setText("Bluetooth Opened");
    }

    /**
     * BEGINLISTENINGFORDATA
     * Thread to get updates from AirCasting Device
     * UPDATES values on screen using GenerateSensorData
     */
    void beginListenForData()
    {
        // INSTANTIATE handler
        // handler has the scope to communicate between this class and Main Activity
        final Handler handler = new Handler();
        //This is the ASCII code for a newline character, which is used in Arduino code
        final byte delimiter = 10;

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        // DELIMITTOR for string balues - unused in this class, but used in GenerateSensor Data JPM
        //final String minorDelims = ";";

        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                // WHILE bluetooth thread running

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    // TRY to get data
                    try {
                        // GET the amount of bytes available on input stream buffer
                        int bytesAvailable = mmInputStream.available();

                        // IF there are bytes available
                        if (bytesAvailable > 0) {
                            // CREATE array of bytes in packet
                            byte[] packetBytes = new byte[bytesAvailable];
                            // GET bytes
                            mmInputStream.read(packetBytes);
                            // FOR every byte
                            for (int i = 0; i < bytesAvailable; i++) {
                                // SET b = this byte from array
                                byte b = packetBytes[i];
                                // IF b is a delimiter
                                if (b == delimiter) {
                                    // SET encodedBytes EQUAL to number of bytes already read
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    // MAKE new array less what's already been read
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            access.updateCurrentData(generator.getData(data));
                                            sound.getAmplitude();
                                            refreshAQInset();
                                            }
                                    });

                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }

            }
        });

        workerThread.start();
    }

    public void startTimer() {

        //set a new Timer

        timer = new Timer();

        //initialize the TimerTask's job

        initializeTimerTask();



        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms

        timer.schedule(timerTask, 1000, 1000); //

    }



    public void stoptimertask(View v) {

        //stop the timer, if it's not already null

        if (timer != null) {

            timer.cancel();

            timer = null;

        }

    }


    public void initializeTimerTask() {

        timerTask = new TimerTask() {

            public void run() {

                //use a handler to run a toast that shows the current timestamp

                handler.post(new Runnable() {

                    public void run() {
                        // SET sound
                        sound.getAmplitude();
                        // REFRESH AQInsert
                        refreshAQInset();
                    }

                });

            }

        };

    }



    void closeBT() throws IOException
    {
        // SET stopworker to stop the beginListeningForData thread
        stopWorker = true;
        // CLOSE the output stream
        mmOutputStream.close();
        // CLOSE the input stream
        mmInputStream.close();
        // CLOSE the socket...
        mmSocket.close();
        //myLabel.setText("Bluetooth Closed");
    }



}