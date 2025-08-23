package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.service.UserService;
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

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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

}
