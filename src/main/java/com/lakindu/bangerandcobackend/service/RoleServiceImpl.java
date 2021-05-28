package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.repository.RoleRepository;
import com.lakindu.bangerandcobackend.serviceinterface.RoleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
//creating a service class to interact with the persistence Layer of Role
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    //constructor injection
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role _getRoleInformation(String roleName) throws ResourceNotFoundException {
        //call the method in the repository that queries the database
        final Role theRole = roleRepository.findRoleByRoleName(roleName);
        if (theRole != null) {
            //if role is a valid object return the object
            return theRole;
        } else {
            //throw an exception
            throw new ResourceNotFoundException("The role does not exist at Banger and Co");
        }
    }
}
