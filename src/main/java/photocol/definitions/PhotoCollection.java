package photocol.definitions;

import java.util.List;

public class PhotoCollection {
    public boolean isPublic;
    public String name;
    public List<ACLEntry> aclList;
    public String uri;

    // FIXME: this is a mess of constructors depending on the use case
    public PhotoCollection(boolean isPublic, String name) {
        this.isPublic = isPublic;
        this.name = name;
    }
    public PhotoCollection(boolean isPublic, String name, List<ACLEntry> aclList) {
        this.isPublic = isPublic;
        this.name = name;
        this.aclList = aclList;
    }
    public PhotoCollection(boolean isPublic, String name, String uri, List<ACLEntry> aclList) {
        this.isPublic = isPublic;
        this.name = name;
        this.uri = uri;
        this.aclList = aclList;
    }

    // generate URI from name
    // TODO: update this later
    public void generateUri() {
        if(name==null) return;
        uri = name.toLowerCase().replaceAll(" ", "-").replaceAll("[^a-zA-Z0-9\\-]", "");
    }
}
