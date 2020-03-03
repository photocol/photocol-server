package photocol.layer.handler;

import com.google.gson.Gson;
import photocol.definitions.response.StatusResponse;
import photocol.layer.service.CollectionService;
import spark.Request;
import spark.Response;

import static photocol.definitions.response.StatusResponse.Status.*;

public class CollectionHandler {

    private CollectionService collectionService;
    private Gson gson;
    public CollectionHandler(CollectionService collectionService, Gson gson) {
        this.collectionService = collectionService;
        this.gson = gson;
    }

    public StatusResponse createCollection(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("user");
        if(uid==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        return null;
    }

}
