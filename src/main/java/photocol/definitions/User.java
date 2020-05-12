package photocol.definitions;

import java.util.List;

public class User {
    public String username;
    public String password;
    public String email;
    public String displayName;
    public String profilePhoto;
    public int profilePhotoPid;     // used only when updating profile photo
    public List<PhotoCollection> collections;   // used only on profile page

    public User(String email, String username,  String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
    // on login, no email is specified
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    // on getting profile
    public User(String email, String username, String displayName, String profilePhoto) {
        this.email = email;
        this.username = username;
        this.displayName = displayName;
        this.profilePhoto = profilePhoto;
    }

    public String getUsername() {
        return this.username;
    }
    public String getEmail() { return this.email;}

}
