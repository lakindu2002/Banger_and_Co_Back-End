package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.entity.Role;

public interface RoleService {
    //internal method to get the user role info when creating an account
    Role getRoleInformation(String roleName);
}
