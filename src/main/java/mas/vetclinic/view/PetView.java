package mas.vetclinic.view;

import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.model.entity.pet.PetGender;

public record PetView(Long id, String name, String speciesName, PetGender gender,
                      String chipNumber, String registrationNumber) {
    public static PetView of(Pet pet) {
        return new PetView(pet.getId(), pet.getName(), pet.getSpecies().getName(),
                pet.getGender(), pet.getChipNumber(), pet.getRegistrationNumber());
    }
}