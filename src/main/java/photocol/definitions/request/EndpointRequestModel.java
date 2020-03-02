package photocol.definitions.request;

import photocol.definitions.User;

// define schemas for all endpoint api input request with JSON data
public class EndpointRequestModel {

    // all endpoint requests must have simple validation method and ability to convert to some other internal type
    private interface EndpointRequest<T> {
        boolean isValid();
        T toServiceType();
    }

    // endpoint: POST /signup
    public static class SignupRequest implements EndpointRequest<User> {
        public String username;
        public String email;
        public String passwordHash;

        @Override
        public boolean isValid() {
            // TODO: do validation
            return true;
        }

        @Override
        public User toServiceType() {
            return new User(email, username, passwordHash);
        }
    }

    // endpoint: POST /login
    public static class LoginRequest implements EndpointRequest<User> {
        public String username;
        public String email;
        public String passwordHash;

        @Override
        public boolean isValid() {
            // TODO: do validation
            return true;
        }

        @Override
        public User toServiceType() {
            return new User(email, username, passwordHash);
        }
    }
}
