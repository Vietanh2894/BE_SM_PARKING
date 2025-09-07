package nckh.felix.StupidParking.controller;

import java.util.List;
import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.domain.RestResponse;
import nckh.felix.StupidParking.domain.dto.ResUserLoginDTO;
import nckh.felix.StupidParking.domain.dto.UserLoginDTO;
import nckh.felix.StupidParking.domain.dto.UserDashboardDTO;
import nckh.felix.StupidParking.service.UserService;
import nckh.felix.StupidParking.service.UserAuthService;
import nckh.felix.StupidParking.service.UserDashboardService;
import nckh.felix.StupidParking.util.SecurityUtil;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
//csas

@RestController
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserAuthService userAuthService;
    private final SecurityUtil securityUtil;
    private final UserDashboardService userDashboardService;

    public UserController(UserService userService, PasswordEncoder passwordEncoder,
            UserAuthService userAuthService, SecurityUtil securityUtil,
            UserDashboardService userDashboardService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userAuthService = userAuthService;
        this.securityUtil = securityUtil;
        this.userDashboardService = userDashboardService;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(@RequestBody User xUser) {
        // Mã hóa mật khẩu trước khi lưu
        String hashPassword = this.passwordEncoder.encode(xUser.getPassword());
        xUser.setPassword(hashPassword);

        User vUser = this.userService.handleCreateUser(xUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(vUser);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
        User vUser = this.userService.fetchUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(vUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUsers());
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User xUser) {
        String hashPassword = this.passwordEncoder.encode(xUser.getPassword());
        xUser.setPassword(hashPassword);
        User vUser = this.userService.handleUpdateUser(xUser);

        return ResponseEntity.ok(vUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        if (id >= 1500) {
            throw new IdInvalidException("id khong lon hon 1501");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @PostMapping("/user/login")
    public ResponseEntity<RestResponse<ResUserLoginDTO>> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            // Xác thực user
            User authenticatedUser = userAuthService.authenticateUser(userLoginDTO);

            if (authenticatedUser == null) {
                RestResponse<ResUserLoginDTO> errorResponse = new RestResponse<>();
                errorResponse.setStatusCode(401);
                errorResponse.setMessage("Email hoặc password không đúng");
                errorResponse.setError(null);
                errorResponse.setData(null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Tạo Authentication object cho User với role USER
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser.getEmail(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            // Tạo JWT token
            String accessToken = securityUtil.createToken(authentication);

            // Set authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Tạo response
            ResUserLoginDTO.UserInfo userInfo = new ResUserLoginDTO.UserInfo(
                    authenticatedUser.getId(),
                    authenticatedUser.getName(),
                    authenticatedUser.getEmail(),
                    "USER");

            ResUserLoginDTO response = new ResUserLoginDTO(accessToken, "USER", userInfo);

            RestResponse<ResUserLoginDTO> successResponse = new RestResponse<>();
            successResponse.setStatusCode(200);
            successResponse.setMessage("Đăng nhập thành công");
            successResponse.setError(null);
            successResponse.setData(response);

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            RestResponse<ResUserLoginDTO> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setMessage("Lỗi server: " + e.getMessage());
            errorResponse.setError("INTERNAL_SERVER_ERROR");
            errorResponse.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============== USER DASHBOARD APIs ==============

    /**
     * API lấy dashboard tổng quan cho User (cần JWT token)
     */
    @GetMapping("/user/dashboard")
    public ResponseEntity<RestResponse<UserDashboardDTO>> getUserDashboard() {
        try {
            // Lấy email từ Authentication context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                RestResponse<UserDashboardDTO> errorResponse = new RestResponse<>();
                errorResponse.setStatusCode(401);
                errorResponse.setMessage("Vui lòng đăng nhập để truy cập dashboard");
                errorResponse.setError("UNAUTHORIZED");
                errorResponse.setData(null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String email = authentication.getName(); // Email từ JWT token
            UserDashboardDTO dashboard = userDashboardService.getUserDashboard(email);

            RestResponse<UserDashboardDTO> successResponse = new RestResponse<>();
            successResponse.setStatusCode(200);
            successResponse.setMessage("Lấy thông tin dashboard thành công");
            successResponse.setError(null);
            successResponse.setData(dashboard);

            return ResponseEntity.ok(successResponse);

        } catch (IllegalArgumentException e) {
            RestResponse<UserDashboardDTO> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(404);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setError("USER_NOT_FOUND");
            errorResponse.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            RestResponse<UserDashboardDTO> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setMessage("Lỗi server: " + e.getMessage());
            errorResponse.setError("INTERNAL_SERVER_ERROR");
            errorResponse.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * API lấy danh sách xe của User (cần JWT token)
     */
    @GetMapping("/user/vehicles")
    public ResponseEntity<RestResponse<List<UserDashboardDTO.VehicleInfo>>> getUserVehicles() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                RestResponse<List<UserDashboardDTO.VehicleInfo>> errorResponse = new RestResponse<>();
                errorResponse.setStatusCode(401);
                errorResponse.setMessage("Vui lòng đăng nhập để xem danh sách xe");
                errorResponse.setError("UNAUTHORIZED");
                errorResponse.setData(null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String email = authentication.getName();
            List<UserDashboardDTO.VehicleInfo> vehicles = userDashboardService.getUserVehicles(email);

            RestResponse<List<UserDashboardDTO.VehicleInfo>> successResponse = new RestResponse<>();
            successResponse.setStatusCode(200);
            successResponse.setMessage("Lấy danh sách xe thành công");
            successResponse.setError(null);
            successResponse.setData(vehicles);

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            RestResponse<List<UserDashboardDTO.VehicleInfo>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setMessage("Lỗi server: " + e.getMessage());
            errorResponse.setError("INTERNAL_SERVER_ERROR");
            errorResponse.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * API lấy lịch sử đăng ký tháng của User (cần JWT token)
     */
    @GetMapping("/user/dang-ky-thang")
    public ResponseEntity<RestResponse<List<UserDashboardDTO.DangKyThangInfo>>> getUserDangKyThangHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                RestResponse<List<UserDashboardDTO.DangKyThangInfo>> errorResponse = new RestResponse<>();
                errorResponse.setStatusCode(401);
                errorResponse.setMessage("Vui lòng đăng nhập để xem lịch sử đăng ký");
                errorResponse.setError("UNAUTHORIZED");
                errorResponse.setData(null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String email = authentication.getName();
            List<UserDashboardDTO.DangKyThangInfo> dangKyHistory = userDashboardService
                    .getUserDangKyThangHistory(email);

            RestResponse<List<UserDashboardDTO.DangKyThangInfo>> successResponse = new RestResponse<>();
            successResponse.setStatusCode(200);
            successResponse.setMessage("Lấy lịch sử đăng ký tháng thành công");
            successResponse.setError(null);
            successResponse.setData(dangKyHistory);

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            RestResponse<List<UserDashboardDTO.DangKyThangInfo>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setMessage("Lỗi server: " + e.getMessage());
            errorResponse.setError("INTERNAL_SERVER_ERROR");
            errorResponse.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
