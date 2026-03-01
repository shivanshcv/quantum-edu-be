import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Standalone JWT generator for testing. Uses same format as JwtService.
 * Run: cd scripts && javac GenerateJwtToken.java && java GenerateJwtToken 1
 */
public class GenerateJwtToken {
    private static final String SECRET = "quantum-edu-jwt-secret-key-min-256-bits-for-hs256-algorithm";
    private static final long EXPIRY_HOURS = 24;

    public static void main(String[] args) {
        String userId = args.length > 0 ? args[0] : "1";
        long now = System.currentTimeMillis() / 1000;
        long exp = now + (EXPIRY_HOURS * 3600);

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = String.format("{\"sub\":\"%s\",\"role\":\"USER\",\"iat\":%d,\"exp\":%d}",
                userId, now, exp);

        String headerB64 = base64Url(header.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        String toSign = headerB64 + "." + payloadB64;

        byte[] sig = hmacSha256(toSign.getBytes(StandardCharsets.UTF_8), SECRET.getBytes(StandardCharsets.UTF_8));
        String sigB64 = base64Url(sig);

        String token = toSign + "." + sigB64;
        System.out.println(token);
    }

    private static String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static byte[] hmacSha256(byte[] data, byte[] key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            SecretKey secretKey = new SecretKeySpec(key, "HmacSHA256");
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
