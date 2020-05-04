package photocol.definitions;

public class User {
    public String username;
    public String password;
    public String email;

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

    public String getUsername() {
        return this.username;
    }
    public String getEmail() { return this.email;}

}
