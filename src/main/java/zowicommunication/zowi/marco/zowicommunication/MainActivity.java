package zowicommunication.zowi.marco.zowicommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Device Not Available", Toast.LENGTH_SHORT).show();
        }
        else if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Habilita el bluetooth, capullo", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Todo correcto", Toast.LENGTH_SHORT).show();
        }

        getPairedDevices();
    }

    private void getPairedDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.i("Devices", device.getName() + " | " + device.getAddress());
            }
        }
        else {
            Toast.makeText(this, "No paired devices", Toast.LENGTH_SHORT).show();
        }
    }
}
