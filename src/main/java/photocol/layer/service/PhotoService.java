package photocol.layer.service;

import photocol.definitions.Photo;
import photocol.definitions.response.StatusResponse;
import photocol.layer.store.PhotoStore;
import photocol.util.S3ConnectionClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;

import static photocol.definitions.response.StatusResponse.Status.*;

public class PhotoService {

    private PhotoStore photoStore;
    private S3ConnectionClient s3;
    public PhotoService(PhotoStore photoStore, S3ConnectionClient s3) {
        this.photoStore = photoStore;
        this.s3 = s3;
    }

    public StatusResponse<String> upload(String contentType, byte[] data, String imageuri, int uid) {
        // for a more exhaustive list, see: https://www.iana.org/assignments/media-types/media-types.xhtml#image
        // for now, only common ones allowed
        String ext;
        if(contentType==null)
            return new StatusResponse<>(STATUS_IMAGE_MIMETYPE_INVALID);
        switch(contentType) {
            case "image/jpg": case "image/jpeg": ext = "jpg"; break;
            case "image/gif": ext = "gif"; break;
            case "image/png": ext = "png"; break;
            default:
                return new StatusResponse<>(STATUS_IMAGE_MIMETYPE_INVALID);
        }

        // generate unique URI
        String randUri;
        do {
            randUri = String.valueOf(Math.random()).substring(2);
        } while(photoStore.checkIfPhotoExists(randUri).status()!=STATUS_IMAGE_NOT_FOUND);

        StatusResponse status = s3.putObject(data, randUri+"."+ext);
        if(status.status() != STATUS_OK) {
            s3.deleteObject(randUri+"."+ext);
            return status;
        }

        return photoStore.createImage(randUri+"."+ext, imageuri, uid);
    }

    // check image permissions and retrieve image
    public StatusResponse<ResponseInputStream<GetObjectResponse>> permalink(String uri, int uid) {
        if(photoStore.checkPhotoPermissions(uri, uid).status()!=STATUS_OK)
            return new StatusResponse<>(STATUS_IMAGE_NOT_FOUND);

        return s3.getObject(uri);
    }

    // get user photos
    public StatusResponse<List<Photo>> getUserPhotos(int uid) {
        return photoStore.getUserPhotos(uid);
    }
}
