package es.pfctonimartos.tpms;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler handler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt gatt;

    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String WRITE_UUID = "0000ffe9-0000-1000-8000-00805f9b34fb";
    public final static String READ_UUID = "0000ffe4-0000-1000-8000-00805f9b34fb";
    public final static String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";

    public final static String QUERYID = "FC02D52B";
    private TextView FLpressure;
    private TextView FLtemperature;

    private TextView FRpressure;
    private TextView FRtemperature;

    private TextView RLpressure;
    private TextView RLtemperature;

    private TextView RRpressure;
    private TextView RRtemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();

        handler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

    }

    private void setUpViews() {
        FLpressure = (TextView) findViewById(R.id.frontLeftPressure);
        FLtemperature = (TextView) findViewById(R.id.frontLeftTemperature);

        FRpressure = (TextView) findViewById(R.id.frontRightPressure);
        FRtemperature = (TextView) findViewById(R.id.frontRightTemperature);

        RLpressure = (TextView) findViewById(R.id.rearLeftPressure);
        RLtemperature = (TextView) findViewById(R.id.rearLeftTemperature);

        RRpressure = (TextView) findViewById(R.id.rearRightPressure);
        RRtemperature = (TextView) findViewById(R.id.rearRightTemperature);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mLEScanner = bluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (gatt == null) {
            return;
        }
        gatt.close();
        gatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(filters, settings, mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            byte[] advertisement = scanRecord;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("onLeScan", device.toString());
                    connectToDevice(device);
                }
            });
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        if (gatt == null && device.toString().equalsIgnoreCase("D0:B5:C2:E8:40:6B")) {
            gatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
            // Por cada servicio recibido
            for (BluetoothGattService service : services) {
                // Buscamos en cada una de sus características
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    // Si la caracterísitca es WRITE
                    if (characteristic.getUuid().toString().equalsIgnoreCase(WRITE_UUID)) {
                        Log.i("WRITE_UUID GOT:", WRITE_UUID);
                        sendCharacteristic(gatt, characteristic);
                    }
                    // Si la característica es READ, configuramos una notificación
                    if (characteristic.getUuid().toString().equalsIgnoreCase(READ_UUID)) {
                        Log.i("READ_UUID GOT:", READ_UUID);
                        gatt.setCharacteristicNotification(characteristic, true);
                    }
                    // Si la característica es SERVICE, guardamos el dispositivo
                    if (characteristic.getUuid().toString().equalsIgnoreCase(SERVICE_UUID)) {
                        Log.i("SERVICE_UUID GOT:", SERVICE_UUID);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == gatt.GATT_SUCCESS) {
                byte[] arrayOfByte = characteristic.getValue();
                // Procesado posterior
                Log.i("onCharacteristicRead", characteristic.toString());
                gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // le pasaremos la QUERY por aquí
            //writeBytes(UtilsConfig.chatOrders(QUERYID));
            Log.i("onCharacteristicWrite", characteristic.toString());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] arrayOfByte = characteristic.getValue();
            //if(arrayOfByte[4]!=0 && arrayOfByte[5]!=0) {
            showData(arrayOfByte);
            //}
            gatt.readCharacteristic(characteristic);
            // Esta función se lanzará cuando recibamos una respuesta tras ejecutar un << writeCharacteristic(characteristic) >>
            // También se autodispara en caso de activar notificaciones
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

    };

    public void sendCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] query = QUERYID.getBytes();
        byte[] data = chatOrder(query);
        characteristic.setValue(data);
        gatt.writeCharacteristic(characteristic);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public static byte[] chatOrder(byte[] m) {
        if (m.length % 2 != 0) {
            return null;
        }
        byte[] bytes = new byte[(m.length / 2)];
        int i = 0;
        int j = 0;
        while (i < m.length) {
            bytes[j] = UnitsManager.uniteByte(m[i], m[i + 1]);
            i += 2;
            j += 1;
        }
        return bytes;
    }

    public void showData(byte[] data) {
        int order = data[2];
        int sensorId = data[3];

        switch (order) {
            case -43:
                getSensorsIds(data);
                break;
            case 1:
                getCurrentId(sensorId);
                getTyre(sensorId, data);
                break;
        }
    }

    public static void getSensorsIds(byte[] data) {
        byte[] items = new byte[4];
        System.arraycopy(data, 3, items, 0, items.length);
        Log.d("ID1", UnitsManager.bytesToHex(items));
        System.arraycopy(data, 7, items, 0, items.length);
        Log.d("ID2", UnitsManager.bytesToHex(items));
        System.arraycopy(data, 11, items, 0, items.length);
        Log.d("ID3", UnitsManager.bytesToHex(items));
        System.arraycopy(data, 15, items, 0, items.length);
        Log.d("ID4", UnitsManager.bytesToHex(items));
    }

    public static void getCurrentId(int sensorId) {
        Integer sensor = sensorId;
        switch (sensor) {
            case 1:
                Log.d("FRONT LEFT SENSOR", sensor.toString());
                break;
            case 2:
                Log.d("FRONT RIGHT SENSOR", sensor.toString());
                break;
            case 3:
                Log.d("REAR LEFT SENSOR", sensor.toString());
                break;
            case 4:
                Log.d("REAR RIGHT SENSOR", sensor.toString());
                break;
        }
    }

    public Tyre getTyre(int sensorId, byte[] data) {

        HashMap<String, String> tyreData = new HashMap<>();
        tyreData.put("IR", Integer.valueOf(UnitsManager.getAbsValue(data[4])).toString());
        tyreData.put("TY", Integer.valueOf(UnitsManager.getAbsValue(data[5])).toString());
        tyreData.put("TW", Integer.valueOf(UnitsManager.getAbsValue(data[6])).toString());
        tyreData.put("DL", Integer.valueOf(UnitsManager.getAbsValue(data[7])).toString());
        tyreData.put("DISTYPE", Integer.valueOf(UnitsManager.getAbsValue(data[8])).toString());

        final Tyre tyre = getTyreData(tyreData, sensorId);

        byte dd = (byte) tyre.getIr();
        Integer ir = UnitsManager.byteToInt(dd, sensorId);

        String pressure = UnitsManager.getPressureValue(tyre.getTy());
        String temperature = UnitsManager.getTemValue(tyre.getTw());

        Log.d("Pressure: ", pressure);
        Log.d("Temperature: ", temperature);
        Log.d("Last_IR: ", ir.toString());

        printInView(pressure, temperature, sensorId);

        return tyre;
    }

    private String preparePropertyName (final String propertyName, final int sensorId){
        String sensorPosition = "";
        String sensorName;
        String property = propertyName.toUpperCase();

        switch (sensorId){
            case 1:
                sensorPosition = "FL_WHEEL_";
                break;
            case 2:
                sensorPosition = "FR_WHEEL_";
                break;
            case 3:
                sensorPosition = "RL_WHEEL_";
                break;
            case 4:
                sensorPosition = "RR_WHEEL_";
                break;
        }

        sensorName = sensorPosition.concat(propertyName);

        return sensorName;
    }
    private String preparePropertyUnit (final String propertyName){

        String propertyUnit = "";

        switch (propertyName){
            case "pressure":
                propertyUnit = "bar";
                break;
            case "temperature":
                propertyUnit = "ºC";
                break;
        }

        return propertyUnit;
    }

    private JSONObject createJSONObject (final String propertyName, final String propertyValue, final int sensorId) throws JSONException {

        Date timestamp = new Date();

        JSONObject dataPoint = new JSONObject();
        JSONObject dataPoints = new JSONObject();
        JSONObject singleSensor = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        dataPoint.put("time", timestamp);
        dataPoint.put("value", propertyValue);

        dataPoints.put("data_points", dataPoint);

        singleSensor.put("data_points", dataPoint);
        singleSensor.put("sensor_name", propertyName);
        singleSensor.put("units", propertyValue);

        jsonObject.put("device_id", sensorId);
        jsonObject.put("sensors", singleSensor);

        return jsonObject;
    }

    private void printInView(final String pressure, final String temperature, final int sensorId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (sensorId){
                    case 1:
                        FLpressure.setText(pressure.concat(" Bar"));
                        FLtemperature.setText(temperature.concat(" ºC"));
                        break;
                    case 2:
                        FRpressure.setText(pressure.concat(" Bar"));
                        FRtemperature.setText(temperature.concat(" ºC"));
                        break;
                    case 3:
                        RLpressure.setText(pressure.concat(" Bar"));
                        RLtemperature.setText(temperature.concat(" ºC"));
                        break;
                    case 4:
                        RRpressure.setText(pressure.concat(" Bar"));
                        RRtemperature.setText(temperature.concat(" ºC"));
                        break;
                }
            }
        });
    }

    private static Tyre getTyreData(HashMap<String, String> tyreData, int id) {

        Tyre tyre = new Tyre();

        tyre.setId(id);
        tyre.setIr(Integer.parseInt(tyreData.get("IR")));
        tyre.setDl(Integer.parseInt(tyreData.get("DL")));
        tyre.setTy(Integer.parseInt(tyreData.get("TY")));
        tyre.setTw(Integer.parseInt(tyreData.get("TW")));
        tyre.setDis(Integer.parseInt(tyreData.get("DISTYPE")));

        return tyre;
    }


    /********************************/
    /* DEFAULT GATT CHARACTERISTICS */
    /********************************/
    // ScanResult{mDevice=D0:B5:C2:E8:40:6B,
    // mScanRecord=ScanRecord [ mAdvertiseFlags=6,
    //                          mServiceUuids=[0000fff0-0000-1000-8000-00805f9b34fb, 0000ffb0-0000-1000-8000-00805f9b34fb],
    //                          mManufacturerSpecificData={0=[-1, -1, -1, -1, 100, 0, -1]},
    //                          mServiceData={},
    //                          mTxPowerLevel=0,
    //                          mDeviceName=ZhiXuan-TPMS],
    // mRssi=-49,
    // mTimestampNanos=207430302345260}
}
