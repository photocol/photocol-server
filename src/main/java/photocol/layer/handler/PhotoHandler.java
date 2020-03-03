package photocol.layer.handler;

import com.google.gson.Gson;
import photocol.definitions.Photo;
import photocol.definitions.response.StatusResponse;
import photocol.layer.service.PhotoService;
import photocol.util.S3ConnectionClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import spark.Request;
import spark.Response;

import java.util.List;

import static photocol.definitions.response.StatusResponse.Status.*;

public class PhotoHandler {

    private PhotoService photoService;
    private Gson gson;
    private S3ConnectionClient s3;
    public PhotoHandler(PhotoService photoService, Gson gson, S3ConnectionClient s3) {
        this.photoService = photoService;
        this.gson = gson;
        this.s3 = s3;
    }

    // simple image passthrough from s3
    // todo: move this to service layer, add authentication
    public Object permalink(Request req, Response res) {
        String uri = req.params("imageuri");
        String conditionalHeader = req.headers("If-None-Match");
        String eTag;
        Integer uid = req.session().attribute("uid");
        if(uid==null) {
            res.status(404);
            return gson.toJson(new StatusResponse<>(STATUS_IMAGE_NOT_FOUND));
        }

        StatusResponse<ResponseInputStream<GetObjectResponse>> status;
        if((status=photoService.permalink(uri, uid)).status()!=STATUS_OK) {
            res.status(404);
            return gson.toJson(new StatusResponse<>(STATUS_IMAGE_NOT_FOUND));
        }

        // caching with etags
        ResponseInputStream<GetObjectResponse> response = status.payload();
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

    // handle put request of uploading image
    public StatusResponse<String> upload(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            return new StatusResponse<>(STATUS_NOT_LOGGED_IN);

        // get file data
        return photoService.upload(req.contentType(), req.bodyAsBytes(), req.params("imageuri"), uid);
    }

    // show all photos owned by user
    public StatusResponse<List<Photo>> getUserPhotos(Request req, Response res) {
        res.type("application/json");

        // make sure logged in
        Integer uid = req.session().attribute("uid");
        if(uid==null)
            return new StatusResponse<>(STATUS_NOT_LOGGED_IN);

        // get all photos
        return photoService.getUserPhotos(uid);
    }

    // update image attributes
    public StatusResponse update(Request req, Response res) {
        res.type("application/json");

        // TODO: @tiffany implement this in service layer
        // photoService.updateImage();

        // TODO: working here
        return null;
    }
}
