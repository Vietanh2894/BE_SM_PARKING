package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User handleCreateUser(User user) {
        // Validation: kiểm tra email, CCCD, SDT đã tồn tại chưa
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
        }
        if (user.getCccd() != null && userRepository.existsByCccd(user.getCccd())) {
            throw new IllegalArgumentException("CCCD đã tồn tại trong hệ thống");
        }
        if (user.getSdt() != null && userRepository.existsBySdt(user.getSdt())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống");
        }

        return userRepository.save(user);
    }

    public User fetchUserById(long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null);
    }

    public User fetchUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User fetchUserByCccd(String cccd) {
        return userRepository.findByCccd(cccd);
    }

    public User fetchUserBySdt(String sdt) {
        return userRepository.findBySdt(sdt);
    }

    public List<User> fetchAllUsers() {
        return userRepository.findAll();
    }

    public User handleUpdateUser(User reqUser) {
        User currentUser = fetchUserById(reqUser.getId());
        if (currentUser != null) {
            // Validation: kiểm tra email, CCCD, SDT đã tồn tại chưa (trừ chính nó)
            if (reqUser.getEmail() != null && !reqUser.getEmail().equals(currentUser.getEmail())) {
                if (userRepository.existsByEmail(reqUser.getEmail())) {
                    throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
                }
            }
            if (reqUser.getCccd() != null && !reqUser.getCccd().equals(currentUser.getCccd())) {
                if (userRepository.existsByCccd(reqUser.getCccd())) {
                    throw new IllegalArgumentException("CCCD đã tồn tại trong hệ thống");
                }
            }
            if (reqUser.getSdt() != null && !reqUser.getSdt().equals(currentUser.getSdt())) {
                if (userRepository.existsBySdt(reqUser.getSdt())) {
                    throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống");
                }
            }

            currentUser.setName(reqUser.getName());
            currentUser.setEmail(reqUser.getEmail());
            currentUser.setCccd(reqUser.getCccd());
            currentUser.setSdt(reqUser.getSdt());
            currentUser.setDiaChi(reqUser.getDiaChi());

            // Chỉ cập nhật password nếu có thay đổi
            if (reqUser.getPassword() != null && !reqUser.getPassword().isEmpty()) {
                String hashPassword = passwordEncoder.encode(reqUser.getPassword());
                currentUser.setPassword(hashPassword);
            }

            currentUser = userRepository.save(currentUser);
        }
        return currentUser;
    }

    public void handleDeleteUser(long id) {
        userRepository.deleteById(id);
    }

    public User handleGetUserByUsername(String username) {
        return userRepository.findByEmail(username);
    }

    /**
     * Tạo hoặc cập nhật User từ thông tin đăng ký tháng
     */
    public User createOrUpdateUserFromMonthlyRegistration(String cccd, String name, String diaChi, String sdt) {
        return createOrUpdateUserFromMonthlyRegistration(cccd, name, diaChi, sdt, null, null);
    }

    /**
     * Tạo hoặc cập nhật User từ thông tin đăng ký tháng với email và password tùy
     * chọn
     */
    public User createOrUpdateUserFromMonthlyRegistration(String cccd, String name, String diaChi,
            String sdt, String email, String password) {
        // Tìm user theo CCCD trước
        User existingUser = fetchUserByCccd(cccd);

        if (existingUser != null) {
            // Cập nhật thông tin nếu cần
            boolean needUpdate = false;

            if (name != null && !name.equals(existingUser.getName())) {
                existingUser.setName(name);
                needUpdate = true;
            }
            if (diaChi != null && !diaChi.equals(existingUser.getDiaChi())) {
                existingUser.setDiaChi(diaChi);
                needUpdate = true;
            }
            if (sdt != null && !sdt.equals(existingUser.getSdt())) {
                // Kiểm tra số điện thoại có bị trùng không
                if (!userRepository.existsBySdt(sdt)) {
                    existingUser.setSdt(sdt);
                    needUpdate = true;
                }
            }
            if (email != null && !email.trim().isEmpty() && !email.equals(existingUser.getEmail())) {
                // Kiểm tra email có bị trùng không
                if (!userRepository.existsByEmail(email)) {
                    existingUser.setEmail(email);
                    needUpdate = true;
                }
            }
            if (password != null && !password.trim().isEmpty()) {
                // Cập nhật password mới
                String hashPassword = passwordEncoder.encode(password);
                existingUser.setPassword(hashPassword);
                needUpdate = true;
            }

            if (needUpdate) {
                return userRepository.save(existingUser);
            }
            return existingUser;
        } else {
            // Tạo user mới
            User newUser = new User();
            newUser.setCccd(cccd);
            newUser.setName(name != null ? name : "User " + cccd.substring(8)); // Tên mặc định từ 4 số cuối CCCD
            newUser.setDiaChi(diaChi);
            newUser.setSdt(sdt);

            // Thiết lập email
            if (email != null && !email.trim().isEmpty()) {
                // Kiểm tra email có bị trùng không
                if (!userRepository.existsByEmail(email)) {
                    newUser.setEmail(email);
                } else {
                    // Nếu email bị trùng, tạo email mặc định
                    String defaultEmail = "user" + cccd + "@parking.system";
                    newUser.setEmail(defaultEmail);
                }
            } else {
                // Tạo email mặc định từ CCCD
                String defaultEmail = "user" + cccd + "@parking.system";
                newUser.setEmail(defaultEmail);
            }

            // Thiết lập password
            if (password != null && !password.trim().isEmpty()) {
                String hashPassword = passwordEncoder.encode(password);
                newUser.setPassword(hashPassword);
            } else {
                // Tạo password mặc định từ CCCD
                String defaultPassword = cccd.substring(8) + "2024"; // 4 số cuối CCCD + năm
                String hashPassword = passwordEncoder.encode(defaultPassword);
                newUser.setPassword(hashPassword);
            }

            return userRepository.save(newUser);
        }
    }
}
