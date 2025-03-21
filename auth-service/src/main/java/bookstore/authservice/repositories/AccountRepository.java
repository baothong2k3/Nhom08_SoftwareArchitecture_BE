package bookstore.authservice.repositories;

import bookstore.authservice.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "account", path = "account")
public interface AccountRepository extends JpaRepository<Account, Long> {
    public boolean existsAccountByUserName(String username);
    public boolean existsAccountByEmail(String email);
    public Account findAccountByUserName(String username);
    public Account findAccountByEmail(String email);
}
