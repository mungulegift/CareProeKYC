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

    public static final String ESIGNET_SERVICE_URL = "https://esignet.zam-dst.mosip.net";
    public static final String CLIENT_ID = "bfgC0STwXNn7EkPksKCc91VQytFwad9XJPWSyaClZNE";
    public static final String CALLBACK_URL = "http://localhost:8080/demo/api/clientDetails";

    private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEwgIBADANBgkqhkiG9w0BAQEFAASCBKwwggSoAgEAAoIBAgCV41YfWq2fI3yE\n" +
            "dDtXNnPk7VEk9ozvjQdAsirxDfccfsQaURie4L6ddYVL+yKojFXtudDYvoL9Pl2b\n" +
            "V0YgzzZB8pLrUiUFaRaqr3yPeJu3v2BhA3TsENqhhLFXazqM2mKPhCeQ+Xi3gyfx\n" +
            "f3/0d/CULyF49jBB2X/FzvvMc2D7kNEMbPPqnX0NZMMbFh7qgHbx52QVy7O2Ax+y\n" +
            "p78YNRL6YBasjPkqp0gLuvcFVNFVI6JsjaXXVe5kM5BOcD/ytnpegplOafS3l37n\n" +
            "oxcyrdYIAESF5qTIBqKjKvvud9f5y1JBg7jU3cyMq3a0Wz9SVNgbiPczn28IUl/W\n" +
            "VKvpzsWUIwIDAQABAoIBAgCJcRfk0mKInaKtVmOfmjf1gMO6kDY152zyHy4PMo4Y\n" +
            "3Aj4VMqtzUoc2smrHZLvWRfmUFA4n6E/lLODISkWJswK9uNP01hNOMrnItmmOaMY\n" +
            "f+1hqCjL2ryQBTwU/CeJRyu/jCA36PqP1D2JTi/HvT+msf0QjTc8+rMWcLEmDdqt\n" +
            "c0wcERz2xNag2PFulVLOBZYGQGfJTMlIEBabWoS1G0Kx8nvLSvvTXC7izP6llFnz\n" +
            "AUcmnfMIbesXd7oay9x5J4IpTt7Lijnj+eD6WiekDprO7BVqAa8bOdAOar5VsNcv\n" +
            "/5mrPVyTwL6w5f8MNJzdodRBuzwP+t17Jmox8udeS3LBMQKBgQ2PVfrnJp5jijpz\n" +
            "bK5yMxPVlEqvK8IyXDRxKtMhOLgIFVmU8veeikFYPVQnKjdpx743uq+lOFGBBSoJ\n" +
            "ZB3B/uE3GsOBUvgnRieilL8pYVqzLv8yKQJPgkpSFxds91PiohrejtSPgpMQsCtE\n" +
            "cSE931BbRGwCMkofIDxIqacBgeJhCwKBgQsNw6YEmkJvu6x6h29bS3loY0/0Zqup\n" +
            "JFun/nLbV+BS30QkJMxNFEvqmhUBd+h2WLEKdoXcKddCbfsLleCBqg8S18OgZhvI\n" +
            "qiorQeOT4sBGoeP5iEDKiJl1XVSYaCiD/6dZyxtdL1qely63Rh98Efnz8VksEmoW\n" +
            "dT1c2mnoyWi4SQKBgQf3yAAtP8Ymm50JIj3HoTfwcrkIAeCVExVFwnJZvSh2p4UC\n" +
            "P1s97+PB5BfFA8uX/uVJfgukPFm4RL6U0h2iVxEj/jgPkB99hH3ZzSofOLPBQsZ2\n" +
            "RR60dc0SO7tqLlM+gx7i1G8W/RVJ4xsploiAAO8JEkWdbMudSqXh1L7r5dSOTQKB\n" +
            "gQHnex7siA6N5QzPhuleFuBxZTpu5gkIeGT5ydHLi5XvcNryRwyS8r4pSiX4PMnl\n" +
            "KV5GaSiiRur4XK30zvsAG2XBXz4qMazzoqzlCtLDtTpecKEvaZOP0HxvRYa/QqJI\n" +
            "KxCPXXf3U6MO0wiPNhtSal5e8eLkvr9b4xKRHmdEXP4XUQKBgQEocYWKCwVqzr3X\n" +
            "+uZt7IIpiL0LhgFicko4zeE+LTcGrYg99JtQRl3MEV1X4+rvnXRrqALRigc1Xuc6\n" +
            "LaHV26ZoW7HN7denF22fIcFUw7bCQFF9b0toMSMDU8dazHT6I825vIHp/FTyWSSD\n" +
            "ib5xMFkzkeAcBenUkqqWjih4oxJzWA==\n" +
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
