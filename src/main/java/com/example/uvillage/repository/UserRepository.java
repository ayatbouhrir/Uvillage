package com.example.uvillage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.uvillage.models.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

