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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

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

        Integer uid = req.session().attribute("uid");
        if(uid==null)
            uid = -1;

        ResponseInputStream<GetObjectResponse> response = photoService.permalink(uri, uid);

        // caching with etags
        eTag = response.response().eTag();
        if (conditionalHeader != null && conditionalHeader.equals(eTag)) {
            try {
                response.close();
            } catch (IOException err) {
                err.printStackTrace();
                throw new HttpMessageException(500, HttpMessageException.Error.S3_ERROR);
            }
            res.status(304);
            return "";
        }
        res.type(response.response().contentType());
        res.header("Cache-Control", "private, must-revalidate");
        res.header("ETag", response.response().eTag());

        // must close s3 stream for it to work properly
        // guidance from: https://stackoverflow.com/questions/33398405/stream-a-video-file-over-http-with-spark-java
        try(OutputStream os = res.raw().getOutputStream()){
            response.transferTo(os);
            response.close();
        } catch(IOException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, HttpMessageException.Error.S3_ERROR);
        }
        return null;
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
        return false;
    }

    /**
     * Get photo details
     * @param req   spark request object
     * @param res   spark response object
     * @return      photo object with details
     * @throws HttpMessageException on error
     */
    public Photo details(Request req, Response res) throws HttpMessageException {
        String photouri = req.params("photouri");
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            uid=-1;

        return this.photoService.details(photouri, uid);
    }

    /**
     * Delete image from account
     * (For removing an image from a collection, see CollectionHandler::addRemovePhoto)
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean delete(Request req, Response res) throws HttpMessageException {
        return photoService.deletePhoto(req.params("photouri"), req.session().attribute("uid"));
    }
}
