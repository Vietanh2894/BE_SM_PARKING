package nckh.felix.StupidParking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.domain.dto.UserLoginDTO;
import nckh.felix.StupidParking.repository.UserRepository;

@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Xác thực User bằng email và password
     * 
     * @param userLoginDTO Thông tin đăng nhập của user
     * @return User nếu xác thực thành công, null nếu thất bại
     */
    public User authenticateUser(UserLoginDTO userLoginDTO) {
        // Tìm user theo email
        User user = userRepository.findByEmail(userLoginDTO.getEmail());

        if (user == null) {
            return null; // Email không tồn tại
        }

        // Kiểm tra password
        if (passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            return user; // Xác thực thành công
        }

        return null; // Password không đúng
    }

    /**
     * Kiểm tra email có tồn tại không
     * 
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
