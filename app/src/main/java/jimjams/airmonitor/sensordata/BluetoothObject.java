package jimjams.airmonitor.sensordata;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import jimjams.airmonitor.database.DBAccess;

/**
 * Created by justinmaccreery on 4/23/15.
 */
public class BluetoothObject {

    /**
     * INSTANCE of local data object
     */
    private DBAccess access = DBAccess.getDBAccess();

    /**
     * INSTANCE of sensorDataGenerator
     */
    private SensorDataGenerator generator = SensorDataGenerator.getInstance();

    /**
     * INSTANCES of required bluetooth classes JPM
     */
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;

    /**
     * INSTANCES of bluetooth helper classes JPM
     */
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    /**
     * PUBLIC constructor
     */

    public String BluetoothObject(){
        try {
            findBT();
            openBT();
            // RETURN success
            return "Bluetooth connection open.";
        }
        // CATCH and print IOException JPM
        catch (IOException | NullPointerException ex) {
            // RETURN error
            return ("Error: " + ex);
        }
    }



    /**
     * FINDBT method
     * GETS bluetooth adaptor
     * ATTEMPTS to enable bluetooth if it isn't enabled
     * FINDS correct bluetooth device
     * ASSIGNS correct bluetooth device for use
     * JPM
     */

    private void findBT()
    {
        // INSTANTIATE bluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // IF no bluetooth adaptor
        if(mBluetoothAdapter == null)
        {
            // DO something ?
        }

        // IF bluetooth adaptor found && not enabled
        if(!mBluetoothAdapter.isEnabled())
        {
            // ATTEMPT to enable bluetooth
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
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
     * @throws java.io.IOException
     */
    private void openBT() throws IOException
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
    }

    /**
     * BEGINLISTENINGFORDATA
     * Thread to get updates from AirCasting Device
     * UPDATES values on screen using GenerateSensorData
     */
    private void beginListenForData()
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
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    // TRY to get data
                    try
                    {
                        // GET the amount of bytes available on input stream buffer
                        int bytesAvailable = mmInputStream.available();
                        // IF there are bytes available
                        if(bytesAvailable > 0)
                        {
                            // CREATE array of bytes in packet
                            byte[] packetBytes = new byte[bytesAvailable];
                            // GET bytes
                            mmInputStream.read(packetBytes);
                            // FOR every byte
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                // SET b = this byte from array
                                byte b = packetBytes[i];
                                // IF b is a delimiter
                                if(b == delimiter)
                                {
                                    // SET encodedBytes EQUAL to number of bytes already read
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    // MAKE new array less what's already been read
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            // SEND read data to refreshAQInsert

                                            // UPDATE data table containing latest sensor data
                                            access.updateCurrentData(generator.getData());
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }
}
