package photocol.definitions.request;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import photocol.definitions.ACLEntry;
import photocol.definitions.PhotoCollection;
import photocol.definitions.User;

import java.util.List;

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
            // TODO: improve validation
            return this.username!=null && this.email!=null && this.passwordHash!=null;
        }

        @Override
        public User toServiceType() {
            return new User(email, username, passwordHash);
        }
    }

    // endpoint: POST /login
    public static class LoginRequest implements EndpointRequest<User> {
        public String email;
        public String passwordHash;

        @Override
        public boolean isValid() {
            // TODO: improve validation
            return this.email!=null && this.passwordHash!=null;
        }

        @Override
        public User toServiceType() {
            return new User(email, passwordHash);
        }
    }

    // endpoint: POST /collection/:collectionname/new
    public static class NewCollectionRequest implements EndpointRequest<PhotoCollection> {

        public boolean isPublic;
        public String name;

        @Override
        public boolean isValid() {
            return name!=null;
        }

        @Override
        public PhotoCollection toServiceType() {
            return new PhotoCollection(isPublic, name);
        }
    }

    // endpoint: POST /collection/:collectionname/update
    public static class UpdateCollectionRequest implements EndpointRequest<PhotoCollection> {
        public boolean isPublic;
        public String name;
        public List<ACLEntry> aclList;

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public PhotoCollection toServiceType() {
            return new PhotoCollection(isPublic, name, aclList);
        }
    }

    // endpoint: POST /collection/:collectioname/addimage
    public static class AddImageRequest implements EndpointRequest<String> {
        public String uri;

        @Override
        public boolean isValid() {
            return uri!=null;
        }

        @Override
        public String toServiceType() {
            return uri;
        }
    }
}
