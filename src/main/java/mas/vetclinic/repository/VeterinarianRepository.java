package mas.vetclinic.repository;

import mas.vetclinic.model.entity.person.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {
    @Query("""
        SELECT v FROM Veterinarian v
        WHERE (LOWER(v.firstName) LIKE LOWER(:single)
            OR LOWER(v.lastName) LIKE LOWER(:single))
    """)
    List<Veterinarian> searchBySinglePartOfName(@Param(value = "single") String single);

    @Query("""
        SELECT v FROM Veterinarian v
        WHERE ((LOWER(v.firstName) LIKE LOWER(:first)) AND (LOWER(v.lastName) LIKE LOWER(:last)))
            OR ((LOWER(v.firstName) LIKE LOWER(:last)) AND (LOWER(v.lastName) LIKE LOWER(:first)))
    """)
    List<Veterinarian> searchByFullName(@Param(value = "first") String first,
                                  @Param(value = "last") String last);
}
