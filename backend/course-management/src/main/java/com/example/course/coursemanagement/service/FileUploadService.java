package com.example.course.coursemanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

@Service
@Slf4j
public class FileUploadService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final long MAX_AVATAR_BYTES = 5L  * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES  = 200L * 1024 * 1024;

    private static final List<String> ALLOWED_IMAGE_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final List<String> ALLOWED_VIDEO_TYPES =
            Arrays.asList("video/mp4", "video/quicktime", "video/webm", "video/x-msvideo");

    // ══════════════════════════════════════════════════════════
    //  UPLOAD AVATAR
    // ══════════════════════════════════════════════════════════
    public String uploadAvatar(MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_AVATAR_BYTES, "Ảnh");

        String publicId  = "eduflow/avatars/" + UUID.randomUUID();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        // Params gửi lên (không gồm file, api_key, resource_type)
        // Sort alphabetically: public_id → timestamp
        Map<String, String> signParams = new TreeMap<>();
        signParams.put("public_id", publicId);
        signParams.put("timestamp", timestamp);

        String signature = sign(signParams);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file",      toResource(file));
        body.add("public_id", publicId);
        body.add("timestamp", timestamp);
        body.add("api_key",   apiKey);
        body.add("signature", signature);

        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
        return doUpload(url, body);
    }

    // ══════════════════════════════════════════════════════════
    //  UPLOAD VIDEO
    // ══════════════════════════════════════════════════════════
    public String uploadVideo(MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_BYTES, "Video");

        String publicId  = "eduflow/videos/" + UUID.randomUUID();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        Map<String, String> signParams = new TreeMap<>();
        signParams.put("public_id", publicId);
        signParams.put("timestamp", timestamp);

        String signature = sign(signParams);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file",      toResource(file));
        body.add("public_id", publicId);
        body.add("timestamp", timestamp);
        body.add("api_key",   apiKey);
        body.add("signature", signature);

        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/video/upload";
        return doUpload(url, body);
    }

    // ══════════════════════════════════════════════════════════
    //  XÓA FILE CŨ
    // ══════════════════════════════════════════════════════════
    public void deleteFile(String cloudinaryUrl, String resourceType) {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) return;
        try {
            String publicId  = extractPublicId(cloudinaryUrl);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            Map<String, String> signParams = new TreeMap<>();
            signParams.put("public_id", publicId);
            signParams.put("timestamp", timestamp);
            String signature = sign(signParams);

            String url = "https://api.cloudinary.com/v1_1/" + cloudName
                    + "/" + resourceType + "/destroy";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("public_id", publicId);
            body.add("timestamp", timestamp);
            body.add("api_key",   apiKey);
            body.add("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
            log.info("Deleted: {}", publicId);
        } catch (Exception e) {
            log.warn("Delete failed: {}", e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Cloudinary signature:
     * 1. Sort params alphabetically
     * 2. Join: "key1=val1&key2=val2"
     * 3. Append api_secret (no separator)
     * 4. SHA1 hex
     */
    private String sign(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    if (sb.length() > 0) sb.append("&");
                    sb.append(e.getKey()).append("=").append(e.getValue());
                });
        sb.append(apiSecret);
        return sha1(sb.toString());
    }

    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA1 error", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String doUpload(String url, MultiValueMap<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Map> resp = restTemplate.postForEntity(
                url, new HttpEntity<>(body, headers), Map.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            String secureUrl = (String) resp.getBody().get("secure_url");
            log.info("Uploaded: {}", secureUrl);
            return secureUrl;
        }
        throw new RuntimeException("Cloudinary error: " + resp.getStatusCode());
    }

    private ByteArrayResource toResource(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        return new ByteArrayResource(bytes) {
            @Override public String getFilename() {
                return file.getOriginalFilename() != null
                        ? file.getOriginalFilename() : "file";
            }
        };
    }

    private String extractPublicId(String url) {
        int idx = url.indexOf("/upload/");
        if (idx == -1) return url;
        String after = url.substring(idx + 8);
        if (after.matches("v\\d+/.*")) after = after.replaceFirst("v\\d+/", "");
        int dot = after.lastIndexOf('.');
        return dot > 0 ? after.substring(0, dot) : after;
    }

    private void validateFile(MultipartFile file, List<String> allowed,
                              long maxBytes, String label) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException(label + " không được trống!");
        String ct = file.getContentType();
        if (ct == null || !allowed.contains(ct))
            throw new IllegalArgumentException(label + " phải là: " + String.join(", ", allowed));
        if (file.getSize() > maxBytes)
            throw new IllegalArgumentException(label + " tối đa " + (maxBytes/1_048_576) + "MB!");
    }
}