package cn.itcast.hilink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {

    private final List<DeviceInfoItem> items;

    public DeviceInfoAdapter(List<DeviceInfoItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvContent = view.findViewById(R.id.tvContent);
        }
    }

    @Override
    public DeviceInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceInfoAdapter.ViewHolder holder, int position) {
        DeviceInfoItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvContent.setText(item.content);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
