package photocol.definitions.request;

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

    // endpoint: POST /user/signup
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

    // endpoint: POST /user/login
    public static class LoginRequest implements EndpointRequest<User> {
        public String username;
        public String passwordHash;

        @Override
        public boolean isValid() {
            // TODO: improve validation
            return this.username!=null && this.passwordHash!=null;
        }

        @Override
        public User toServiceType() {
            return new User(username, passwordHash);
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

    // endpoints: POST /collection/:collectioname/addphoto,
    //            POST /collection/:collectionname/removephoto
    public static class PhotoUriRequest implements EndpointRequest<String> {
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
