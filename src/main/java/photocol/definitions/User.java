package photocol.definitions;

public class User {
    public String username;
    public String passwordHash;
    public String email;

    public User(String email, String username,  String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }
    // on login, no username is specified
    public User(String email, String passwordHash) {
        this.email = email;
        // TODO: is it necessary to get username?
        this.username = null;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return this.username;
    }
    public String getEmail() { return this.email;}

}
