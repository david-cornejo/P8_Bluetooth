package com.example.practica_8_bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERABLE_BT = 2;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 3;
    CheckBox cbhabilitar, cbvisible;
    ImageView emparejar;
    TextView nombre;
    ListView lista;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> DispositivosEmparejados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cbhabilitar = findViewById(R.id.cbhabilitar);
        cbvisible = findViewById(R.id.cbvisible);
        emparejar = findViewById(R.id.Emparejar);
        nombre = findViewById(R.id.tvNombre);
        lista = findViewById(R.id.Lista);
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            Toast.makeText(this, "Bluetooth no soportado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions();
        } else {
            checkBluetoothState();
        }
        cbhabilitar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                if (hasBluetoothPermissions()) {
                    disableBluetooth();
                } else {
                    requestBluetoothPermissions();
                }
            } else {
                if (hasBluetoothPermissions()) {
                    enableBluetooth();
                } else {
                    requestBluetoothPermissions();
                }
            }
        });
        cbvisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (hasBluetoothPermissions()) {
                    makeDeviceDiscoverable();
                } else {
                    requestBluetoothPermissions();
                }
            }
        });
        emparejar.setOnClickListener(v -> {
            if (hasBluetoothPermissions()) {
                listBondedDevices();
            } else {
                requestBluetoothPermissions();
            }
        });
    }

    private boolean hasBluetoothPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) ==
                        PackageManager.PERMISSION_GRANTED &&
                (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                                PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) ==
                                        PackageManager.PERMISSION_GRANTED));
    }

    private void requestBluetoothPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.BLUETOOTH);
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }
        ActivityCompat.requestPermissions(this,
                permissions.toArray(new String[0]),
                REQUEST_BLUETOOTH_PERMISSION);
    }

    private void checkBluetoothState() {
        if (!BA.isEnabled()) {
            enableBluetooth();
        }
        nombre.setText(getBluetoothLocalName());
    }

    private void enableBluetooth() {
        Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentOn, REQUEST_ENABLE_BT);
    }

    private void disableBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            BA.disable();
            Toast.makeText(MainActivity.this, "Apagado", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeDeviceDiscoverable() {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, REQUEST_DISCOVERABLE_BT);
        Toast.makeText(MainActivity.this, "Visible por 2 minutos", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBluetoothState();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                checkBluetoothState();
            } else {
                Toast.makeText(this, "Encendido cancelado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_DISCOVERABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Visibilidad cancelada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void listBondedDevices() {
        DispositivosEmparejados = BA.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();
        for (BluetoothDevice bt : DispositivosEmparejados) {
            list.add(bt.getName());
        }
        if (list.isEmpty()) {
            Toast.makeText(this, "No hay dispositivos emparejados", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mostrando dispositivos", Toast.LENGTH_SHORT).show();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
            lista.setAdapter(adapter);
        }
    }

    public String getBluetoothLocalName() {
        if (BA == null) {
            BA = BluetoothAdapter.getDefaultAdapter();
        }
        String name = BA.getName();
        if (name == null) {
            name = BA.getAddress();
        }
        return name;
    }
}
