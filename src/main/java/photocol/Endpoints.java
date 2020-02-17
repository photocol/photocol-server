/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import photocol.store.CollectionStore;
import photocol.store.PhotoStore;
import photocol.store.UserStore;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Endpoints {

    private UserStore us;
    private PhotoStore ps;
    private CollectionStore cs;

    public Endpoints(UserStore us, PhotoStore ps, CollectionStore cs) {
        this.us = us;
        this.ps = ps;
        this.cs = cs;

        // login endpoints
        Spark.post("/signup", us::signUp);
        Spark.post("/login", us::logIn);
        Spark.get("/logout", us::logOut);
        Spark.get("/userdetails", us::getLoggedInUser);

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
