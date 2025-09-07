package nckh.felix.StupidParking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.dto.LoginDTO;
import nckh.felix.StupidParking.domain.dto.ResLoginDTO;
import nckh.felix.StupidParking.domain.dto.ResLogoutDTO;
import nckh.felix.StupidParking.service.StaffService;
import nckh.felix.StupidParking.util.SecurityUtil;

@RestController
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final StaffService staffService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityUtil securityUtil, StaffService staffService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.staffService = staffService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@RequestBody LoginDTO loginDTO) {
        // CHỈ CHO PHÉP STAFF LOGIN - username là username của account Staff
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // xác thực người dùng => chỉ Staff account mới được authenticate
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // Lấy thông tin staff theo username
        Staff staff = staffService.fetchStaffByUsername(loginDTO.getUsername());

        // Tạo JWT token cho Staff
        String access_token = this.securityUtil.createToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo thông tin staff cho response
        ResLoginDTO.StaffInfo staffInfo = new ResLoginDTO.StaffInfo(
                staff.getMaNV(),
                staff.getHoTen(),
                staff.getEmail(),
                staff.getChucVu().toString());

        ResLoginDTO res = new ResLoginDTO(access_token, "STAFF", staffInfo);
        return ResponseEntity.ok().body(res);
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
