package photocol.definitions.request;

import photocol.definitions.User;

public class SignupRequest {
    public String username;
    public String email;
    public String passwordHash;

    public SignupRequest(String username, String email, String passwordHash) {
        this.username = username;
        this.email =
        this.passwordHash = passwordHash;
    }

    public boolean isValid() {
        return this.username!=null && this.passwordHash!=null;
    }

    public User toUser() {
        return new User(username, email, passwordHash);
    }
}
