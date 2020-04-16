package photocol.layer.service;

import photocol.definitions.Photo;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.PhotoStore;
import photocol.util.S3ConnectionClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class PhotoService {

    private PhotoStore photoStore;
    private S3ConnectionClient s3;
    public PhotoService(PhotoStore photoStore, S3ConnectionClient s3) {
        this.photoStore = photoStore;
        this.s3 = s3;
    }

    /**
     * Upload an image to S3
     * @param contentType   content type of image
     * @param data          image data as a byte array
     * @param imageuri      original filename
     * @param uid           image owner
     * @return              auto-generated uid of uploaded image
     * @throws HttpMessageException on failure
     */
    public String upload(String contentType, byte[] data, String imageuri, int uid)
            throws HttpMessageException {
        // for a more exhaustive list, see: https://www.iana.org/assignments/media-types/media-types.xhtml#image
        // for now, only common ones allowed
        // TODO: make this more inclusive and robust
        // TODO: in the future, verify image sizes and formats
        String ext;
        if(contentType==null)
            throw new HttpMessageException(400, IMAGE_MIMETYPE_INVALID);
        switch(contentType) {
            case "image/jpg": case "image/jpeg": ext = "jpg"; break;
            case "image/gif": ext = "gif"; break;
            case "image/png": ext = "png"; break;
            default:
                throw new HttpMessageException(400, IMAGE_MIMETYPE_INVALID);
        }

        // generate unique URI
        // TODO: make this more robust (and fixed-length)
        String randUri;
        do {
            randUri = String.valueOf(Math.random()).substring(2);
        } while(photoStore.checkIfPhotoExists(randUri));

        // TODO: remove
//        StatusResponse status = s3.putObject(data, randUri+"."+ext);
//        if(status.status() != STATUS_OK) {
//            s3.deleteObject(randUri+"."+ext);
//            return status;
//        }
        String newUri = randUri + "." + ext;
        s3.putObject(data, newUri);
        photoStore.createImage(newUri, imageuri, uid);

        return newUri;
    }

    /**
     * Gets image stream from permalink
     * @param uri   image uri (from permalink)
     * @param uid   viewer uid
     * @return      ResponseInputStream<GetObjectResponse> of image
     * @throws HttpMessageException on failure
     */
    public ResponseInputStream<GetObjectResponse> permalink(String uri, int uid)
            throws HttpMessageException {
        // TODO: remove
//        if(photoStore.checkPhotoPermissions(uri, uid).status()!=STATUS_OK)
//            return new StatusResponse<>(STATUS_IMAGE_NOT_FOUND);
        photoStore.checkPhotoPermissions(uri, uid);
        return s3.getObject(uri);
    }

    /**
     * Get user photos (simple passthrough).
     * @param uid   uid of user to get photos of
     * @return      list of photo objects
     * @throws HttpMessageException
     */
    public List<Photo> getUserPhotos(int uid) throws HttpMessageException {
        return photoStore.getUserPhotos(uid);
    }
}
