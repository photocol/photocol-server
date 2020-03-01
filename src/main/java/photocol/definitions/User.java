package photocol.definitions;

public class User {
    public String username;
    public String passwordHash;
    public String email;

    public User(String username, String passwordHash, String email ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public String getUsername() {
        return this.username;
    }

}
