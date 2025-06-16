package cn.itcast.hilink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<Device> deviceList; // 存储设备的列表
    private OnConnectClickListener listener; // 监听器，用于处理连接按钮的点击事件

    // 构造方法
    public DeviceAdapter(List<Device> deviceList, OnConnectClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建视图
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        // 获取当前设备
        Device device = deviceList.get(position);

        // 设置设备信息
        holder.deviceNameTextView.setText(device.getName());
        holder.deviceIpTextView.setText(device.getIp());
        holder.deviceTypeTextView.setText(device.getType());
        holder.macAddressTextView.setText(device.getMac());
        holder.onlineStatusTextView.setText(device.isOnline() ? "在线" : "离线");
        holder.responseTimeTextView.setText(device.getResponseTime() + "ms");

        // 处理扫描状态
        if (device.isScanning()) {
            // 扫描中 - 显示进度
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressText.setVisibility(View.VISIBLE);
            holder.openPortsTextView.setVisibility(View.GONE);
            holder.connectButton.setEnabled(false);

            holder.progressBar.setProgress(device.getScanProgress());
            holder.progressText.setText(
                    "扫描进度: " + device.getScanProgress() + "% (" +
                            device.getScannedPorts() + "/65535)"
            );
        } else {
            // 非扫描状态 - 显示端口信息
            holder.progressBar.setVisibility(View.GONE);
            holder.progressText.setVisibility(View.GONE);
            holder.openPortsTextView.setVisibility(View.VISIBLE);
            holder.connectButton.setEnabled(true);

            // 显示开放的端口
            StringBuilder ports = new StringBuilder("开放端口: ");
            if (device.getOpenPorts() == null || device.getOpenPorts().isEmpty()) {
                ports.append("无");
            } else {
                for (int port : device.getOpenPorts()) {
                    ports.append(port).append(", ");
                }
                // 如果端口列表非空，移除最后一个逗号
                if (ports.length() > 0) {
                    ports.setLength(ports.length() - 2);
                }
            }
            holder.openPortsTextView.setText(ports.toString());
        }

        // 连接按钮的点击事件
        holder.connectButton.setOnClickListener(v -> {
            if (listener != null && device.isOnline()) {
                listener.onConnectClick(device.getIp(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size(); // 返回设备的数量
    }

    // ViewHolder 类，绑定视图中的各个控件
    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView deviceNameTextView;
        TextView deviceIpTextView;
        TextView deviceTypeTextView;
        TextView macAddressTextView;
        TextView onlineStatusTextView;
        TextView responseTimeTextView;
        TextView openPortsTextView;
        Button connectButton;
        ProgressBar progressBar;
        TextView progressText;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceName);
            deviceIpTextView = itemView.findViewById(R.id.deviceIP);
            deviceTypeTextView = itemView.findViewById(R.id.deviceType);
            macAddressTextView = itemView.findViewById(R.id.macAddress);
            onlineStatusTextView = itemView.findViewById(R.id.onlineStatus);
            responseTimeTextView = itemView.findViewById(R.id.responseTime);
            openPortsTextView = itemView.findViewById(R.id.openPorts);
            connectButton = itemView.findViewById(R.id.connectButton);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
        }
    }

    // 定义一个接口用于处理连接按钮点击事件
    public interface OnConnectClickListener {
        void onConnectClick(String ip, int position); // 添加位置参数
    }
}