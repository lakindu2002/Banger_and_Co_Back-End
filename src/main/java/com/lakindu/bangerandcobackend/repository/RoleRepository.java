package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//denote interface as a Repository for it to be detected during component scan
public interface RoleRepository extends JpaRepository<Role, Integer> {

    //define query via JPA. Auto Constructs the query.
    Role findRoleByRoleName(String roleName);
}
