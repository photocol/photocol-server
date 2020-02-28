package photocol.layer.handler;

import com.google.gson.Gson;
import photocol.layer.service.PhotoService;

public class PhotoHandler {

    private PhotoService photoService;
    private Gson gson;
    public PhotoHandler(PhotoService photoService, Gson gson) {
        this.photoService = photoService;
        this.gson = gson;
    }
}
