package bookstore.authservice.repositories;

import bookstore.authservice.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "account", path = "account")
public interface AccountRepository extends JpaRepository<Account, Long> {
    //Tìm tài khoản theo tên đăng nhập
    Optional<Account> findByEmail(String email);

    //Tìm tài khoản theo tên đăng nhập
    Optional<Account> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

}
