package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Optional<User> findById(long id);

    User findByEmail(String email);
}
