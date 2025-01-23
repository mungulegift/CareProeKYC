package carepro.mosip.carepro.api;

import carepro.mosip.carepro.service.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
public class OAuthController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);

    @Autowired
    private OAuthService oauthService;

    @GetMapping("/demo/api/clientDetails")
    public Map<String, Object> handleClientDetails(@RequestParam("code") String authorizationCode) {
        try {
            // Log the authorization code received
            logger.info("Received authorization code: {}", authorizationCode);

            // Use the authorization code to get an access token
            String tokenResponse = oauthService.getAccessToken(authorizationCode);

            // Log the token response
            logger.info("Token response: {}", tokenResponse);

            // Handle the token response and extract user info
            Map<String, Object> tokenData = oauthService.handleTokenResponse(tokenResponse);
            Object dataObj = tokenData.get("data");

            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                String accessToken = (String) data.get("accessToken");

                // Log the access token
                logger.info("Access token: {}", accessToken);

                // Use the access token to get user info
                Map<String, Object> userInfo = oauthService.getUserInfo(accessToken);
                return userInfo;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error handling client details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to handle client details");
            return errorResponse;
        }
    }
//    @GetMapping("/my-name")
//    public Map<String, Object> getName(@RequestParam ("name") String name) {
//
//        try {
//            oauthService.
//        }
//    }

}


