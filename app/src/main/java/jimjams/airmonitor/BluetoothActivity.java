package jimjams.airmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import jimjams.airmonitor.database.DBAccess;

/**
 * Created by pjoy on 3/25/15.
 */
public class BluetoothActivity extends ActionBarActivity {
    private static final int REQUEST_ENABLE_BT = 1;

    //Declare buttons
    private Button turnOnButton = null;
    private Button turnOffButton = null;
    private Button listPairedButton = null;
    private Button searchButton = null;

    //declare objects
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;

    // DATA object
    private DBAccess access = DBAccess.getDBAccess();

    private ListView listViewPaired = null;
    private ListView listViewDetected = null;

    private ArrayList<BluetoothDevice> arrayListDetectedBluetoothDevices = null;
    private ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices = null;
    private Set<BluetoothDevice> pairedDevices = null;

    private ArrayAdapter<String> pairedAdapter = null;
    private ArrayAdapter<String> detectedAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //declare a bluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //if the bluetooth adapter is null gracefully end and display a message
        if (bluetoothAdapter == null) {
            turnOnButton.setEnabled(false);
            turnOffButton.setEnabled(false);
            listPairedButton.setEnabled(false);
            searchButton.setEnabled(false);

            //Create Message
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
        }//end if statement

        else {
            //setup turnOn button
            turnOnButton = (Button) findViewById(R.id.turnOn);
            turnOnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Auto-generated method stub
                    turnOn(v);
                }//end onClick method
            });

            //setup turnOff button
            turnOffButton = (Button) findViewById(R.id.turnOff);
            turnOffButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Auto-generated method stub
                    turnOff(v);
                }//end onClick method
            });

            //setup listPairedButton
            listPairedButton = (Button) findViewById(R.id.showPaired);
            listPairedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Auto-generated method stub
                    showPaired(v);
                }//end onClick method
            });

            /*This is not needed for our application at this time
            //setup searchButton
            makeDiscButton = (Button) findViewById(R.id.makeDisc);
            makeDiscButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Auto-generated method stub
                    makeDisc(v);
                }//end onClick method
            });
            */

            //setup searchButton
            searchButton = (Button) findViewById(R.id.search);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Auto-generated method stub
                    search(v);
                }//end onClick method
            });

            //create a new clickable list to display paired devices
            listViewPaired = (ListView) findViewById(R.id.listViewPaired);
            pairedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            listViewPaired.setAdapter(pairedAdapter);
            listViewPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Get the bluetooth device that was clicked
                    bluetoothDevice = arrayListPairedBluetoothDevices.get(position);

                    // SAVE device name
                    access.setBluetoothDeviceName(bluetoothDevice.getName());

                    Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " connected.", Toast.LENGTH_SHORT).show();}//end onItemClick method
            });

            //create a new clickable list that displays detected devices
            listViewDetected = (ListView) findViewById(R.id.listViewDetected);
            detectedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            listViewDetected.setAdapter(detectedAdapter);
            listViewDetected.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                    //get the bluetooth device that was clicked
                    bluetoothDevice = arrayListDetectedBluetoothDevices.get(position);

                    //flag to check if bonding is successful
                    Boolean isBonded = false;
                    try {
                        //returns true if successful
                        isBonded = createBond(bluetoothDevice);

                        //if successful notify change
                        if(isBonded){
                            detectedAdapter.notifyDataSetChanged();
                        }//end if statement

                        //else create message
                        else{
                            Toast.makeText(getApplicationContext(), "Device already paired", Toast.LENGTH_SHORT).show();
                        }//end else statement
                    }//end try block
                    catch (Exception e) {
                        System.out.println("Pairing Failed");
                        e.printStackTrace();
                    }//end catch block
                }//end onItemClick method
            });
            arrayListDetectedBluetoothDevices = new ArrayList<BluetoothDevice>();
            arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        }//end else statement
    }//end onCreate method

    //This method will turn on the bluetooth or display a message
    private void turnOn(View view) {
        if (!bluetoothAdapter.isEnabled()) {
            //Create new intent and run it.
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            //Create Message
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_LONG).show();
        }//end if statement
        else {
            //Create Message
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_LONG).show();
        }//end else statement
    }//end turnOn method

    //This message will show a list of paired devices
    private void showPaired(View view) {
        //clear the adapter
        pairedAdapter.clear();

        if (bluetoothAdapter.isEnabled()) {
            // get paired devices
            pairedDevices = bluetoothAdapter.getBondedDevices();

            for (BluetoothDevice device : pairedDevices) {
                pairedAdapter.add(device.getName() + "\n" + device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }//end for loop

            pairedAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }//end if statement

        else {
            Toast.makeText(getApplicationContext(), "Bluetooth is off", Toast.LENGTH_SHORT).show();
        }//end else statement
    }//end showPaired method

    //this creates a broadcast receiver
    final private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //Message message = Message.obtain();
            String action = intent.getAction();

            //if a device is found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //declare and initiate a new bluetooth device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(arrayListDetectedBluetoothDevices.size() < 1) {
                    //add the name and the MAC address of the object to the adapter
                    detectedAdapter.add(device.getName() + "\n" + device.getAddress());
                    arrayListDetectedBluetoothDevices.add(device);
                    detectedAdapter.notifyDataSetChanged();
                }//end if statement

                else {
                    //flag to check whether or not device is already in the list
                    boolean flag = false;
                    for (int i = 0; i < arrayListDetectedBluetoothDevices.size(); i++) {
                        if (device.getAddress().equals(arrayListDetectedBluetoothDevices.get(i).getAddress())) {
                            flag = true;
                        }//end if statement
                    }//end for loop

                    if (flag == false) {
                        detectedAdapter.add(device.getName() + "\n" + device.getAddress());
                        arrayListDetectedBluetoothDevices.add(device);
                        detectedAdapter.notifyDataSetChanged();
                    }//end if statement
                }//end else statement
            }//end if statement
        }//end onReceive
    };

    //this method will search for new bluetooth devices and display them
    private void search(View view) {
        //if bluetooth adapter is enabled begin search or cancel search
        if (bluetoothAdapter.isEnabled()) {

            //if the device is already searching, cancel the search
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
                Toast.makeText(getApplicationContext(), "Cancelled Search", Toast.LENGTH_LONG).show();
            }//end if statement

            else {
                //clear the adapter
                detectedAdapter.clear();

                //Create Message
                Toast.makeText(getApplicationContext(), "Searching", Toast.LENGTH_LONG).show();

                //Start searching
                bluetoothAdapter.startDiscovery();
                registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }//end else statement
        }//end if statement

        //else if bluetooth adapter is disabled, display a message
        else {
            //Create Message
            Toast.makeText(getApplicationContext(), "Bluetooth is off", Toast.LENGTH_LONG).show();
        }//end else statement
    }//end search method

    //this message will disable the bluetooth adapter and display a message
    private void turnOff(View view) {
        //Disable Bluetooth Adapter
        bluetoothAdapter.disable();
        //Create Message
        Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();
    }//end turnOff method

    private boolean createBond(BluetoothDevice bluetoothDevice) throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean result = (Boolean) createBondMethod.invoke(bluetoothDevice);
        System.out.println("This is the result: " + result);
        return result;
    }//end createBond method

    public void connect(BluetoothDevice bluetoothDevice){
        bluetoothSocket = createSocket(bluetoothDevice);

        if(bluetoothSocket != null) {
            try {
                bluetoothSocket.connect();

                //Create Message
                Toast.makeText(getApplicationContext(), "Device connected", Toast.LENGTH_LONG).show();
            }//end try block
            catch (Exception e) {
                //Create Message
                Toast.makeText(getApplicationContext(), "Device failed to connect", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }//end catch block
        }//end if statement
    }//end waitForConnectMethod

    public void disconnect(BluetoothSocket socket){
        try{
            //close the socket
            socket.close();

            //make original socket null again
            bluetoothSocket = null;
            Toast.makeText(getApplicationContext(), "Previous Device Disconnected", Toast.LENGTH_SHORT).show();
        }//end try block
        catch (IOException e){
        }//end catch block
    }//end disconnect method

    private static BluetoothSocket createSocket(BluetoothDevice device){
        BluetoothSocket socket = null;
        try{
            Method m = device.getClass().getMethod("createRfcommSocket", int.class);
            socket = (BluetoothSocket) m.invoke(device, 1);
        }//end try method
        catch(Exception e){
            e.printStackTrace();
        }//end catch method

        return socket;
    }//end createSocket class

    @Override
    protected void onStop(){
        super.onStop();
        try{
            //Unregister receiver
            unregisterReceiver(bluetoothReceiver);
        }
        catch(Exception e){

            e.printStackTrace();
        }//end catch method
    }

    @Override
    protected void onDestroy() {
        //TODO Auto-generated method stub
        super.onDestroy();

    }//end onDestroy method
}