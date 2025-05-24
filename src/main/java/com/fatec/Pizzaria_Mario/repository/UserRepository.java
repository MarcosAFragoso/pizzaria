package com.fatec.Pizzaria_Mario.repository;

import com.fatec.Pizzaria_Mario.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
}