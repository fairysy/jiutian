package cn.itcast.hilink;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PackageActivity extends AppCompatActivity {

    private EditText etUrl;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ContentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet);

        initializeViews();
        setupRecyclerView();
        setupButtonListener();
    }

    private void initializeViews() {
        etUrl = findViewById(R.id.etUrl);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        Button btnFetch = findViewById(R.id.btnFetch);
    }

    private void setupRecyclerView() {
        adapter = new ContentAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupButtonListener() {
        findViewById(R.id.btnFetch).setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "请输入URL地址", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!url.startsWith("http")) {
                url = "https://" + url;
                etUrl.setText(url);
            }
            fetchContent(url);
        });
    }

    private void fetchContent(String url) {
        showLoading(true);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Document doc = Jsoup.connect(url).get();
                List<WebContent> items = parseDocument(doc);
                runOnUiThread(() -> {
                    adapter.updateItems(items);
                    showLoading(false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(PackageActivity.this,
                            "抓取失败: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private List<WebContent> parseDocument(Document doc) {
        List<WebContent> items = new ArrayList<>();

        // 解析图片
        for (Element img : doc.select("img")) {
            String src = img.absUrl("src");
            if (!src.isEmpty()) {
                items.add(new WebContent.Image(src));
            }
        }

        // 解析链接
        for (Element a : doc.select("a")) {
            String href = a.absUrl("href");
            String text = a.text();
            if (!href.isEmpty()) {
                items.add(new WebContent.Link(href, text));
            }
        }

        // 解析视频
        for (Element iframe : doc.select("iframe")) {
            String src = iframe.absUrl("src");
            if (src.contains("youtube.com") || src.contains("vimeo.com")) {
                items.add(new WebContent.Video(src));
            }
        }

        return items;
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
