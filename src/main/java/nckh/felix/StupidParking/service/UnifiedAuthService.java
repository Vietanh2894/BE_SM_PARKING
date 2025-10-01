package nckh.felix.StupidParking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.Account;
import nckh.felix.StupidParking.domain.dto.UnifiedLoginDTO;
import nckh.felix.StupidParking.repository.UserRepository;

/**
 * Service thống nhất để xử lý authentication cho cả User và Staff
 */
@Service
public class UnifiedAuthService {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final StaffService staffService;
    private final PasswordEncoder passwordEncoder;

    public UnifiedAuthService(UserRepository userRepository,
            AccountService accountService,
            StaffService staffService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.staffService = staffService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Xác thực thông tin đăng nhập thống nhất
     * 
     * @param loginDTO Thông tin đăng nhập (có thể là email hoặc username)
     * @return AuthResult chứa thông tin loại user và đối tượng tương ứng
     */
    public AuthResult authenticate(UnifiedLoginDTO loginDTO) {
        if (loginDTO.isEmail()) {
            // Thử authenticate User trước (dùng email)
            User user = authenticateUser(loginDTO.getCredential(), loginDTO.getPassword());
            if (user != null) {
                return new AuthResult("USER", user, null);
            }
        } else {
            // Thử authenticate Staff (dùng username)
            Staff staff = authenticateStaff(loginDTO.getCredential(), loginDTO.getPassword());
            if (staff != null) {
                return new AuthResult("STAFF", null, staff);
            }
        }

        return null; // Authentication failed
    }

    /**
     * Xác thực User bằng email và password
     */
    private User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    /**
     * Xác thực Staff bằng username và password
     */
    private Staff authenticateStaff(String username, String password) {
        Account account = accountService.fetchAccountByUsername(username);
        if (account == null || !account.isActive()) {
            return null;
        }

        if (passwordEncoder.matches(password, account.getPassword())) {
            Staff staff = staffService.fetchStaffByAccount(account);
            return staff;
        }

        return null;
    }

    /**
     * Lớp kết quả authentication
     */
    public static class AuthResult {
        private String userType; // "USER" hoặc "STAFF"
        private User user;
        private Staff staff;

        public AuthResult(String userType, User user, Staff staff) {
            this.userType = userType;
            this.user = user;
            this.staff = staff;
        }

        public String getUserType() {
            return userType;
        }

        public User getUser() {
            return user;
        }

        public Staff getStaff() {
            return staff;
        }

        public boolean isUser() {
            return "USER".equals(userType);
        }

        public boolean isStaff() {
            return "STAFF".equals(userType);
        }
    }
}