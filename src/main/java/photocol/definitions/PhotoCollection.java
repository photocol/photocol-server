package photocol.definitions;

import java.util.List;

public class PhotoCollection {
    public boolean isPublic;
    public String name;
    public List<ACLEntry> aclList;
    public PhotoCollection(boolean isPublic, String name) {
        this.isPublic = isPublic;
        this.name = name;
    }
    public PhotoCollection(boolean isPublic, String name, List<ACLEntry> aclList) {
        this.isPublic = isPublic;
        this.name = name;
        this.aclList = aclList;
    }

    // generate URI from name
    // TODO: update this later
    public String uri() {
        if(this.name==null) return null;
        return this.name.toLowerCase().replaceAll(" ", "-").replaceAll("[^a-zA-Z0-9\\-]", "");
    }
}
