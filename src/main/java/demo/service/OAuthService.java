package demo.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static demo.Constants.*;

@Service
public class OAuthService {

    public RSAPrivateKey getPrivateKey(String privateKeyPath) throws Exception {
        String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        privateKeyContent = privateKeyContent.replaceAll("", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }

    public String createJwt(String clientId, String tokenEndPoint, RSAPrivateKey privateKey) {
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);
        Instant now = Instant.now();

        return JWT.create()
                .withIssuer(clientId)
                .withSubject(clientId)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(3600)))
                .withAudience(tokenEndPoint)
                .sign(algorithm);
    }

    public String getAccessToken(String authorizationCode) throws Exception {
        String tokenEndPoint = ESIGNET_SERVICE_URL + "/v1/esignet/oauth/v2/token";
        RSAPrivateKey privateKey = getPrivateKey(CLIENT_PRIVATE_KEY);
        String jwt = createJwt(CLIENT_ID, tokenEndPoint, privateKey);

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

        return response.body();
    }

    public Map<String, Object> handleTokenResponse(String tokenResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJson = mapper.readTree(tokenResponse);

        if (responseJson.has("access_token")) {
            String accessToken = responseJson.get("access_token").asText();
            int expiresIn = responseJson.get("expires_in").asInt();

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("expiresIn", System.currentTimeMillis() + expiresIn * 1000L);
            response.put("data", data);
            response.put("error", null);
            return response;
        } else {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> error = new HashMap<>();
            error.put("statusCode", httpCode);
            error.put("message", "not able to get access token");
            response.put("data", null);
            response.put("error", error);
            return response;
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


