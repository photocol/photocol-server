package photocol.layer.handler;

import com.google.gson.Gson;
import static photocol.definitions.request.EndpointRequestModel.*;

import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.service.CollectionService;
import spark.Request;
import spark.Response;

import java.util.List;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class CollectionHandler {

    private CollectionService collectionService;
    private Gson gson;
    public CollectionHandler(CollectionService collectionService, Gson gson) {
        this.collectionService = collectionService;
        this.gson = gson;
    }

    /**
     * List the current user's accesible collections (i.e., explicitly in the ACL list of)
     * @param req   spark request object
     * @param res   spark response object
     * @return      list of collections on success
     * @throws HttpMessageException on failure
     */
    public List<PhotoCollection> getUserCollections(Request req, Response res) throws HttpMessageException {
        int uid = req.session().attribute("uid");
        String username = req.session().attribute("username");
        return collectionService.getUserCollections(uid, username);
    }

    /**
     * Create a collection
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean createCollection(Request req, Response res) throws HttpMessageException {
        int uid = req.session().attribute("uid");

        NewCollectionRequest collection = gson.fromJson(req.body(), NewCollectionRequest.class);
        if(collection==null || !collection.isValid())
            throw new HttpMessageException(400, INPUT_FORMAT_ERROR);

        return collectionService.createCollection(uid, collection.toServiceType());
    }

    /**
     * Update a collection
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean updateCollection(Request req, Response res) throws HttpMessageException  {
        int uid = req.session().attribute("uid");

        UpdateCollectionRequest collectionRequest = gson.fromJson(req.body(), UpdateCollectionRequest.class);
        String collectionUri = req.params("collectionuri");
        String collectionOwner = req.params("username");
        if(collectionRequest==null || !collectionRequest.isValid()
            || collectionUri==null || collectionOwner==null)
            throw new HttpMessageException(400, INPUT_FORMAT_ERROR);

        return collectionService.update(uid, collectionUri, collectionOwner, collectionRequest.toServiceType());
    }

    /**
     * List photos in a collection
     * @param req   spark request object
     * @param res   spark response object
     * @return      photocollection object, including list of photo objects
     * @throws HttpMessageException on failure
     */
    public PhotoCollection getCollection(Request req, Response res) throws HttpMessageException {
        int uid = req.session().attribute("uid");

        String collectionUri = req.params("collectionuri");
        String collectionOwner = req.params("username");
        if(collectionUri==null || collectionOwner==null)
            throw new HttpMessageException(400, INPUT_FORMAT_ERROR);

        return collectionService.getCollection(uid, collectionUri, collectionOwner);
    }

    /**
     * Add or remove photo to/from collection
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean addRemovePhoto(Request req, Response res) throws HttpMessageException {
        int uid = req.session().attribute("uid");

        String[] uriComponents = req.uri().split("/");
        boolean isAdd = uriComponents[uriComponents.length-1].toLowerCase().equals("addphoto");

        PhotoUriRequest addRequest = gson.fromJson(req.body(), PhotoUriRequest.class);
        String collectionUri = req.params("collectionuri");
        String collectionOwner = req.params("username");
        if(collectionUri==null || collectionOwner==null || addRequest==null || !addRequest.isValid())
            throw new HttpMessageException(400, INPUT_FORMAT_ERROR);

        String photoUri = addRequest.toServiceType();
        return collectionService.addRemovePhoto(uid, collectionUri, collectionOwner, photoUri, isAdd);
    }

    /**
     * Delete a collection and all images within it
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean deleteCollection(Request req, Response res) throws HttpMessageException {
        // TODO: possible that username session variable is wrong
        //       maybe implement Redis for sessions later
        String username = req.session().attribute("username");
        int uid = req.session().attribute("uid");

        String collectionUri = req.params("collectionuri");
        String collectionOwner = req.params("username");

        // make sure collection owner is self
        if(!collectionOwner.equals(username))
            throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS, "NOT COLLECTION OWNER");

        return collectionService.deleteCollection(uid, collectionUri);
    }

}
