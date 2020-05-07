package photocol.definitions;

import java.util.Date;

public class Photo {
    public String uri;
    public String filename;
    public String caption;
    public Date uploadDate;
    public PhotoMetadata metadata;

    public static class PhotoMetadata {
        public String mimeType;
        public double exposureTime;
        public double fNumber;
        public int iso;
        public int width;
        public int height;
        public Date captureDate;
    }

    // TODO: replace this constructor with the latter one
    public Photo(String uri, String caption, Date uploadDate) {
        this.uri = uri;
        this.caption = caption;
        this.uploadDate = uploadDate;
    }

    public Photo(String uri, String filename, String caption, Date uploadDate, PhotoMetadata metadata) {
        this.uri = uri;
        this.filename = filename;
        this.caption = caption;
        this.uploadDate = uploadDate;
        this.metadata = metadata;
    }
}
