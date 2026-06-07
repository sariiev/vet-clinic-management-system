package mas.vetclinic.repository;

import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.PersonRole;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    @Query("""
        SELECT p FROM Person p
        WHERE :role MEMBER OF p.roles
        AND ((LOWER(p.firstName) LIKE LOWER(:single)
            OR LOWER(p.lastName) LIKE LOWER(:single)))
    """)
    List<Person> searchBySinglePartOfNameAndRole(@Param(value = "single") String single,
                                                 @Param(value = "role") PersonRole role,
                                                 Limit limit);

    @Query("""
        SELECT p FROM Person p
        WHERE :role MEMBER OF p.roles
        AND (((LOWER(p.firstName) LIKE LOWER(:first)) AND (LOWER(p.lastName) LIKE LOWER(:last)))
            OR ((LOWER(p.firstName) LIKE LOWER(:last)) AND (LOWER(p.lastName) LIKE LOWER(:first))))
    """)
    List<Person> searchByFullNameAndRole(@Param(value = "first") String first,
                                         @Param(value = "last") String last,
                                         @Param(value = "role") PersonRole role,
                                         Limit limit);

    boolean existsByPhoneNumberEquals(String phoneNumber);
    Optional<Person> findByPhoneNumber(String phoneNumber);
}
