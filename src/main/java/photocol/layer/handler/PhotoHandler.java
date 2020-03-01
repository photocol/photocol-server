package photocol.layer.handler;

import com.google.gson.Gson;
import photocol.layer.service.PhotoService;
import photocol.util.S3ConnectionClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import spark.Request;
import spark.Response;

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
        String imageName = req.params("imageuri");
        String conditionalHeader = req.headers("If-None-Match");
        String eTag;

        ResponseInputStream<GetObjectResponse> response = s3.getObject(imageName);

        // caching with etags
        eTag = response.response().eTag();
        if (conditionalHeader != null && conditionalHeader.equals(eTag)) {
            res.status(304);
            return 0;
        }
        res.type(response.response().contentType());
        res.header("Cache-Control", "public, max-age=3600");
        res.header("ETag", response.response().eTag());

        // return response stream
        return response;
    }
}
