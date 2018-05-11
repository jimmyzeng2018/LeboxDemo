package com.zayata.bledemo;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zayata.bluetoothble.LeboxManager;
import com.zayata.bluetoothble.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<DeviceBean> bleDeviceList;

    public DeviceAdapter(Context context) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }

    public void addDevice(DeviceBean bleDevice) {
        if (!contains(bleDeviceList,bleDevice))
            bleDeviceList.add(bleDevice);
    }

    private boolean contains(List<DeviceBean> bleDeviceList, DeviceBean bleDevice){
        if (bleDeviceList != null && bleDeviceList.size() > 0){
            for (DeviceBean leboxDevice : bleDeviceList){
                if (leboxDevice.getMac().equals(bleDevice.getMac())){
                    return true;
                }
            }
        }
        return false;
    }

    public void removeDevice(DeviceBean bleDevice) {
        bleDeviceList.remove(bleDevice);
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            DeviceBean device = bleDeviceList.get(i);
            if (LeboxManager.getInstance().isDeviceConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            DeviceBean device = bleDeviceList.get(i);
            if (!LeboxManager.getInstance().isDeviceConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public DeviceBean getItem(int position) {
        if (position > bleDeviceList.size())
            return null;
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.adapter_device, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
            holder.txt_mac = (TextView) convertView.findViewById(R.id.txt_mac);
            holder.layout_idle = (LinearLayout) convertView.findViewById(R.id.layout_idle);
            holder.layout_connected = (LinearLayout) convertView.findViewById(R.id.layout_connected);
            holder.btn_connect = (Button) convertView.findViewById(R.id.btn_connect);
            holder.btn_detail = (Button) convertView.findViewById(R.id.btn_detail);
        }

        final DeviceBean bleDevice = getItem(position);
        if (bleDevice != null) {
            boolean isConnected = LeboxManager.getInstance().isDeviceConnected(bleDevice);
            String name = bleDevice.getName();
            String mac = bleDevice.getMac();
            holder.txt_name.setText(name);
            holder.txt_mac.setText(mac);
            if (isConnected) {
                holder.txt_name.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.txt_mac.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.layout_idle.setVisibility(View.GONE);
                holder.layout_connected.setVisibility(View.VISIBLE);
            } else {
                holder.txt_name.setTextColor(context.getResources().getColor(R.color.dark_gray));
                holder.txt_mac.setTextColor(context.getResources().getColor(R.color.dark_gray));
                holder.layout_idle.setVisibility(View.VISIBLE);
                holder.layout_connected.setVisibility(View.GONE);
            }
        }

        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onConnect(bleDevice);
                }
            }
        });


        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onReadWrite(bleDevice);
                }
            }
        });

        return convertView;
    }

    class ViewHolder {
        TextView txt_name;
        TextView txt_mac;
        LinearLayout layout_idle;
        LinearLayout layout_connected;
        Button btn_connect;
        Button btn_detail;
    }

    public interface OnDeviceClickListener {
        void onConnect(DeviceBean bleDevice);

        void onReadWrite(DeviceBean bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }

}
