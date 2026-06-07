package mas.vetclinic.repository;

import mas.vetclinic.model.entity.person.Shelter;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    boolean existsByPhoneNumbersContaining(String phoneNumber);
    List<Shelter> findByNameContainingIgnoreCase(String name, Limit limit);
    @Query("SELECT s FROM Shelter s WHERE :phoneNumber MEMBER OF s.phoneNumbers")
    List<Shelter> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
