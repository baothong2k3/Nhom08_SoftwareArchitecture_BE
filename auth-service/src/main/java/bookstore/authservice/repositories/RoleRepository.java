package bookstore.authservice.repositories;

import bookstore.authservice.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "role", path = "role")
public interface RoleRepository extends JpaRepository<Role, Long> {
    public Role findRoleByName(String name);
}
