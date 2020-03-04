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
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return this.username;
    }
    public String getEmail() { return this.email;}

}
