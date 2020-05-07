package photocol.layer.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import photocol.definitions.Photo;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.PhotoStore;
import photocol.util.S3ConnectionClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
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
     * @param filename      original filename
     * @param uid           image owner
     * @return              auto-generated uid of uploaded image
     * @throws HttpMessageException on failure
     */
    public String upload(String contentType, byte[] data, String filename, int uid)
            throws HttpMessageException {

        // generate unique (16-character fixed-length) URI
        String randUri;
        do {
            randUri = ("00000000000" + String.valueOf(Math.random()).replace(".", ""));
            randUri = randUri.substring(randUri.length()-16);
        } while(photoStore.checkIfPhotoExists(randUri));

        // content-type is not used; instead, extract mimetype
        String mimeType;
        String extension;
        Photo.PhotoMetadata photoMetadata = new Photo.PhotoMetadata();

        // extract select metadata
        // library: https://github.com/drewnoakes/metadata-extractor
        // TODO: this cannot process some formats, e.g., SVG
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(data));

            // extract mimetype, extension
            FileTypeDirectory fileTypeDirectory = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
            photoMetadata.mimeType = fileTypeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE);
            extension = fileTypeDirectory.getString(FileTypeDirectory.TAG_EXPECTED_FILE_NAME_EXTENSION);

            // extract image width and height
            for(Directory directory : metadata.getDirectories()) {
                for(Tag tag : directory.getTags()) {
                    if(tag.getTagName().equals("Image Width"))
                        photoMetadata.width = directory.getInt(tag.getTagType());
                    if(tag.getTagName().equals("Image Height"))
                        photoMetadata.height = directory.getInt(tag.getTagType());
                }
            }
        } catch(IOException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, INPUT_FORMAT_ERROR, "IOEXCEPTION_READING_IMAGE");
        } catch(ImageProcessingException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, INPUT_FORMAT_ERROR, "IMAGE_FORMAT_ERROR");
        } catch (MetadataException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, INPUT_FORMAT_ERROR, "METADATA_FETCH_ERROR");
        }

        String newUri = randUri + "." + extension;
        s3.putObject(data, newUri);

        photoStore.createPhoto(new Photo(newUri, filename, "", new Date(), photoMetadata), uid);
        return newUri;
    }

    /**
     * Gets image stream from permalink
     * @param uri   image uri (from permalink)
     * @param uid   viewer uid
     * @return      ResponseInputStream GetObjectResponse of image
     * @throws HttpMessageException on failure
     */
    public ResponseInputStream<GetObjectResponse> permalink(String uri, int uid)
            throws HttpMessageException {
        photoStore.checkPhotoPermissions(uri, uid, false);
        return s3.getObject(uri);
    }

    /**
     * Get user photos (simple passthrough)
     * @param uid   uid of user to get photos of
     * @return      list of photo objects
     * @throws HttpMessageException on failure
     */
    public List<Photo> getUserPhotos(int uid) throws HttpMessageException {
        return photoStore.getUserPhotos(uid);
    }

    /**
     * Delete a photo from account
     * @param uri   photo uri
     * @param uid   uid of owner
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean deletePhoto(String uri, int uid) throws HttpMessageException {
        // get pid and checks if user is owner of photo
        int pid = photoStore.checkPhotoPermissions(uri, uid, true);

        // remove from image and icj tables (fails if not owner)
        photoStore.deletePhoto(pid);

        // delete from S3
        s3.deleteObject(uri);

        return true;
    }
}
