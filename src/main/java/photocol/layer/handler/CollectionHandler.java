package photocol.layer.handler;

import com.google.gson.Gson;
import photocol.layer.service.CollectionService;

public class CollectionHandler {

    private CollectionService collectionService;
    private Gson gson;
    public CollectionHandler(CollectionService collectionService, Gson gson) {
        this.collectionService = collectionService;
        this.gson = gson;
    }

}
