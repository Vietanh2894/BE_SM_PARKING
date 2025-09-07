package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByCccd(String cccd);

    User findBySdt(String sdt);

    boolean existsByEmail(String email);

    boolean existsByCccd(String cccd);

    boolean existsBySdt(String sdt);
}
