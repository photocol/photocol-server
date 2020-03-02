package photocol.util;

import jdk.net.SocketFlow;
import photocol.definitions.response.StatusResponse;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static photocol.definitions.response.StatusResponse.Status.STATUS_MISC;
import static photocol.definitions.response.StatusResponse.Status.STATUS_OK;

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

    public StatusResponse<ResponseInputStream<GetObjectResponse>> getObject(String key) {
        try {
            return new StatusResponse<>(STATUS_OK,
                    s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()));
        } catch(S3Exception exception) {
            exception.printStackTrace();
            // TODO: use more descriptive status
            return new StatusResponse<>(STATUS_MISC);
        }
    }

    public StatusResponse putObject(byte[] data, String uri) {
        try {
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(uri).build(),
                    RequestBody.fromBytes(data));
            return new StatusResponse(STATUS_OK);
        } catch(S3Exception exception) {
            exception.printStackTrace();
            // TODO: use more descriptive status
            return new StatusResponse(STATUS_MISC);
        }
    }

    // FIXME: dummy stub; implement this
    public StatusResponse deleteObject(String uri) {
        return new StatusResponse(STATUS_MISC);
    }

}
