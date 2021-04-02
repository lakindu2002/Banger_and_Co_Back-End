package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
//annotate with Repository so it is discovered during component scanning
public interface UserRepository extends CrudRepository<User, String> {
    User findUserByEmailAddress(String emailAddress);
}
