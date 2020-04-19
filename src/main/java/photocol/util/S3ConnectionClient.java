package photocol.util;

import photocol.definitions.exception.HttpMessageException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static photocol.definitions.exception.HttpMessageException.Error.*;

// based on example code from
// https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/s3/src/main/java/com/example/s3
// sdk javadocs: https://sdk.amazonaws.com/java/api/latest/
// need to have authentication environment variables set
public class S3ConnectionClient {

    private S3Client s3;
    private final String bucket = "photocol";
    public S3ConnectionClient() {
        try {
            s3 = S3Client.builder().region(Region.US_EAST_1).build();
        } catch (S3Exception exception) {
            System.err.println("Couldn't connect to S3 client.");
            exception.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get image as stream from server
     * @param key   image uri
     * @return      ResponseInputStream GetObjectResponse object of image
     * @throws HttpMessageException on S3 error
     */
    public ResponseInputStream<GetObjectResponse> getObject(String key) throws HttpMessageException {
        try {
            return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
        } catch(S3Exception exception) {
            exception.printStackTrace();
            throw new HttpMessageException(500, S3_ERROR);
        }
    }

    /**
     * Upload image to s3
     * @param data  byte stream of image data
     * @param uri   (unique) uri of image
     * @return      true on success
     * @throws HttpMessageException on S3 error
     */
    public boolean putObject(byte[] data, String uri) throws HttpMessageException {
        try {
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(uri).build(),
                    RequestBody.fromBytes(data));

            return true;
        } catch(S3Exception err) {
            err.printStackTrace();
            throw new HttpMessageException(500, S3_ERROR);
        }
    }

    /**
     * Delete image from s3
     * @param uri   photouri of image
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean deleteObject(String uri) throws HttpMessageException {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(uri).build());

            return true;
        } catch(S3Exception err) {
            err.printStackTrace();
            throw new HttpMessageException(500, S3_ERROR);
        }
    }

}
