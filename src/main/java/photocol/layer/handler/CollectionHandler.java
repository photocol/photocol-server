package photocol.layer.handler;

import com.google.gson.Gson;
import static photocol.definitions.request.EndpointRequestModel.NewCollectionRequest;

import photocol.definitions.Photo;
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

    // create a collection
    public StatusResponse createCollection(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("user");
        if(uid==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        NewCollectionRequest collection = gson.fromJson(req.body(), NewCollectionRequest.class);
        if(collection==null || !collection.isValid()) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }

        return collectionService.createCollection(uid, collection.toServiceType());
    }

    // list images in a collection
    public StatusResponse<List<Photo>> getCollection(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("user");
        if(uid==null)
            return new StatusResponse<>(STATUS_NOT_LOGGED_IN);

        String collectionName = req.params("collectionname");
        if(collectionName==null) {
            res.status(400);
            return new StatusResponse<>(STATUS_HTTP_ERROR);
        }

        return collectionService.getCollection(uid, collectionName);
    }

}
