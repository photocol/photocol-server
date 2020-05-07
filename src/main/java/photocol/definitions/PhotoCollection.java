package photocol.definitions;

import java.util.List;

public class PhotoCollection {
    public boolean isPublic;
    public String name;
    public List<ACLEntry> aclList;
    public String uri;
    public String owner;
    public String description;
    public String coverPhotoUri;
    public int coverPhotoPid;
    public List<Photo> photos;

    // this constructor used when creating a photo collection
    public PhotoCollection(boolean isPublic, String name) {
        this.isPublic = isPublic;
        this.name = name;
        this.generateUri();
    }

    // this constructor used for all other instances
    public PhotoCollection(boolean isPublic, String name, List<ACLEntry> aclList, String coverPhotoUri, String description) {
        this.isPublic = isPublic;
        this.name = name;
        this.aclList = aclList;
        this.coverPhotoUri = coverPhotoUri;
        this.description = description;
        this.generateUri();
    }

    // photolist only set when receiving photos
    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    // generate URI from name
    public void generateUri() {
        if(name==null) return;
        uri = name.toLowerCase().replaceAll(" ", "-").replaceAll("[^a-zA-Z0-9\\-]", "");
    }
}
