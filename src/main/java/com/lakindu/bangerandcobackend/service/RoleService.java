package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
//creating a service class to interact with the persistence Layer of Role
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    //constructor injection
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleInformation(String roleName) {
        //call the method in the repository that queries the database
        final Role theRole = roleRepository.findRoleByRoleName(roleName);
        if (theRole != null) {
            //if role is a valid object return the object
            return theRole;
        } else {
            //else return null
            return null;
        }
    }
}
