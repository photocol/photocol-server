package photocol.definitions.request;

import photocol.definitions.User;

public class SignupRequest {
    public String username;
    public String passwordHash;

    public SignupRequest(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public boolean isValid() {
        return this.username!=null && this.passwordHash!=null;
    }

    public User toUser() {
        return new User(username, passwordHash);
    }
}
