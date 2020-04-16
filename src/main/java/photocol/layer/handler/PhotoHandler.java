package photocol.layer.handler;

import com.google.gson.Gson;
import photocol.definitions.Photo;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.service.PhotoService;
import photocol.util.S3ConnectionClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import spark.Request;
import spark.Response;

import java.util.List;

public class PhotoHandler {

    private PhotoService photoService;
    private Gson gson;
    private S3ConnectionClient s3;
    public PhotoHandler(PhotoService photoService, Gson gson, S3ConnectionClient s3) {
        this.photoService = photoService;
        this.gson = gson;
        this.s3 = s3;
    }

    /**
     * Image passthrough from S3. If an ETag is specified and it matches the one from S3, a 304 (cached) response
     * is given.
     * @param req   spark request object
     * @param res   spark response object
     * @return      image stream on success (may return early with 304 if cache success)
     * @throws HttpMessageException on failure
     */
    public Object permalink(Request req, Response res) throws HttpMessageException {
        String uri = req.params("photouri");
        String conditionalHeader = req.headers("If-None-Match");
        String eTag;

        int uid = req.session().attribute("uid");

        // TODO: remove
//        StatusResponse<ResponseInputStream<GetObjectResponse>> status;
//        if((status=photoService.permalink(uri, uid)).status()!=STATUS_OK) {
//            res.status(404);
//            return gson.toJson(new StatusResponse<>(STATUS_IMAGE_NOT_FOUND));
//        }
        ResponseInputStream<GetObjectResponse> response = photoService.permalink(uri, uid);

        // caching with etags
        // TODO: remove
//        ResponseInputStream<GetObjectResponse> response = status.payload();
        eTag = response.response().eTag();
        if (conditionalHeader != null && conditionalHeader.equals(eTag)) {
            res.status(304);
            return "";
        }
        res.type(response.response().contentType());
        res.header("Cache-Control", "public, max-age=3600");
        res.header("ETag", response.response().eTag());

        // return response stream
        return response;
    }

    /**
     * Handle put request of uploading image
     * @param req   spark request object
     * @param res   spark response object
     * @return      newly uploaded photo uri
     * @throws HttpMessageException on failure
     */
    public String upload(Request req, Response res) throws HttpMessageException {
        int uid = req.session().attribute("uid");
        return photoService.upload(req.contentType(), req.bodyAsBytes(), req.params("photouri"), uid);
    }

    /**
     * Show all photos owned by user
     * @param req   spark request object
     * @param res   spark response object
     * @return      list of photo objects on success
     * @throws HttpMessageException on failure
     */
    public List<Photo> getUserPhotos(Request req, Response res) throws HttpMessageException {
        int uid = req.session().attribute("uid");
        return photoService.getUserPhotos(uid);
    }

    /**
     * Update image attributes
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean update(Request req, Response res) throws HttpMessageException {
        // TODO: working here
        // photoService.updateImage();
        return false;
    }
}
