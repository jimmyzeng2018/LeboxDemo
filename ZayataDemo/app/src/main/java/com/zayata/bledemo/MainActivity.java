package com.zayata.bledemo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.zayata.bluetoothble.LeboxManager;
import com.zayata.bluetoothble.callback.LeboxGattCallback;
import com.zayata.bluetoothble.callback.LeboxListenerCallback;
import com.zayata.bluetoothble.callback.LeboxNotifyCallback;
import com.zayata.bluetoothble.callback.LeboxScanCallback;
import com.zayata.bluetoothble.bean.DeviceBean;
import com.zayata.bluetoothble.exception.LeboxException;
import com.zayata.bluetoothble.utils.HexUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  {
    
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;


    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initView();

        LeboxManager.getInstance().init(getApplication());
        LeboxManager.getInstance().enableLog(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeboxManager.getInstance().disconnect();
        LeboxManager.getInstance().destroy();
    }


    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_start:
                        startScan();
                        break;
                    case R.id.action_stop:
                        LeboxManager.getInstance().cancelScan();
                        break;
                }
                return true;
            }
        });

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(DeviceBean bleDevice) {
                if (!bleDevice.equals(LeboxManager.getInstance().getConnectedDevice())) {
                    LeboxManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onReadWrite(DeviceBean bleDevice) {
                if (LeboxManager.getInstance().isDeviceConnected(bleDevice)) {
                    Intent intent = new Intent(MainActivity.this, ReadWriteActivity.class);
                    startActivity(intent);
                }
            }
        });
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);
        listView_device.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkPermissions();
            }
        },1000);
    }


    //如果有Menu,创建完后,系统会自动添加到ToolBar上
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    private void startScan() {
        LeboxManager.getInstance().cancelScan();
        LeboxManager.getInstance().scan(new LeboxScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage(getString(R.string.start_scan));
                mProgressDialog.show();
            }

            @Override
            public void onLeScan(DeviceBean bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(DeviceBean bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<DeviceBean> scanResultList) {
                mProgressDialog.dismiss();
            }
        });
    }

    private void connect(final DeviceBean bleDevice) {
        LeboxManager.getInstance().connect(bleDevice.getMac(), new LeboxGattCallback() {
            @Override
            public void onStartConnect() {

                mToolbar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog = new ProgressDialog(MainActivity.this);
                        mProgressDialog.setMessage(getString(R.string.connecting));
                        mProgressDialog.show();
                    }
                }, 0);

            }

            @Override
            public void onConnectFail(final LeboxException exception) {
                mToolbar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this,

                                getString(R.string.connect_fail) + ":" + exception.getDescription(), Toast.LENGTH_LONG).

                                show();
                    }
                }, 0);
            }

            @Override
            public void onConnectSuccess(DeviceBean bleDevice, BluetoothGatt gatt, int status) {
                mToolbar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        mDeviceAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, getString(R.string.connect_success), Toast.LENGTH_LONG).show();
                    }
                }, 0);
                bleDevice.getLeboxImp().setDataChangeListener(new LeboxListenerCallback() {
                    @Override
                    public void onDataChangeNotify(byte cmd, byte[] data) {

                    }
                });
            }

            @Override
            public void onDisConnected(final boolean isActiveDisConnected, final DeviceBean bleDevice, BluetoothGatt gatt, int status) {
                mToolbar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();

                        mDeviceAdapter.removeDevice(bleDevice);
                        mDeviceAdapter.notifyDataSetChanged();

                        if (isActiveDisConnected) {
                            Toast.makeText(MainActivity.this, bleDevice.getName() + getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, bleDevice.getName() + getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 0);
                bleDevice.getLeboxImp().clearDataChangeListener();
            }
        });
    }



    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            LeboxManager.getInstance().askEnableBluetooth(this);
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                startScan();
            }
        }
        if (requestCode == LeboxManager.REQUEST_BLUETOOTH){
            if (resultCode == RESULT_OK){
                checkPermissions();
            }
        }
    }

}
