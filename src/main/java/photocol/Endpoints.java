/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import com.google.gson.Gson;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.handler.CollectionHandler;
import photocol.layer.handler.PhotoHandler;
import photocol.layer.handler.UserHandler;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.*;

public class Endpoints {

    public Endpoints(UserHandler userHandler, CollectionHandler collectionHandler, PhotoHandler photoHandler,
                     Gson gson) {

        path("/perma", () -> {
            before(this::checkLoggedIn);
            get("/:photouri", photoHandler::permalink);
        });

        path("/user", () -> {
            before(this::setupCors);
            before("/login", this::checkLoggedIn);

            post("/signup", userHandler::signUp, gson::toJson);
            post("/login", userHandler::logIn, gson::toJson);
            get("/logout", userHandler::logOut, gson::toJson);
            get("/details", userHandler::userDetails, gson::toJson);
        });

        path("/photo", () -> {
            before(this::setupCors);
            before(this::checkLoggedIn);

            get("/currentuser", photoHandler::getUserPhotos, gson::toJson);
            path("/:photouri", () -> {
                put("/upload", photoHandler::upload, gson::toJson);
                post("/update", photoHandler::update, gson::toJson);
            });
        });

        path("/collection", () -> {
            before(this::setupCors);
            before(this::checkLoggedIn);

            get("/currentuser", collectionHandler::getUserCollections, gson::toJson);
            post("/new", collectionHandler::createCollection, gson::toJson);
            path("/:username/:collectionuri", () -> {
                get("/", collectionHandler::getCollection, gson::toJson);
                post("/update", collectionHandler::updateCollection, gson::toJson);
                post("/addphoto", collectionHandler::addPhoto, gson::toJson);
            });
        });

        // simple exception mapper
        exception(HttpMessageException.class, (exception, req, res) -> {
            res.status(exception.status());
            res.body(exception.message());
        });
    }

    // authorization middleware
    private void checkLoggedIn(Request req, Response res) throws HttpMessageException {
        if(req.session().attribute("uid")==null)
            throw new HttpMessageException(401, "Not signed in");
    }

    // CORS middleware
    private void setupCors(Request req, Response res) throws HttpMessageException {
        // passthrough on image endpoint
        if(req.uri().startsWith("/photo/") && req.requestMethod().equals("GET"))
            return;

        // FIXME: for now, only allowing requests from localhost
        // TODO: how to actually verify origin?
        String origin = req.headers("Origin");
        if(origin==null)
            throw new HttpMessageException(401, "Unauthorized origin header value");
        res.header("Access-Control-Allow-Origin", origin);
        res.header("Access-Control-Allow-Credentials", "true");
        res.header("Vary", "Origin");

        // CORS requires a preflight request for certain requests (e.g., PUT) and set some options
        // so we set them here and sent an HTTP OK
        if(req.requestMethod().equals("OPTIONS")) {
            res.header("Access-Control-Allow-Headers", "content-type");
            res.header("Access-Control-Allow-Methods", "PUT");
            Spark.halt(200);
        }
    }
}
