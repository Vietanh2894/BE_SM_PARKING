package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.Account;
import nckh.felix.StupidParking.domain.Role;
import nckh.felix.StupidParking.domain.Account.TrangThaiAccount;
import nckh.felix.StupidParking.repository.AccountRepository;

@Service
public class AccountService {
    private AccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Account handleCreateAccount(Account account) {
        // Mã hóa mật khẩu trước khi lưu
        String hashPassword = this.passwordEncoder.encode(account.getPassword());
        account.setPassword(hashPassword);
        return this.accountRepository.save(account);
    }

    public Account fetchAccountByUsername(String username) {
        Optional<Account> accountOptional = this.accountRepository.findById(username);
        if (accountOptional.isPresent()) {
            return accountOptional.get();
        }
        return null;
    }

    public List<Account> fetchAllAccounts() {
        return this.accountRepository.findAll();
    }

    public Account handleUpdateAccount(Account reqAccount) {
        Account currentAccount = this.fetchAccountByUsername(reqAccount.getUsername());
        if (currentAccount != null) {
            // Mã hóa mật khẩu nếu có thay đổi
            if (!reqAccount.getPassword().equals(currentAccount.getPassword())) {
                String hashPassword = this.passwordEncoder.encode(reqAccount.getPassword());
                currentAccount.setPassword(hashPassword);
            }
            currentAccount.setTrangThai(reqAccount.getTrangThai());
            currentAccount.setRole(reqAccount.getRole());
            currentAccount = this.accountRepository.save(currentAccount);
        }
        return currentAccount;
    }

    public void handleDeleteAccount(String username) {
        this.accountRepository.deleteById(username);
    }

    // Business logic methods
    public List<Account> fetchAccountsByTrangThai(TrangThaiAccount trangThai) {
        return this.accountRepository.findByTrangThai(trangThai);
    }

    public List<Account> fetchAccountsByRole(Role role) {
        return this.accountRepository.findByRole(role);
    }

    public List<Account> fetchActiveAccountsByRole(Role role) {
        return this.accountRepository.findActiveAccountsByRole(role);
    }

    public Account handleEnableAccount(String username) {
        Account account = this.fetchAccountByUsername(username);
        if (account != null) {
            account.enableAccount();
            account = this.accountRepository.save(account);
        }
        return account;
    }

    public Account handleDisableAccount(String username) {
        Account account = this.fetchAccountByUsername(username);
        if (account != null) {
            account.disableAccount();
            account = this.accountRepository.save(account);
        }
        return account;
    }

    // Authentication method
    public Account authenticateAccount(String username, String password) {
        Account account = this.fetchAccountByUsername(username);
        if (account != null && account.isActive()) {
            if (this.passwordEncoder.matches(password, account.getPassword())) {
                return account;
            }
        }
        return null;
    }

    // Statistics methods
    public Long countAccountsByTrangThai(TrangThaiAccount trangThai) {
        return this.accountRepository.countByTrangThai(trangThai);
    }

    public Account handleGetAccountByUsername(String username) {
        return this.fetchAccountByUsername(username);
    }
}
