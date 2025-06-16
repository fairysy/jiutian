package cn.itcast.hilink;

import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DirbTaskThreaded implements Runnable {

    private final String baseUrl;
    private final TextView resultView;
    private final String userAgent;
    private final ExecutorService executor;
    private volatile boolean isCancelled = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public DirbTaskThreaded(String baseUrl, TextView resultView, String userAgent) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.resultView = resultView;
        this.userAgent = (userAgent == null || userAgent.isEmpty()) ? "Mozilla/5.0 (Android)" : userAgent;
        this.executor = Executors.newFixedThreadPool(10);
        trustAllCerts();
    }

    public void cancel() {
        isCancelled = true;
        executor.shutdownNow();
    }

    @Override
    public void run() {
        final StringBuilder logBuilder = new StringBuilder();

        CountDownLatch latch = new CountDownLatch(wordlist.size());

        for (String path : wordlist) {
            if (isCancelled) break;
            final String fullUrl = baseUrl + path;

            executor.submit(() -> {
                if (isCancelled || Thread.currentThread().isInterrupted()) {
                    latch.countDown();
                    return;
                }
                Response res = performRequest(fullUrl);



                handler.post(() -> {
                    String logText;
                    int color;
                    if (res.error != null) {
                        logText = String.format("[ERR] %s - %s (%dms)\n", fullUrl, res.error, res.time);
                        color = 0xFFFF0000; // 红色
                    } else if (res.code >= 200 && res.code < 300) {
                        logText = String.format("[%3d] %s (%dms, %sB)\n",
                                res.code,
                                fullUrl,
                                res.time,
                                (res.length >= 0 ? res.length : "?"));
                        color = 0xFF00AAFF; // 蓝色成功状态你也可以换颜色
                    } else if (res.code >= 400) {
                        logText = String.format("[%3d] %s (%dms, %sB)\n",
                                res.code,
                                fullUrl,
                                res.time,
                                (res.length >= 0 ? res.length : "?"));
                        color = 0xFFFF0000; // 红色错误状态码
                    } else {
                        logText = String.format("[%3d] %s (%dms, %sB)\n",
                                res.code,
                                fullUrl,
                                res.time,
                                (res.length >= 0 ? res.length : "?"));
                        color = 0xFF888888; // 其他灰色
                    }

                    int start = resultView.getText().length();
                    resultView.append(logText);
                    int end = resultView.getText().length();

                    // 设置颜色
                    Spannable spannable = (Spannable) resultView.getText();
                    spannable.setSpan(new android.text.style.ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                });

                // 这里不频繁刷新UI，避免卡顿
                latch.countDown();
            });
        }

        try {
            // 等待所有任务结束或取消
            latch.await();
        } catch (InterruptedException e) {
            // 如果中断，设置取消标志
            isCancelled = true;
        }

        executor.shutdownNow();

        // 扫描结束后，统一更新UI和提示
        handler.post(() -> {
            resultView.append(logBuilder.toString());
            Toast.makeText(
                    resultView.getContext(),
                    isCancelled ? "扫描已取消" : "扫描结束",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private Response performRequest(String urlStr) {
        long startTime = System.currentTimeMillis();
        int responseCode = -1;
        int contentLength = -1;
        String errorMsg = null;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn;

            if (url.getProtocol().equalsIgnoreCase("https")) {
                trustAllCerts();
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setHostnameVerifier((hostname, session) -> true);
                conn = httpsConn;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setInstanceFollowRedirects(true);

            try {
                responseCode = conn.getResponseCode();
                contentLength = conn.getContentLength();
            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        return new Response(responseCode, contentLength, elapsedTime, errorMsg);
    }

    private void trustAllCerts() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] xcs, String s) {
                        }

                        public void checkServerTrusted(X509Certificate[] xcs, String s) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception ignored) {
        }
    }

    private static class Response {
        int code;
        int length;
        long time;
        String error;

        Response(int code, int length, long time, String error) {
            this.code = code;
            this.length = length;
            this.time = time;
            this.error = error;
        }
    }

    private final List<String> wordlist = Arrays.asList(
            // 常见管理后台与登录路径
            "admin", "admin123", "administrator", "login", "user", "users", "manager", "member", "dashboard",
            "system", "sysadmin", "root", "control", "backend", "console", "auth", "signin", "signup", "secure",
            "admin/login", "admin_area", "admincp", "cms", "adminpanel", "adminarea", "adm", "panel", "cpanel",
            "wp-admin", "wp-login", "webadmin", "backend", "index", "home", "main", "portal", "secure", "access",

            // 数据相关
            "data", "database", "sql", "db", "mysql", "dbadmin", "data_backup", "backup", "dump", "sql_dump",

            // 上传路径
            "upload", "uploads", "files", "file", "file_upload", "upload_file", "media", "img", "image", "images",

            // 前端静态资源路径
            "css", "js", "javascript", "style", "assets", "static", "src", "public", "themes", "dist", "lib", "font",

            // 接口路径
            "api", "api/v1", "api/v2", "api/admin", "rest", "services", "service", "rpc", "json", "soap", "endpoint",

            // 文件
            "robots.txt", "sitemap.xml", "crossdomain.xml", "README.md", "LICENSE", "config.json", "config.php",
            ".env", ".git", ".svn", ".htaccess", ".htpasswd", ".gitignore", "web.config", "server-status",

            // 安装与测试
            "install", "setup", "test", "phpinfo", "info", "status", "health", "diagnostics", "debug", "monitor",

            // 框架特征路径
            "laravel", "symfony", "thinkphp", "tp", "admin.php", "index.php", "main.php", "server.php",
            "web.php", "route.php", "router.php", "routes", "modules", "mod", "app", "apps",

            // 云平台/开发平台路径
            "aws", "azure", "gcp", "firebase", "cloud", "admin/aws", "admin/azure", "admin/firebase",

            // 安全隐患目录
            "backup.zip", "backup.tar", "site.bak", "db.bak", "website.zip", "test.zip", "data.tar.gz",
            "logs", "log", "error_log", "debug.log", "access.log", "tmp", "temp", "cache", "session",

            // 语言特定路径
            "zh", "en", "cn", "us", "fr", "de", "ja", "ko", "es", "ru", "lang", "locale", "locales", "translations",

            // 管理目录变体
            "adm1n", "4dm1n", "4dmin", "administrator1", "managers", "superadmin", "superuser", "authadmin",

            // 用户变体
            "user1", "user2", "member1", "profile", "account", "accounts", "userprofile", "userinfo", "me",

            // 敏感路径
            "secret", "private", "hidden", "config", "configuration", "conf", "setup", "init", "install.php",

            // 登录模块扩展
            "login.php", "login.html", "auth/login", "user/login", "admin/login.php", "account/login", "member/login",

            // SEO 与 sitemap
            "seo", "sitemap", "robots", "track", "analytics", "google", "bing", "yahoo", "baidu", "stats", "counter",

            // 异常处理
            "404", "403", "401", "500", "error", "exception", "fail", "failure", "notfound", "invalid", "unauthorized",

            // 框架测试入口
            "test.php", "debug.php", "dev.php", "build", "examples", "samples", "demo", "demo1", "demo2", "demo3",

            // 设备与接口
            "mobile", "m", "api/device", "device", "devices", "client", "clients", "phone", "android", "ios",

            // 第三方工具残留
            "phpmyadmin", "adminer", "mysqladmin", "pma", "tools", "monitoring", "zabbix", "grafana", "kibana",

            // VPN/隧道/代理
            "vpn", "proxy", "socks", "ipsec", "openvpn", "v2ray", "wireguard", "ssh", "shell", "terminal", "cli",

            // CTF&漏洞环境
            "dvwa", "mutillidae", "bWAPP", "vulnerabilities", "vuln", "xss", "sqli", "csrf", "lfi", "rce", "cmd", "shell",

            // 开发者工具和插件
            "dev", "developer", "webpack", "node_modules", "composer.json", "package.json", "yarn.lock", "gulpfile.js",

            // 其他扩展词（手动添加）
            "html", "php", "asp", "jsp", "cgi", "aspx", "index.php", "main.php", "home.php", "home.html", "landing",
            "start", "main", "go", "welcome", "new", "default", "mainpage", "verify", "captcha", "oauth", "connect",
            "reset", "reset-password", "forgot", "forgot-password", "changepass", "changepassword", "token", "apikey",

            // 结尾
            "admin2", "admin3", "controlpanel", "config_backup", "backup_admin", "bak", "oldadmin", "webadmin_old"
    );
}

