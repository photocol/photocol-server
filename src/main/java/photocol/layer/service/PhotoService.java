package photocol.layer.service;

import photocol.definitions.response.StatusResponse;
import photocol.layer.store.PhotoStore;
import photocol.util.S3ConnectionClient;

import static photocol.definitions.response.StatusResponse.Status.*;

public class PhotoService {

    private PhotoStore photoStore;
    private S3ConnectionClient s3;
    public PhotoService(PhotoStore photoStore, S3ConnectionClient s3) {
        this.photoStore = photoStore;
        this.s3 = s3;
    }

    public StatusResponse<String> upload(String contentType, byte[] data, int uid) {

        // for a more exhaustive list, see: https://www.iana.org/assignments/media-types/media-types.xhtml#image
        // for now, only common ones allowed
        String ext;
        switch(contentType) {
            case "image/jpeg": ext = "jpg"; break;
            case "image/gif": ext = "gif"; break;
            case "image/png": ext = "png"; break;
            default:
                return new StatusResponse<>(STATUS_IMAGE_MIMETYPE_INVALID);
        }

        // generate unique URI
        String randUri;
        do {
            randUri = String.valueOf(Math.random()).substring(2);
        } while(photoStore.checkIfPhotoExists(randUri).status()==STATUS_IMAGE_NOT_FOUND);

        StatusResponse status = s3.putObject(data, randUri+"."+ext);
        if(status.status() != STATUS_OK) {
            s3.deleteObject(randUri+"."+ext);
            return status;
        }
        return photoStore.createImage(randUri+"."+ext, uid);
    }
}
