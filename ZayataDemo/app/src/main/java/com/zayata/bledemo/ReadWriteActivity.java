package com.zayata.bledemo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zayata.bluetoothble.LeboxManager;
import com.zayata.bluetoothble.callback.LeboxVolumeCallback;
import com.zayata.bluetoothble.callback.LeboxWriteCallback;
import com.zayata.bluetoothble.exception.LeboxException;

public class ReadWriteActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_write);

        initData();
        initView();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(LeboxManager.getInstance().getConnectedDevice().getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initData() {

        if (LeboxManager.getInstance().getConnectedDevice() == null)
            finish();

        LeboxManager.getInstance().getConnectedDevice().getLeboxImp().getVolume(new LeboxVolumeCallback() {

            @Override
            public void onReadSuccess(int volume) {

            }

            @Override
            public void onReadFailure(LeboxException exception) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }




}
