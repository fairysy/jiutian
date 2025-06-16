package cn.itcast.hilink;

public abstract class WebContent {
    public static class Image extends WebContent {
        public final String url;

        public Image(String url) {
            this.url = url;
        }
    }

    public static class Link extends WebContent {
        public final String url;
        public final String text;

        public Link(String url, String text) {
            this.url = url;
            this.text = text;
        }
    }

    public static class Video extends WebContent {
        public final String url;

        public Video(String url) {
            this.url = url;
        }
    }
}