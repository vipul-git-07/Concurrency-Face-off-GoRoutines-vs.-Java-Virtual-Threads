package org.vatsuvaksi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vatsuvaksi.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
