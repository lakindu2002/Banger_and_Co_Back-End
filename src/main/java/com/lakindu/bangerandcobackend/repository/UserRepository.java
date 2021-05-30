package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//annotate with Repository so it is discovered during component scanning
public interface UserRepository extends JpaRepository<User, String> {
    User findUserByEmailAddress(String emailAddress);

    User findUserByUsername(String username);
}
