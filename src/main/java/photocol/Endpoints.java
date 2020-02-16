/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import photocol.store.CollectionStore;
import photocol.store.PhotoStore;
import photocol.store.UserStore;
import spark.Request;
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
        Spark.post("/signup", (req, res) -> dummyHandler(req));
        Spark.post("/login", (req, res) -> dummyHandler(req));
        Spark.get("/logout", (req, res) -> dummyHandler(req));

        // get user/collection/image data
        Spark.get("/user/:username", (req, res) -> dummyHandler(req));
        Spark.get("/collection/:username/:collection", (req, res) -> dummyHandler(req));
        Spark.get("/images/:imageuri", (req, res) -> dummyHandler(req));

        // create/edit/delete user/collection/image data
        Spark.put("/collection/:collection", (req, res) -> dummyHandler(req));
        Spark.put("/collection/:collection/:image", (req, res) -> dummyHandler(req));
        Spark.post("/user/edit", (req, res) -> dummyHandler(req));
        Spark.post("/collection/:collection/edit", (req, res) -> dummyHandler(req));
        Spark.post("/collection/:collection/:image/edit", (req, res) -> dummyHandler(req));
    }

    // for testing only; will throw an exception if called
    private String dummyHandler(Request req) {
        throw new RuntimeException();
    }
}
