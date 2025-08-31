package nckh.felix.StupidParking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        // Kiểm tra xem người dùng đã authenticated chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            // Nếu đã login bằng Staff, cho phép truy cập
            return ResponseEntity.ok("🎉 AUTHENTICATED - Chào mừng đến với Smart Parking Dashboard! " +
                    "Bạn đã đăng nhập thành công với tài khoản: " + authentication.getName() +
                    " | Quyền: " + authentication.getAuthorities());
        } else {
            // Nếu chưa login, chặn truy cập
            return ResponseEntity.status(401).body("🔒 TRUY CẬP BỊ TỪ CHỐI - " +
                    "Bạn cần đăng nhập bằng tài khoản Staff để truy cập hệ thống. " +
                    "Vui lòng sử dụng POST /login với username/password của Staff để lấy JWT token, " +
                    "sau đó thêm 'Authorization: Bearer YOUR_TOKEN' vào header của request.");
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("📊 DASHBOARD - Chào mừng " + authentication.getName() +
                    " đến với Dashboard quản lý bãi đỗ xe thông minh! " +
                    "🔑 Quyền truy cập: " + authentication.getAuthorities() +
                    " | 🕒 Thời gian truy cập: " + java.time.LocalDateTime.now());
        }

        return ResponseEntity.status(401).body("🔒 UNAUTHORIZED - Cần đăng nhập để truy cập dashboard");
    }

    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            return ResponseEntity.ok("✅ Authenticated as: " + authentication.getName() +
                    " | 🎯 Roles: " + authentication.getAuthorities() +
                    " | 🕐 Check time: " + java.time.LocalDateTime.now());
        }

        return ResponseEntity.status(401).body("❌ Not authenticated - Please login first using POST /login");
    }
}
