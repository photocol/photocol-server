package photocol.definitions;

import java.util.Date;

public class Photo {
    public String uri;
    public String description;
    public Date uploadDate;

    public Photo(String uri, String description, Date uploadDate) {
        this.uri = uri;
        this.description = description;
        this.uploadDate = uploadDate;
    }
}
