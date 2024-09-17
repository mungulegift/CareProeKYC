package carepro.mosip.carepro.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;


@Service
public class OAuthService {

    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    public static final String ESIGNET_SERVICE_URL = "https://esignet-ekyc.grz.gov.zm";
    public static final String CLIENT_ID = "RvyY2Lj7BRTR5yT2KntqL8UqUrltUZNvmrmBuq0ekOk";
    public static final String CALLBACK_URL = "http://localhost:8080/demo/api/clientDetails";

    private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAgCFfe+Hv6IzxoXo\n" +
            "43OzELhTIij4lGwBCDbxbaJO4eAyVLlfzntppVZI2q3LosXYqKip4mS5F8aXGP08\n" +
            "cEGumcK8lHHFmvZn9ZHjxPALNdZTu5jrGTS0RuR5qGu7MTtOsfHUIFjDn1zXf7Di\n" +
            "firGSsPUZ5w9sfPmN2A1+e2D4Fza6sLKCGmeq4P4+PsFn8dncqoSZ1iqRerXll+D\n" +
            "z+nd7JQENjp2OvwcrHbwXroNlTUL3vpjS4IcDjr5qrH2waVL78smv//pBbNYLTDj\n" +
            "3vEJ5o3mPUjUj3tpgoAfRUgH0EJnlNlcxo8GSvKszvPoJDaGiR1eI/3XHnBq2DD7\n" +
            "18775f/HpQIDAQABAoIBAREXcb64u4TTizupO5wWrPL/8az6G+X57kb1GY3t/62S\n" +
            "CXYJ0uVQ5P5I+W2UNq6p0YaG1QyZzbR035+IY/B+AV/xxateTuQ6neZCxdU5nGp2\n" +
            "H3sibAwU85XcCYcbI5PM1sCpL5REaDezQquNKd7fnbkpVujTSv4T4v6mNFHpPRpO\n" +
            "Z32ptAEiZvUMi4qE8Itc/FNa3/T7mrBxsKY18RRlCB7c+FDBNExVnIldT4jcz9PZ\n" +
            "EM7wz65xHUp375Z5OnKese4g3VoLjJbj3diqAdEVtz1AA37fJ7RTMk7BnKeGAwFR\n" +
            "Su1nb5MhxdGTjNZLj5K6M1fz9c3dNnFRZVNFmgVWxqXhAoGBDqEq0iYpfB81VOYA\n" +
            "hrD1G5dKvFMUtnIFemyDZTDlVmzYWo/YSKxeRrk7UcukFuuqd7gjaElXCcdsUsia\n" +
            "Min2oQWQjWaktmLmedGUt9bWvyIZbUEl9jFnTEtRMLXfG/zba/ddM7dZ+QUHB+O0\n" +
            "JwX2+nMWkK6vsp+b9IlbGYiP65tZAoGBCR/zegjdEP1SgQXT0VDRlmcfUQS9dshX\n" +
            "mKtXKDF2w+zRvevkTRCh4M0vhblQAn7BYWIZd9f/9sjUG49kOc+n7uMDtqNsSZ0P\n" +
            "p5ZWOrEME2G18SYrqUvo1+/HsCpaBgGkhXJGdIWaRT7pIizhX+Jjo4InVfn/X+bh\n" +
            "mumwBAsQJyEtAoGBDOwAZbS9idwLAF1T86SUK3W71pJPxkL6YcM9YAELYQYtJ/jM\n" +
            "YoyVjUSJh9smqfQpEyv7Yl79WS7jmE+GG28DYAEJSEx0ioDa8JjVEnjaH+MfsGgN\n" +
            "/uJuSm5wXWUXl6IFsdzBXy/Bcfol1qsi8iELoPZM5SjXGiBu4VT9uZTtAn+pAoGB\n" +
            "AsB9GJ/VtAsCN+E/CZCbNQXyvud0EH/JU0TRa1DHpb0lYGblGozalUasf2LtC2OZ\n" +
            "BLqmPauKCa51sb4kFv9WgUg71Xgh722LzBod9WNx5eKBEbJWVp+LIJyzF6EeN1OF\n" +
            "f62ALCIi7aFUhDcO3XEUZbqaDGbyKsUh4yt5Jy8C9xodAoGABWcQczvnkTm0908/\n" +
            "kctuZSL+1CNEkOwEU5bbLDzM6rGMs29WWHsODMFMdJWQXzGhhx2X0ygSX1lA2XMe\n" +
            "VbAVdg03i1mIdxcju75GwvyLm+Sko6AuV3owSJV12nmAwGgRxFvEhFKZcJHS7qFz\n" +
            "VIHhExZyljlSlYCturGri3fpzjg=\n" +
            "-----END PRIVATE KEY-----";
    public RSAPrivateKey getPrivateKey() throws Exception {
        try {
            String privateKeyContent = PRIVATE_KEY
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            logger.error("Error reading private key", e);
            throw new Exception("Could not read private key", e);
        }
    }

    public String createJwt(String clientId, String tokenEndPoint, RSAPrivateKey privateKey) {
        try {
            Algorithm algorithm = Algorithm.RSA256(null, privateKey);
            Instant now = Instant.now();

            return JWT.create()
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withIssuedAt(Date.from(now))
                    .withExpiresAt(Date.from(now.plusSeconds(3600)))
                    .withAudience(tokenEndPoint)
                    .sign(algorithm);
        } catch (Exception e) {
            logger.error("Error creating JWT", e);
            return null;
        }
    }

    public String getAccessToken(String authorizationCode) throws Exception {
        try {
            String tokenEndPoint = ESIGNET_SERVICE_URL + "/v1/esignet/oauth/v2/token";
            RSAPrivateKey privateKey = getPrivateKey();
            String jwt = createJwt(CLIENT_ID, tokenEndPoint, privateKey);

            if (jwt == null) {
                throw new Exception("JWT creation failed");
            }

            Map<Object, Object> data = new HashMap<>();
            data.put("code", authorizationCode);
            data.put("client_id", CLIENT_ID);
            data.put("redirect_uri", CALLBACK_URL);
            data.put("grant_type", "authorization_code");
            data.put("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
            data.put("client_assertion", jwt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenEndPoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(ofFormData(data))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("Request URI: " + request.uri());
            logger.info("Request Headers: " + request.headers());
            logger.info("Request Body: " + ofFormData(data));
            logger.info("Response Status Code: " + response.statusCode());
            logger.info("Response Body: " + response.body());

            if (response.statusCode() != 200) {
                logger.error("Token endpoint returned status code " + response.statusCode());
                logger.error("Response body: " + response.body());
                throw new Exception("Token endpoint returned status code " + response.statusCode());
            }

            return response.body();
        } catch (Exception e) {
            logger.error("Error getting access token", e);
            throw new Exception("Could not get access token", e);
        }
    }

    public Map<String, Object> handleTokenResponse(String tokenResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        logger.info("Token Response: " + tokenResponse);
        JsonNode responseJson = mapper.readTree(tokenResponse);

        Map<String, Object> response = new HashMap<>();
        if (responseJson.has("access_token")) {
            String accessToken = responseJson.get("access_token").asText();
            int expiresIn = responseJson.get("expires_in").asInt();

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("expiresIn", System.currentTimeMillis() + expiresIn * 1000L);
            response.put("data", data);
            response.put("error", null);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("statusCode", 500);
            error.put("message", "Not able to get access token");
            response.put("data", null);
            response.put("error", error);
        }
        return response;
    }

    public Map<String, Object> getUserInfo(String accessToken) throws Exception {
        try {
            String userInfoEndpoint = ESIGNET_SERVICE_URL + "/v1/esignet/oidc/userinfo";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(userInfoEndpoint))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                logger.info("=============Here is it: " + response.body());
                return decodeUserInfo(response.body());
            } else {
                throw new Exception("Failed to get user info, status code: " + response.statusCode());
            }
        } catch (Exception e) {
            logger.error("Error getting user info", e);
            throw new Exception("Could not get user info", e);
        }
    }

    private Map<String, Object> decodeUserInfo(String encodedUserInfo) throws Exception {
        String[] parts = encodedUserInfo.split("\\.");
        if (parts.length > 1) {
            byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
            String payloadJsonStr = new String(decodedBytes);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payload = mapper.readTree(payloadJsonStr);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("data", payload);
            userInfo.put("error", null);
            return userInfo;
        } else {
            throw new Exception("Invalid JWT format");
        }
    }

    private static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        StringBuilder form = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (form.length() > 0) {
                form.append("&");
            }
            form.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            form.append("=");
            form.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(form.toString());
    }
}
