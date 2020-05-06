package photocol.definitions;

import java.util.Date;

public class Photo {
    public String uri;
    public String filename;
    public String caption;
    public Date uploadDate;
    public Metadata metadata;

    public class Metadata {
        double exposureTime;
        double fNumber;
        int iso;
        int width;
        int height;
        Date captureDate;
    }

    public Photo(String uri, String caption, Date uploadDate) {
        this.uri = uri;
        this.caption = caption;
        this.uploadDate = uploadDate;
    }
}
