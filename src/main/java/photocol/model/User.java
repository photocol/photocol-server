package photocol.model;

public class User {
    private String username;
    private String passwordHash;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean validatePasswordHash(String testHash) {
        return testHash.equals(passwordHash);
    }
}
