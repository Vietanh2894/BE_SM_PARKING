package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.Role;
import nckh.felix.StupidParking.service.RoleService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    public ResponseEntity<Role> createNewRole(@RequestBody Role xRole) {
        Role vRole = this.roleService.handleCreateRole(xRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(vRole);
    }

    @GetMapping("/roles/{roleId}")
    public ResponseEntity<Role> getRoleByRoleId(@PathVariable("roleId") String roleId) {
        Role vRole = this.roleService.fetchRoleByRoleId(roleId);
        return ResponseEntity.status(HttpStatus.OK).body(vRole);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.status(HttpStatus.OK).body(this.roleService.fetchAllRoles());
    }

    @PutMapping("/roles")
    public ResponseEntity<Role> updateRole(@RequestBody Role xRole) {
        Role vRole = this.roleService.handleUpdateRole(xRole);
        return ResponseEntity.ok(vRole);
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<String> deleteRole(@PathVariable("roleId") String roleId) throws IdInvalidException {
        this.roleService.handleDeleteRole(roleId);
        return ResponseEntity.ok("Xóa role thành công");
    }

    @GetMapping("/roles/name/{roleName}")
    public ResponseEntity<Role> getRoleByRoleName(@PathVariable("roleName") String roleName) {
        Role vRole = this.roleService.fetchRoleByRoleName(roleName);
        return ResponseEntity.status(HttpStatus.OK).body(vRole);
    }
}
