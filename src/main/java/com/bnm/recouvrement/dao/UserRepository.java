package com.bnm.recouvrement.dao;

import com.bnm.recouvrement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    List<User> findByUserType(String userType);
    List<User> findByUserTypeIn(List<String> userTypes);
    
    /**
     * Trouve tous les utilisateurs ayant un rôle spécifique
     * @param roleName le nom du rôle recherché
     * @return la liste des utilisateurs ayant ce rôle
     */
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoles_Name(@Param("roleName") String roleName);
}
