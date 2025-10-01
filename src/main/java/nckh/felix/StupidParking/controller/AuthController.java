package nckh.felix.StupidParking.controller;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.domain.dto.ResLogoutDTO;
import nckh.felix.StupidParking.domain.dto.UnifiedLoginDTO;
import nckh.felix.StupidParking.domain.dto.UnifiedLoginResponseDTO;
import nckh.felix.StupidParking.service.UnifiedAuthService;
import nckh.felix.StupidParking.util.SecurityUtil;

@RestController
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UnifiedAuthService unifiedAuthService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityUtil securityUtil, UnifiedAuthService unifiedAuthService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.unifiedAuthService = unifiedAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<UnifiedLoginResponseDTO> login(@Valid @RequestBody UnifiedLoginDTO loginDTO) {
        try {
            // Sử dụng UnifiedAuthService để xác thực
            UnifiedAuthService.AuthResult authResult = unifiedAuthService.authenticate(loginDTO);

            if (authResult == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(null);
            }

            String accessToken;
            UnifiedLoginResponseDTO response;

            if (authResult.isUser()) {
                // Xử lý User login
                User user = authResult.getUser();

                // Tạo Authentication object cho User với role USER
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

                // Tạo JWT token cho User
                accessToken = this.securityUtil.createToken(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Tạo response cho User
                UnifiedLoginResponseDTO.UserInfo userInfo = new UnifiedLoginResponseDTO.UserInfo(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        "USER");

                response = new UnifiedLoginResponseDTO(accessToken, userInfo);

            } else if (authResult.isStaff()) {
                // Xử lý Staff login (giữ nguyên logic cũ)
                Staff staff = authResult.getStaff();

                // Nạp input gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        staff.getAccount().getUsername(), loginDTO.getPassword());

                // xác thực người dùng => chỉ Staff account mới được authenticate
                Authentication authentication = authenticationManagerBuilder.getObject()
                        .authenticate(authenticationToken);

                // Tạo JWT token cho Staff
                accessToken = this.securityUtil.createToken(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Tạo response cho Staff
                UnifiedLoginResponseDTO.StaffInfo staffInfo = new UnifiedLoginResponseDTO.StaffInfo(
                        staff.getMaNV(),
                        staff.getHoTen(),
                        staff.getEmail(),
                        staff.getChucVu().toString());

                response = new UnifiedLoginResponseDTO(accessToken, staffInfo);

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(null);
            }

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    @GetMapping("/auth/account")
    public ResponseEntity<String> getAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("Authenticated as: " + authentication.getName() +
                    " with authorities: " + authentication.getAuthorities().toString());
        }
        return ResponseEntity.ok("Not authenticated");
    }

    @PostMapping("/logout")
    public ResponseEntity<ResLogoutDTO> logout() {
        // Lấy thông tin user hiện tại trước khi logout
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "unknown";

        // Xóa authentication khỏi SecurityContext
        SecurityContextHolder.clearContext();

        // Tạo response logout
        ResLogoutDTO response = new ResLogoutDTO(
                "Đăng xuất thành công cho user: " + username + ". Vui lòng xóa token ở phía client.");

        return ResponseEntity.ok().body(response);
    }
}
