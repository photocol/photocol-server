package photocol.layer.handler;

import com.google.gson.Gson;
import static photocol.definitions.request.EndpointRequestModel.*;

import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.response.StatusResponse;
import photocol.layer.service.CollectionService;
import spark.Request;
import spark.Response;

import java.util.List;

import static photocol.definitions.response.StatusResponse.Status.*;

public class CollectionHandler {

    private CollectionService collectionService;
    private Gson gson;
    public CollectionHandler(CollectionService collectionService, Gson gson) {
        this.collectionService = collectionService;
        this.gson = gson;
    }

    // list the current user's collections
    public StatusResponse getUserCollections(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        return collectionService.getUserCollections(uid);
    }

    // create a collection
    public StatusResponse createCollection(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        NewCollectionRequest collection = gson.fromJson(req.body(), NewCollectionRequest.class);
        if(collection==null || !collection.isValid()) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }

        return collectionService.createCollection(uid, collection.toServiceType());
    }

    // update a collection
    public StatusResponse updateCollection(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        UpdateCollectionRequest collectionRequest = gson.fromJson(req.body(), UpdateCollectionRequest.class);
        if(collectionRequest==null || !collectionRequest.isValid()) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }

        String collectionUri = req.params("collectionuri");
        if(collectionUri==null) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }

        return collectionService.update(uid, collectionUri, collectionRequest.toServiceType());
    }

    // list images in a collection
    public StatusResponse<List<Photo>> getCollection(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            return new StatusResponse<>(STATUS_NOT_LOGGED_IN);

        String collectionUri = req.params("collectionuri");
        if(collectionUri==null) {
            res.status(400);
            return new StatusResponse<>(STATUS_HTTP_ERROR);
        }

        return collectionService.getCollection(uid, collectionUri);
    }

    // add image to collection
    public StatusResponse addPhoto(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        String collectionUri = req.params("collectionuri");
        if(collectionUri==null) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }

        AddImageRequest addRequest = gson.fromJson(req.body(), AddImageRequest.class);
        if(addRequest==null || !addRequest.isValid()) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }

        String imageUri = addRequest.toServiceType();
        return collectionService.addImage(uid, collectionUri, imageUri);
    }

}
