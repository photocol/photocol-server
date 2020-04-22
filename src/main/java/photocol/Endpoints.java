/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import com.google.gson.Gson;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.handler.CollectionHandler;
import photocol.layer.handler.PhotoHandler;
import photocol.layer.handler.SearchHandler;
import photocol.layer.handler.UserHandler;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.*;

public class Endpoints {

    public Endpoints(UserHandler userHandler, CollectionHandler collectionHandler, PhotoHandler photoHandler,
                     SearchHandler searchHandler, Gson gson) {

        path("/perma", () -> {
            // no CORS setup required here -- static resource
            before("/*", this::checkLoggedIn);
            get("/:photouri", photoHandler::permalink);
        });

        path("/user", () -> {
            before("/*", this::setupCors);
            before("/logout", this::checkLoggedIn);

            post("/signup", userHandler::signUp, gson::toJson);
            post("/login", userHandler::logIn, gson::toJson);
            get("/logout", userHandler::logOut, gson::toJson);
            get("/details", userHandler::userDetails, gson::toJson);
        });

        path("/photo", () -> {
            before("/*", this::setupCors);
            before("/*", this::checkLoggedIn);

            get("/currentuser", photoHandler::getUserPhotos, gson::toJson);
            path("/:photouri", () -> {
                put("", photoHandler::upload, gson::toJson);
                post("/update", photoHandler::update, gson::toJson);
                delete("", photoHandler::delete, gson::toJson);
            });
        });

        path("/collection", () -> {
            before("/*", this::setupCors);
            before("/*", this::checkLoggedIn);

            get("/currentuser", collectionHandler::getUserCollections, gson::toJson);
            post("/new", collectionHandler::createCollection, gson::toJson);
            path("/:username/:collectionuri", () -> {
                get("", collectionHandler::getCollection, gson::toJson);
                post("/update", collectionHandler::updateCollection, gson::toJson);
                post("/addphoto", collectionHandler::addRemovePhoto, gson::toJson);
                post("/removephoto", collectionHandler::addRemovePhoto, gson::toJson);
                post("/delete", collectionHandler::deleteCollection, gson::toJson);
            });
        });

        path("/search", () -> {
            before("/*", this::setupCors);

            get("/user/:userquery", searchHandler::searchUsers, gson::toJson);
        });

        // simple exception mapper: writes a simple JSON error message, with details if applicable
        exception(HttpMessageException.class, (exception, req, res) -> {
            res.status(exception.status());
            res.body("{\"error\":\"" + exception.error()
                    + (exception.details()!=null ? "\",\"details\":\"" + exception.details() + "\"}" : "\"}"));
        });
    }

    // authorization middleware
    private void checkLoggedIn(Request req, Response res) throws HttpMessageException {
        if(req.session().attribute("uid")==null)
            throw new HttpMessageException(401, HttpMessageException.Error.NOT_LOGGED_IN);
    }

    // CORS middleware
    private void setupCors(Request req, Response res) throws HttpMessageException {
        // FIXME: for now, only allowing requests from localhost
        // TODO: how to actually verify origin?
        String origin = req.headers("Origin");
        if(origin==null)
            throw new HttpMessageException(401, HttpMessageException.Error.UNAUTHORIZED_ORIGIN);
        res.header("Access-Control-Allow-Origin", origin);
        res.header("Access-Control-Allow-Credentials", "true");
        res.header("Vary", "Origin");

        // CORS requires a preflight request for certain requests (e.g., PUT) and set some options
        // so we set them here and sent an HTTP OK
        if(req.requestMethod().equals("OPTIONS")) {
            res.header("Access-Control-Allow-Headers", "Content-Type");
            res.header("Access-Control-Allow-Methods", "PUT, DELETE");
            Spark.halt(200);
        }

        // all cors endpoints send json response
        res.type("application/json");
    }
}
