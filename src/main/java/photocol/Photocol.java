/* Main class for the Photocol app */

package photocol;

import com.google.gson.Gson;
import photocol.layer.store.UserStore;
import photocol.layer.handler.CollectionHandler;
import photocol.layer.handler.PhotoHandler;
import photocol.layer.handler.UserHandler;
import photocol.layer.service.CollectionService;
import photocol.layer.service.PhotoService;
import photocol.layer.service.UserService;
import photocol.layer.store.CollectionStore;
import photocol.layer.store.PhotoStore;
import photocol.util.S3ConnectionClient;

public class Photocol {
    public static void main(String[] args) {

        // services
        Gson gson = new Gson();
        S3ConnectionClient s3 = new S3ConnectionClient();

        // layers of handling
        UserStore userStore = new UserStore();
        UserService userService = new UserService(userStore);
        UserHandler userHandler = new UserHandler(userService, gson);

        CollectionStore collectionStore = new CollectionStore();
        CollectionService collectionService = new CollectionService(collectionStore);
        CollectionHandler collectionHandler = new CollectionHandler(collectionService, gson);

        PhotoStore photoStore = new PhotoStore();
        PhotoService photoService = new PhotoService(photoStore, s3);
        PhotoHandler photoHandler = new PhotoHandler(photoService, gson, s3);

        // init handlers (highest layer) at all endpoints
        new Endpoints(userHandler, collectionHandler, photoHandler, gson);
    }

    }
}
