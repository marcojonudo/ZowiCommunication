package zowicommunication.zowi.marco.zowicommunication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket bluetoothSocket;
    private BluetoothReceiver bluetoothReceiver;
    private OutputStream outputStream;

    private static final int REQUEST_COARSE_LOCATION = 2;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String ZOWI_NAME = "Zowi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothConnectivity(bluetoothAdapter);

        checkLocationPermission();

        //getPairedDevices();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    private void checkLocationPermission() {
        int accessCoarseLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
        }
        else {
            String zowiAddress = getZowiAddress();
            if (zowiAddress.equals("")) {
                startDiscovery();
            }
            else {
                connectDevice(zowiAddress);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery();
                }
                break;
            default:
                break;
        }
    }

    private void connectDevice(String zowiAddress) {
        BluetoothDevice zowiDevice = bluetoothAdapter.getRemoteDevice(zowiAddress);
        try {
            bluetoothSocket = zowiDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Log.i("connectDevice", "bluetoothSocket conectado");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startDiscovery() {
        bluetoothReceiver = new BluetoothReceiver();

        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(bluetoothReceiver, bluetoothFilter);

        bluetoothAdapter.startDiscovery();
    }

    private void checkBluetoothConnectivity(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show();
        }
        else if (!bluetoothAdapter.isEnabled()) {
            //Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            bluetoothAdapter.enable();
        }
    }

    private String getZowiAddress() {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(ZOWI_NAME)) {
                    return device.getAddress();
                }
            }
        }
        return "";
    }

    public void sendCommand(View v) {
        EditText command = (EditText) findViewById(R.id.command);
        String commandText = command.getText().toString();
        char r = '\r';

        try {
            outputStream.write(commandText.getBytes());
            outputStream.write(r);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.i("BluetoothReceiver", "Discovery started");
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.i("BluetoothReceiver", "Discovery finished");
            }
            else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getName().equals(ZOWI_NAME)) {
                    bluetoothAdapter.cancelDiscovery();
                    device.createBond();
                }
                Log.i("BluetoothReceiver", "Device discovered " + device.getName() + ": " + device.getAddress());
            }
            else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                int bondState = device.getBondState();

                if (name.equals(ZOWI_NAME) && (bondState == BluetoothDevice.BOND_BONDED)) {
                    connectDevice(device.getAddress());
                }
            }
        }
    }
}
