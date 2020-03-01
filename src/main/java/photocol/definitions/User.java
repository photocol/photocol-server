package photocol.definitions;

public class User {
    private String username;
    private String email;
    private String passwordHash;


    public User(String username,String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return this.username;
    }
    public String getEmail() { return this.email;}
    public boolean validatePasswordHash(String testHash) {
        return testHash.equals(passwordHash);
    }
}
