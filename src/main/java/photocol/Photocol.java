/* Main class for the Photocol app */
package photocol;

import com.google.gson.Gson;
import photocol.layer.handler.*;
import photocol.layer.service.*;
import photocol.layer.store.*;
import photocol.util.*;

public class Photocol {
    public static void main(String[] args) {
        // services
        Gson gson = new Gson();
        S3ConnectionClient s3 = new S3ConnectionClient();
        DBConnectionClient dbClient = new DBConnectionClient();

        // initialize layers
        UserStore userStore = new UserStore(dbClient);
        CollectionStore collectionStore = new CollectionStore(dbClient);
        PhotoStore photoStore = new PhotoStore(dbClient);

        UserService userService = new UserService(userStore);
        CollectionService collectionService = new CollectionService(collectionStore, photoStore, userStore);
        PhotoService photoService = new PhotoService(photoStore, s3);

        UserHandler userHandler = new UserHandler(userService, gson);
        CollectionHandler collectionHandler = new CollectionHandler(collectionService, gson);
        PhotoHandler photoHandler = new PhotoHandler(photoService, gson, s3);

        SearchHandler searchHandler = new SearchHandler(userStore);

        // init handlers (highest layer) at all endpoints
        new Endpoints(userHandler, collectionHandler, photoHandler, searchHandler, gson);
    }
}
