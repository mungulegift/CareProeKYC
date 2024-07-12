package demo.api;

import demo.model.User;
import demo.repo.UserRepository;
import demo.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.GET }, allowedHeaders = "Content-Type")
@RequestMapping("/demo/api")
public class OAuthController {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private OAuthService oauthService;

    @GetMapping("/search")
    public ResponseEntity<User> search(@RequestParam String nrc) {
        Optional<User> user = userRepo.findByNrc(nrc);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/clientDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAuthorizationCode(@RequestParam String code, HttpServletResponse response) throws Exception {
        String tokenResponse = oauthService.getAccessToken(code);
        Map<String, Object> result = oauthService.handleTokenResponse(tokenResponse);

        System.out.println("Here is the access token "+tokenResponse);

        if (result.containsKey("error")) {
            Map<String, Object> error = castToMap(result.get("error"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", error));
        } else {
            Map<String, Object> data = castToMap(result.get("data"));
            String accessToken = (String) data.get("accessToken");
            long expiresIn = (long) data.get("expiresIn");

            Cookie cookie = new Cookie("access_token", accessToken);
            cookie.setMaxAge((int) (expiresIn / 1000));
            cookie.setPath("/");
            response.addCookie(cookie);

            // Returning the claims data
            return ResponseEntity.ok(data);

        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object obj) {
        return (Map<String, Object>) obj;
    }
}
