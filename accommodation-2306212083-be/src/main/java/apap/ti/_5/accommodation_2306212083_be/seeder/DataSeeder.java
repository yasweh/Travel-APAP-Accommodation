package apap.ti._5.accommodation_2306212083_be.seeder;

import apap.ti._5.accommodation_2306212083_be.model.Property;
import apap.ti._5.accommodation_2306212083_be.model.Room;
import apap.ti._5.accommodation_2306212083_be.model.RoomType;
import apap.ti._5.accommodation_2306212083_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306212083_be.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (propertyRepository.count() > 0) {
            log.info("Database already seeded. Skipping seeder.");
            return;
        }

        log.info("Starting database seeding...");
        
        String[] propertyNames = {
            "Grand Hotel Jakarta",
            "Bali Beach Resort",
            "Surabaya Plaza Hotel",
            "Villa Bandung Hills",
            "Apartemen Jakarta Pusat",
            "Hotel Yogyakarta Heritage",
            "Resort Lombok Paradise",
            "Villa Puncak Mountain",
            "Hotel Semarang Business",
            "Apartemen Surabaya View"
        };

        String[] addresses = {
            "Jl. M.H. Thamrin No. 1, Jakarta Pusat",
            "Jl. Pantai Kuta No. 100, Bali",
            "Jl. Basuki Rahmat No. 50, Surabaya",
            "Jl. Dago Pakar No. 25, Bandung",
            "Jl. Sudirman Kav. 52-53, Jakarta Selatan",
            "Jl. Malioboro No. 15, Yogyakarta",
            "Jl. Senggigi Beach, Lombok",
            "Jl. Raya Puncak KM 77, Bogor",
            "Jl. Pemuda No. 118, Semarang",
            "Jl. HR Muhammad No. 10, Surabaya"
        };

        Integer[] types = {0, 0, 0, 1, 2, 0, 0, 1, 0, 2}; // 0=Hotel, 1=Villa, 2=Apartemen
        Integer[] provinces = {0, 4, 3, 1, 0, 2, 4, 1, 2, 3}; // Province codes

        for (int i = 0; i < 10; i++) {
            // Create Property
            Property property = Property.builder()
                .propertyId("PROP-" + String.format("%04d", i + 1))
                .propertyName(propertyNames[i])
                .type(types[i])
                .address(addresses[i])
                .province(provinces[i])
                .description("Premium accommodation with excellent facilities and services")
                .totalRoom(5)
                .activeStatus(1)
                .activeRoom(5)
                .income(0)
                .ownerName("Owner " + (i + 1))
                .ownerId(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .listRoomType(new ArrayList<>())
                .build();

            // Save property first
            property = propertyRepository.save(property);
            log.info("Created property: {}", property.getPropertyName());

            // Create 2 Room Types for this property
            String[] roomTypeNames = {"Deluxe Room", "Standard Room"};
            int[] roomTypePrices = {1000000 + (i * 100000), 500000 + (i * 50000)};
            int[] roomTypeCapacities = {2, 2};
            String[] roomTypeFacilities = {
                "AC, TV, WiFi, Mini Bar, Hot Water, Bathtub",
                "AC, TV, WiFi, Hot Water"
            };
            int[] roomTypeFloors = {2, 2}; // Both on floor 2
            int[] roomsPerType = {3, 2}; // 3 rooms for Deluxe, 2 for Standard
            
            // Track room numbering per floor
            Map<Integer, Integer> floorRoomCounter = new HashMap<>();
            
            for (int rt = 0; rt < 2; rt++) {
                RoomType roomType = RoomType.builder()
                    .roomTypeId(property.getPropertyId() + "-RT-" + String.format("%03d", rt + 1))
                    .name(roomTypeNames[rt])
                    .price(roomTypePrices[rt])
                    .description("Comfortable " + roomTypeNames[rt].toLowerCase() + " with great amenities")
                    .capacity(roomTypeCapacities[rt])
                    .facility(roomTypeFacilities[rt])
                    .floor(roomTypeFloors[rt])
                    .property(property)
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .listRoom(new ArrayList<>())
                    .build();

                roomType = roomTypeRepository.save(roomType);
                log.info("Created room type: {} for {}", roomType.getName(), property.getPropertyName());

                // Initialize counter for this floor if not exists
                Integer floor = roomTypeFloors[rt];
                if (!floorRoomCounter.containsKey(floor)) {
                    floorRoomCounter.put(floor, 1);
                }

                // Create rooms for this room type with floor-based numbering
                int numRooms = roomsPerType[rt];
                for (int j = 0; j < numRooms; j++) {
                    // Get current room number for this floor
                    int roomNumber = floorRoomCounter.get(floor);
                    
                    // Generate room name: FloorXX (e.g., 201, 202, 203, 204, 205)
                    String roomName = String.format("%d%02d", floor, roomNumber);
                    
                    Room room = Room.builder()
                        .roomId(property.getPropertyId() + "-ROOM-" + roomName)
                        .name(roomName)
                        .availabilityStatus(1) // Available
                        .activeRoom(1) // Active
                        .roomType(roomType)
                        .createdDate(LocalDateTime.now())
                        .updatedDate(LocalDateTime.now())
                        .build();

                    roomRepository.save(room);
                    
                    // Increment counter for this floor
                    floorRoomCounter.put(floor, roomNumber + 1);
                }
            }
            log.info("Created 5 rooms (2 room types) for property: {}", property.getPropertyName());
        }

        log.info("Database seeding completed! Created 10 properties with 50 total rooms (5 rooms per property, 2 room types each).");
    }
}
