package photocol.util;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

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

    public ResponseInputStream<GetObjectResponse> getObject(String key) {
        try {
            return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
        } catch(S3Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

}
