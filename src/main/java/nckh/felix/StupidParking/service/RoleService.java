package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.Role;
import nckh.felix.StupidParking.repository.RoleRepository;

@Service
public class RoleService {
    private RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role handleCreateRole(Role role) {
        return this.roleRepository.save(role);
    }

    public Role fetchRoleByRoleId(String roleId) {
        Optional<Role> roleOptional = this.roleRepository.findById(roleId);
        if (roleOptional.isPresent()) {
            return roleOptional.get();
        }
        return null;
    }

    public List<Role> fetchAllRoles() {
        return this.roleRepository.findAll();
    }

    public Role handleUpdateRole(Role reqRole) {
        Role currentRole = this.fetchRoleByRoleId(reqRole.getRoleId());
        if (currentRole != null) {
            currentRole.setRoleName(reqRole.getRoleName());
            currentRole = this.roleRepository.save(currentRole);
        }
        return currentRole;
    }

    public void handleDeleteRole(String roleId) {
        this.roleRepository.deleteById(roleId);
    }

    // Business logic methods
    public Role fetchRoleByRoleName(String roleName) {
        return this.roleRepository.findByRoleName(roleName);
    }
}
