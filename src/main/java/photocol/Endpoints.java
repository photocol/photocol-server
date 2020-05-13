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

        // purely for getting images and collections, may not require login
        path("/perma", () -> {
            // no CORS setup required for static resource (image)
            // login not required for pictures and collections (i.e., public)
            path("/:photouri", () -> {
                before("/details", this::setupCors);

                get("", photoHandler::permalink);
                get("/download/:downloadfilename", photoHandler::permalink);
                get("/details", photoHandler::details, gson::toJson);
            });

            path("/collection/:username/:collectionuri", () -> {
                before("", this::setupCors);
                get("", collectionHandler::getCollection, gson::toJson);
            });
        });

        // for user data/settings, most don't require login
        path("/user", () -> {
            before("/*", this::setupCors);
            before("/logout", this::checkLoggedIn);
            before("/update", this::checkLoggedIn);

            post("/signup", userHandler::signUp, gson::toJson);
            post("/login", userHandler::logIn, gson::toJson);
            get("/logout", userHandler::logOut, gson::toJson);
            get("/details", userHandler::userDetails, gson::toJson);
            post("/update", userHandler::update, gson::toJson);
            path("/profile", () -> {
                get("", userHandler::getProfile, gson::toJson);
                get("/:username", userHandler::getProfile, gson::toJson);
            });
        });

        // for changing photo information, requires login
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

        // for changing collection information, requires login
        path("/collection", () -> {
            before("/*", this::setupCors);
            before("/*", this::checkLoggedIn);

            get("/currentuser", collectionHandler::getUserCollections, gson::toJson);
            post("/new", collectionHandler::createCollection, gson::toJson);
            path("/:username/:collectionuri", () -> {
                post("/update", collectionHandler::updateCollection, gson::toJson);
                post("/addphoto", collectionHandler::addRemovePhoto, gson::toJson);
                post("/removephoto", collectionHandler::addRemovePhoto, gson::toJson);
                post("/delete", collectionHandler::deleteCollection, gson::toJson);
            });
        });

        // search paths, may not require login
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
        // catch all other errors
        exception(Exception.class, (exception, req, res) -> {
            exception.printStackTrace();
            res.status(500);
            res.body("{\"error\":\"INTERNAL_SERVER_ERROR\"}");
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
