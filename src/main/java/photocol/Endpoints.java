/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import photocol.layer.store.CollectionStore;
import photocol.layer.store.PhotoStore;
import photocol.layer.store.UserStore;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Endpoints {

    private UserStore userStore;
    private PhotoStore photoStore;
    private CollectionStore collectionStore;

    public Endpoints(UserStore userStore, PhotoStore photoStore, CollectionStore collectionStore) {
        this.userStore = userStore;
        this.photoStore = photoStore;
        this.collectionStore = collectionStore;

        // login endpoints
        Spark.post("/signup", userStore::signUp);
        Spark.post("/login", userStore::logIn);
        Spark.get("/logout", userStore::logOut);
        Spark.get("/userdetails", userStore::getLoggedInUser);

        // get user/collection/image data
        Spark.get("/user/:username", this::dummyHandler);
        Spark.get("/collection/:username/:collection", this::dummyHandler);
        Spark.get("/images/:imageuri", this::dummyHandler);

        // create/edit/delete user/collection/image data
        Spark.put("/collection/:collection", this::dummyHandler);
        Spark.put("/collection/:collection/:image", this::dummyHandler);
        Spark.post("/user/edit", this::dummyHandler);
        Spark.post("/collection/:collection/edit", this::dummyHandler);
        Spark.post("/collection/:collection/:image/edit", this::dummyHandler);

        // catch-all; 404
        Spark.get("/*", this::handle404);
        Spark.post("/*", this::handle404);
        Spark.put("/*", this::handle404);
        Spark.delete("/*", this::handle404);
    }

    private String handle404(Request req, Response res) {
        res.status(404);
        return "";
    }

    // for testing only; will throw an exception if called
    private String dummyHandler(Request req, Response res) {
        throw new RuntimeException();
    }
}
