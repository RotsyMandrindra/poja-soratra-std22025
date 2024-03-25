package hei.school.soratra.endpoint.rest.controller.soratra;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@RestController
@RequestMapping("/soratra")
public class PoeticPhraseController {

  private static final String BUCKET_NAME =
      "preprod-bucket-poja-soratra-std22025-bucket-whui3dqwcmio";

  @PostMapping("/{id}")
  public ResponseEntity<String> savePoeticPhrase(
      @PathVariable Long id, @RequestBody String phrase) {
    try (S3Client s3 = S3Client.create()) {
      String objectKey = "poetic_phrases/" + id;
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder().bucket(BUCKET_NAME).key(objectKey).build();
      PutObjectResponse putObjectResponse =
          s3.putObject(putObjectRequest, RequestBody.fromString(phrase, StandardCharsets.UTF_8));
      return ResponseEntity.ok("Poetic phrase successfully saved.");
    } catch (Exception e) {
      return ResponseEntity.ok("Error during file processing.");
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, String>> getPoeticPhraseUrls(@PathVariable Long id) {
    try (S3Client s3 = S3Client.create();
        S3Presigner presigner = S3Presigner.create()) {

      String originalKey = "poetic_phrases/" + id;
      String transformedKey = "poetic_phrases_transformed/" + id;

      PresignedGetObjectRequest originalPresignedRequest =
          presigner.presignGetObject(
              GetObjectPresignRequest.builder()
                  .signatureDuration(Duration.ofMinutes(15))
                  .getObjectRequest(
                      GetObjectRequest.builder().bucket(BUCKET_NAME).key(originalKey).build())
                  .build());

      PresignedGetObjectRequest transformedPresignedRequest =
          presigner.presignGetObject(
              GetObjectPresignRequest.builder()
                  .signatureDuration(Duration.ofMinutes(15))
                  .getObjectRequest(
                      GetObjectRequest.builder().bucket(BUCKET_NAME).key(transformedKey).build())
                  .build());

      Map<String, String> response = new HashMap<>();
      response.put("original_url", originalPresignedRequest.url().toString());
      response.put("transformed_url", transformedPresignedRequest.url().toString());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.ok(new HashMap<>());
    }
  }
}
