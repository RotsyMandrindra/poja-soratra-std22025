package hei.school.soratra.endpoint.rest.controller.soratra;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/soratra")
public class PoeticPhraseController {

    private static final String BUCKET_NAME = "preprod-bucket-poja-soratra-std22025-bucket-whui3dqwcmio";

    @PostMapping("/{id}")
    public ResponseEntity<String> savePoeticPhrase(@PathVariable Long id, @RequestBody String phrase) {
        try (S3Client s3 = S3Client.create()) {
            String objectKey = "poetic_phrases/" + id;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .build();
            PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest,
                    RequestBody.fromString(phrase, StandardCharsets.UTF_8));


            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .build();
            GetObjectResponse getObjectResponse = s3.getObject(getObjectRequest);


            String fileContent = new String(getObjectResponse.readAllBytes(), StandardCharsets.UTF_8);


            return ResponseEntity.ok(fileContent);
        } catch (Exception e) {
            return ResponseEntity.ok("Error during file processing.");
        }
    }
}
