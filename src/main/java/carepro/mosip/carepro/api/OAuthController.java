package carepro.mosip.carepro.api;

import carepro.mosip.carepro.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.GET }, allowedHeaders = "Content-Type")
@RequestMapping("/demo/api")
public class OAuthController {

    @Autowired
    private OAuthService oauthService;

    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);

    @GetMapping("/clientDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAuthorizationCode(@RequestParam String code, HttpServletResponse response) {
        try {
            String tokenResponse = oauthService.getAccessToken(code);
            Map<String, Object> tokenResult = oauthService.handleTokenResponse(tokenResponse);

            if (tokenResult.containsKey("error")) {
                Map<String, Object> error = castToMap(tokenResult.get("error"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", error));
            } else {
                Map<String, Object> data = castToMap(tokenResult.get("data"));
                String accessToken = (String) data.get("accessToken");
                long expiresIn = (long) data.get("expiresIn");

                // Set cookie with access token
                Cookie cookie = new Cookie("access_token", accessToken);
                cookie.setMaxAge((int) (expiresIn / 1000));
                cookie.setPath("/");
                response.addCookie(cookie);

                // Include the access token in the response body
                return ResponseEntity.ok(Map.of(
                        "accessToken", accessToken,
                        "expiresIn", expiresIn
                ));
            }
        } catch (Exception e) {
            logger.error("Error handling authorization code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/handleAuthorizationCode")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getClientDetails(@RequestParam String accessToken) {
        try {
            // Get user info using the access token
            Map<String, Object> userInfoResult = oauthService.getUserInfo(accessToken);

            if (userInfoResult.containsKey("error")) {
                Map<String, Object> error = castToMap(userInfoResult.get("error"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", error));
            } else {
                Map<String, Object> userInfo = castToMap(userInfoResult.get("data"));
                return ResponseEntity.ok(userInfo);
            }
        } catch (Exception e) {
            logger.error("Error fetching client details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object obj) {
        return (Map<String, Object>) obj;
    }
}
