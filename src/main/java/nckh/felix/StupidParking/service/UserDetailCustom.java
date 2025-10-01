package nckh.felix.StupidParking.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import org.springframework.security.core.userdetails.User;

@Component("userDetailsService")
public class UserDetailCustom implements UserDetailsService {
    private final AccountService accountService;
    private final StaffService staffService;

    public UserDetailCustom(AccountService accountService, StaffService staffService) {
        this.accountService = accountService;
        this.staffService = staffService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Kiểm tra xem có phải là email không (cho User) hay username (cho Staff)
        if (username.contains("@")) {
            // Email - có thể là User nhưng Staff authentication vẫn dùng username
            // Trong trường hợp này, chỉ xử lý Staff authentication qua username
            throw new UsernameNotFoundException(
                    "Staff authentication chỉ dùng username, không phải email: " + username);
        } else {
            // Username - xử lý Staff authentication
            nckh.felix.StupidParking.domain.Account account = this.accountService.fetchAccountByUsername(username);
            if (account != null && account.isActive()) {
                // Tìm thấy Staff account và đang active
                nckh.felix.StupidParking.domain.Staff staff = this.staffService.fetchStaffByAccount(account);
                if (staff != null) {
                    String role = "ROLE_" + staff.getChucVu().name(); // ROLE_ADMIN hoặc ROLE_BAO_VE
                    return new org.springframework.security.core.userdetails.User(
                            account.getUsername(),
                            account.getPassword(),
                            Collections.singletonList(new SimpleGrantedAuthority(role)));
                }
            }
        }

        throw new UsernameNotFoundException("Không tìm thấy Staff account với username: " + username);
    }
}
