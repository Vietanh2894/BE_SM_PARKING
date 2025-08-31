package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.Account;
import nckh.felix.StupidParking.domain.Role;
import nckh.felix.StupidParking.domain.Account.TrangThaiAccount;
import nckh.felix.StupidParking.domain.dto.AuthenticateDTO;
import nckh.felix.StupidParking.service.AccountService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createNewAccount(@RequestBody Account xAccount) {
        Account vAccount = this.accountService.handleCreateAccount(xAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(vAccount);
    }

    @GetMapping("/accounts/{username}")
    public ResponseEntity<Account> getAccountByUsername(@PathVariable("username") String username) {
        Account vAccount = this.accountService.fetchAccountByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(vAccount);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.status(HttpStatus.OK).body(this.accountService.fetchAllAccounts());
    }

    @PutMapping("/accounts")
    public ResponseEntity<Account> updateAccount(@RequestBody Account xAccount) {
        Account vAccount = this.accountService.handleUpdateAccount(xAccount);
        return ResponseEntity.ok(vAccount);
    }

    @DeleteMapping("/accounts/{username}")
    public ResponseEntity<String> deleteAccount(@PathVariable("username") String username) throws IdInvalidException {
        this.accountService.handleDeleteAccount(username);
        return ResponseEntity.ok("Xóa account thành công");
    }

    // Business logic endpoints
    @GetMapping("/accounts/status/{status}")
    public ResponseEntity<List<Account>> getAccountsByStatus(@PathVariable("status") String status) {
        TrangThaiAccount trangThai = TrangThaiAccount.valueOf(status.toUpperCase());
        return ResponseEntity.ok(this.accountService.fetchAccountsByTrangThai(trangThai));
    }

    @GetMapping("/accounts/role/{roleId}")
    public ResponseEntity<List<Account>> getAccountsByRole(@PathVariable("roleId") String roleId) {
        Role role = new Role(roleId);
        return ResponseEntity.ok(this.accountService.fetchAccountsByRole(role));
    }

    @GetMapping("/accounts/active/role/{roleId}")
    public ResponseEntity<List<Account>> getActiveAccountsByRole(@PathVariable("roleId") String roleId) {
        Role role = new Role(roleId);
        return ResponseEntity.ok(this.accountService.fetchActiveAccountsByRole(role));
    }

    @PostMapping("/accounts/{username}/enable")
    public ResponseEntity<Account> enableAccount(@PathVariable("username") String username) {
        Account updatedAccount = this.accountService.handleEnableAccount(username);
        if (updatedAccount != null) {
            return ResponseEntity.ok(updatedAccount);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/accounts/{username}/disable")
    public ResponseEntity<Account> disableAccount(@PathVariable("username") String username) {
        Account updatedAccount = this.accountService.handleDisableAccount(username);
        if (updatedAccount != null) {
            return ResponseEntity.ok(updatedAccount);
        }
        return ResponseEntity.notFound().build();
    }

    // Authentication endpoint
    @PostMapping("/accounts/authenticate")
    public ResponseEntity<Account> authenticateAccount(@RequestBody AuthenticateDTO authDTO) {
        Account authenticatedAccount = this.accountService.authenticateAccount(authDTO.getUsername(),
                authDTO.getPassword());
        if (authenticatedAccount != null) {
            return ResponseEntity.ok(authenticatedAccount);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Statistics endpoints
    @GetMapping("/accounts/count/status/{status}")
    public ResponseEntity<Long> countAccountsByStatus(@PathVariable("status") String status) {
        TrangThaiAccount trangThai = TrangThaiAccount.valueOf(status.toUpperCase());
        return ResponseEntity.ok(this.accountService.countAccountsByTrangThai(trangThai));
    }
}
