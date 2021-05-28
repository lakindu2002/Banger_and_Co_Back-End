package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;

public interface RoleService {
    //internal method to get the user role info when creating an account
    Role _getRoleInformation(String roleName) throws ResourceNotFoundException;
}
