package mas.vetclinic.service;

import mas.vetclinic.model.entity.person.Shelter;
import mas.vetclinic.repository.ShelterRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ShelterService {
    public static final int SEARCH_LIMIT = 10;

    private final ShelterRepository shelterRepository;

    public ShelterService(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    @Transactional
    public Shelter createShelter(String name, String emailAddress, Set<String> phoneNumbers) {
        Set<String> phoneNumbersSet = phoneNumbers.stream()
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.toSet());
        for (String phoneNumber : phoneNumbersSet) {
            if (shelterRepository.existsByPhoneNumbersContaining(phoneNumber)) {
                throw new IllegalArgumentException("Phone number " + phoneNumber + " is already in use");
            }
        }
        Shelter shelter = new Shelter(name, emailAddress, phoneNumbersSet);
        return shelterRepository.save(shelter);
    }

    public List<Shelter> searchByNameOrPhoneNumber(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String trimmed = query.trim();

        if (trimmed.matches("\\+?\\d+")) {
            if (!trimmed.startsWith("+")) {
                trimmed = "+" + trimmed;
            }
            return shelterRepository.findByPhoneNumber(trimmed);
        }

        Limit limit = Limit.of(SEARCH_LIMIT);
        return shelterRepository.findByNameContainingIgnoreCase(trimmed, limit);
    }

    public Optional<Shelter> findById(Long id) {
        return shelterRepository.findById(id);
    }
}
