package demo.api;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Controller
@RequestMapping("/demo")
public class ClientDetails {

    private static final String ESIGNET_SERVICE_URL = "https://esignet.zam-dst.mosip.net";
    private static final String CLIENT_ID = "rQkE5g9NtpwD_tI08fEvuyHNHoLuMWkzXzH3YBVBaCo";
    private static final String CLIENT_PRIVATE_KEY = "MIIEwQIBADANBgkqhkiG9w0BAQEFAASCBKswggSnAgEAAoIBAgCEEtHa4k21r+ff\n" +
            "tO1XKcCONEfpvN2pwoPE20pcoYQOmd8NLlOf5TAeNvkVsebXv+dYdTgd/vQUiLn+\n" +
            "rtlFD8WYC9k3OXpA9wr5nX6nPDyEuIfya+hIEYpYrF1DhkTQp3ktD76VzfOCylLf\n" +
            "EH2Xf84efTCvHc0aK7t6K1L7SYYaviVe3p7v/JjZyzt+tGfys2cidNMXDeARRkd5\n" +
            "O7CNsP0nXTAnY90XREcaCHjQsk6f0FcsP1RFZa1buNZqgWjJB2voBi1rI9418+Hk\n" +
            "dyMrU7hJv2ZOdKxyRDiwV6uACPbsyxK0+RgEWMRbUm80VBqlp9GY5vNPc9986G+8\n" +
            "vUX39L+01wIDAQABAoIBATd9bLxJHps6Z/KeoqVffm6A2sICvyMG/bPk6q5WkhRM\n" +
            "Y+G22B2y/m+Da03JLBLzpvCMd2Jq7wP9mh++OL0CbmN+funr9Uh8QxkzKbZ74XVl\n" +
            "zoB1A+fbrsOoz5qCPEFqS8g5+DoaiLgn53+e/xV69g/rS1wX6keXnjIrJ7c6YeS0\n" +
            "e2VOY/s8TUkB8IjJ1gEJT3v3EZFxHMfQjgWcneHV+NBzbReHUden0/bMlR8PeXyV\n" +
            "L29H8T/gSfwEjCrZWqRPxHjv6/3+DJp8Co1xKceQdhMIawFpGhAhfoMVDuY8T3TS\n" +
            "ME1lHKWajBrI8F3jZ95bDMtTe/ZbCmUnHC39+g4xBhM5AoGBDQfyzIU73BX0iz7F\n" +
            "zb1OjcV6MqnNj8MwZcwHa87VNVTzaFaQlXwTOTthTNVE2SV4SgeTNW9YP95dkss8\n" +
            "DVyfmMxf8cgctDQjJTgJRnUfGr1kgJZ6fIeV5koqCix3dPKbFYnWmvIeC+3MO+kJ\n" +
            "0LyEcYbqOycLV0ejtteFDO+RqfK1AoGBCiKiqL2pxxBALe6+tG/0Vo65NyryNvO7\n" +
            "SmPaJ6jVrO4pzRGwPoODupth3IpW6HAeuWrHitzX/2XioWjBmeKzN9ZVqeAOB3s9\n" +
            "qdBwqJgGPnqtKLwh1ehl3yUcWMSVewMuY398IeCGQQnk90XJz+oPMuaTYPXZjUX2\n" +
            "LQkpTz4KcUTbAoGBDACiNpcRh6oBULBE6TfQvuyBnWChoiUa0uiWfUpJP5I7kIML\n" +
            "MWTTIAf9mxNlEs1cenN3t+QS2OEsNyVS7su9hLPaO40iQykypflzMZ53Q93mUDpK\n" +
            "H6GTBEaTakOC1AkRp0kKjjUsT7h8QUJodAbvxtGJtWpFG+M0fBkOx9dxx6NxAoGB\n" +
            "BPCs6CLKRkUdg0YDnl+TcT1P+hvsw+w5NiFFjF83B3tu1I3yogXoh2SFZsHnapR7\n" +
            "5VSuvni2r74xoQs2kpN1V5oZVxvmuq7Pd54py4OOR5X0IQ7STzlfrkygbIQleDgs\n" +
            "vEK/dqPBzKHOANj7bWRg1CafX3t19u5K99inUgCjhmJRAoGBBOZM0Qgx+XscG+c4\n" +
            "ZP6YQqgerLDFfB3vIB03hvNma7edNdLaDk5yGfNqQxqcpDFQZgUBMG5TXvYZtS8R\n" +
            "GRX8EKapb291icEyU8tbCbOPrNdgAZigCzTVsFLcvXwnbXek0CkeI8uv6UZf5JtF\n" +
            "/aPqZqu6h8K+jFe3Tmv7KLdOSLuh";
    private static final String CALLBACK_URL = "http://localhost:3000/demo/api/clientDetails";

    @GetMapping("/api/clientDetails")
    public void handleAuthorizationCode(@RequestParam String code) {
        ResponseEntity<Map> response = getAccessToken(code);
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> data = response.getBody();
            assert data != null;
            String accessToken = (String) data.get("accessToken");
            Long expiresIn = ((Number) data.get("expiresIn")).longValue();
            String cookieName = "access_token";
        } else {
            Map<String, Object> error = response.getBody();
            int statusCode = response.getStatusCodeValue();
            String message = (String) error.get("message");
            System.out.println("HTTP code error: " + statusCode);
            System.out.println("Reason: " + message);

        }
    }

    private ResponseEntity<Map> getAccessToken(String authorizationCode) {
        String tokenEndPoint = ESIGNET_SERVICE_URL + "/v1/esignet/oauth/v2/token";
        String header = "{\"alg\":\"RS256\",\"TYP\":\"JWT\"}";

        long iat = System.currentTimeMillis() / 1000;
        long exp = iat + 3600;

        Map<String, Object> payload = new HashMap<>();
        payload.put("iss", CLIENT_ID);
        payload.put("sub", CLIENT_ID);
        payload.put("iat", iat);
        payload.put("exp", exp);
        payload.put("aud", tokenEndPoint);

        // Encode JWT
        String jwt = "";

        // HTTP Request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Map<String, String> data = new HashMap<>();
        data.put("code", authorizationCode);
        data.put("client_id", CLIENT_ID);
        data.put("redirect_uri", CALLBACK_URL);
        data.put("grant_type", "authorization_code");
        data.put("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        data.put("client_assertion", jwt);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(tokenEndPoint, request, Map.class);
    }
}
