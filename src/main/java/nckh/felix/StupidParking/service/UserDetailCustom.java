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
        // CHỈ CHO PHÉP STAFF LOGIN - KHÔNG CHO USER LOGIN
        // Tìm Staff account theo username
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

        // KHÔNG CHO PHÉP USER LOGIN - CHỈ STAFF MỚI ĐƯỢC PHÉP
        // Bỏ phần code cho User login để chỉ Staff được login

        throw new UsernameNotFoundException("Chỉ tài khoản Staff mới được phép đăng nhập. Username: " + username);
    }
}
