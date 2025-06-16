package cn.itcast.hilink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_LINK = 1;
    private static final int TYPE_VIDEO = 2;

    private List<WebContent> items = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_IMAGE:
                return new ImageViewHolder(inflater.inflate(R.layout.item_image, parent, false));
            case TYPE_LINK:
                return new LinkViewHolder(inflater.inflate(R.layout.item_link, parent, false));
            case TYPE_VIDEO:
                return new VideoViewHolder(inflater.inflate(R.layout.item_video, parent, false));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WebContent item = items.get(position);
        if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind((WebContent.Image) item);
        } else if (holder instanceof LinkViewHolder) {
            ((LinkViewHolder) holder).bind((WebContent.Link) item);
        } else if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).bind((WebContent.Video) item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        WebContent item = items.get(position);
        if (item instanceof WebContent.Image) return TYPE_IMAGE;
        if (item instanceof WebContent.Link) return TYPE_LINK;
        if (item instanceof WebContent.Video) return TYPE_VIDEO;
        return -1;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<WebContent> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;

        ImageViewHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivImage);
        }

        void bind(WebContent.Image image) {
            Glide.with(itemView)
                    .load(image.url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete)
                    .into(ivImage);
        }
    }

    static class LinkViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvUrl;

        LinkViewHolder(View view) {
            super(view);
            tvText = view.findViewById(R.id.tvText);
            tvUrl = view.findViewById(R.id.tvUrl);
        }

        void bind(WebContent.Link link) {
            tvText.setText(link.text);
            tvUrl.setText(link.url);
        }
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView tvVideoUrl;

        VideoViewHolder(View view) {
            super(view);
            tvVideoUrl = view.findViewById(R.id.tvVideoUrl);
        }

        void bind(WebContent.Video video) {
            tvVideoUrl.setText("视频地址: " + video.url);
        }
    }
}
