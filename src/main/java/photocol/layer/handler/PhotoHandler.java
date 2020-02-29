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
    public Object permalink(Request req, Response res) {
        String imageName = req.params("imageuri");
        ResponseInputStream<GetObjectResponse> response = s3.getObject(imageName);
        res.type(response.response().contentType());
        return response;
    }
}
