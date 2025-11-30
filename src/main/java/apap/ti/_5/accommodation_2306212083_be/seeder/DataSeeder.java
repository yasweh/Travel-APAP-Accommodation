package apap.ti._5.accommodation_2306212083_be.seeder;

import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.*;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import apap.ti._5.accommodation_2306212083_be.enums.ServiceSource;
import apap.ti._5.accommodation_2306212083_be.enums.TicketStatus;
import apap.ti._5.accommodation_2306212083_be.enums.SenderType;
import apap.ti._5.accommodation_2306212083_be.enums.ActionType;
import net.datafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final AccommodationReviewRepository reviewRepository;
    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final SupportProgressRepository progressRepository;
    private final IdGenerator idGenerator;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("DataSeeder DISABLED - Skipping database seeding to allow manual data entry with authentication");
        // Seeder is disabled because user IDs and property ownership must match authenticated users
        return;
        
        /* SEEDER CODE DISABLED
        log.info("Starting comprehensive database seeding...");

        // Clear all existing data to ensure fresh seeding on every startup
        clearAllData();

        createCustomers();
        createPropertiesAndRooms();
        createBookingsWithEdgeCases();
        createMaintenanceSchedules();
        createReviews();
        createSupportTickets();

        log.info("Database seeding completed successfully!");
    }

    private void clearAllData() {
        log.info("Clearing existing database data...");
        progressRepository.deleteAll();
        messageRepository.deleteAll();
        ticketRepository.deleteAll();
        reviewRepository.deleteAll();
        maintenanceRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        roomTypeRepository.deleteAll();
        propertyRepository.deleteAll();
        customerRepository.deleteAll();
        
        // Flush to ensure all DELETE operations are executed immediately
        progressRepository.flush();
        messageRepository.flush();
        ticketRepository.flush();
        reviewRepository.flush();
        maintenanceRepository.flush();
        bookingRepository.flush();
        roomRepository.flush();
        roomTypeRepository.flush();
        propertyRepository.flush();
        customerRepository.flush();
        
        log.info("All existing data cleared.");
    }

    private void createCustomers() {
        // Create John Doe with fixed UUID for frontend consistency
        Customer john = Customer.builder()
            .customerId(UUID.fromString("263d012e-b86a-4813-be96-41e6da78e00d"))
            .name("John Doe")
            .email("john@example.com")
            .phone("08123456789")
            .createdDate(LocalDateTime.now())
            .build();
        customerRepository.save(john);

        // Create other customers
        String[] names = {"Jane Smith", "Bob Wilson", "Alice Brown", "Charlie Davis"};
        String[] emails = {"jane@example.com", "bob@example.com", "alice@example.com", "charlie@example.com"};
        String[] phones = {"08234567890", "08345678901", "08456789012", "08567890123"};

        for (int i = 0; i < names.length; i++) {
            Customer customer = Customer.builder()
                .customerId(UUID.randomUUID())
                .name(names[i])
                .email(emails[i])
                .phone(phones[i])
                .createdDate(LocalDateTime.now())
                .build();
            customerRepository.save(customer);
        }
        log.info("Created 5 customers (John Doe + 4 others)");
    }

    private void createPropertiesAndRooms() {
        String[] propertyNames = {
            "King The Land", "Bali Beach Resort", "Surabaya Plaza Hotel",
            "Villa Masai", "Apartemen Taman Jasmine"
        };

        String[] addresses = {
            "Jl. M.H. Thamrin No. 1, Jakarta Pusat",
            "Jl. Pantai Kuta No. 100, Bali",
            "Jl. Basuki Rahmat No. 50, Surabaya",
            "Jl. Dago Pakar No. 25, Bandung",
            "Jl. Sudirman Kav. 52-53, Jakarta Selatan"
        };

        Integer[] types = {0, 0, 0, 1, 2}; // 0=Hotel, 1=Villa, 2=Apartemen
        Integer[] provinces = {0, 4, 3, 1, 0};

        for (int i = 0; i < 5; i++) {
            Property property = Property.builder()
                .propertyId("PROP-" + String.format("%04d", i + 1))
                .propertyName(propertyNames[i])
                .type(types[i])
                .address(addresses[i])
                .province(provinces[i])
                .description("Premium accommodation with excellent facilities")
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

            property = propertyRepository.save(property);
            log.info("Created property: {}", property.getPropertyName());

            // Create 2 Room Types per property
            String[] roomTypeNames = {"Suite", "Deluxe"};
            int[] prices = {1000000 + (i * 100000), 500000 + (i * 50000)};
            int[] capacities = {2, 2};
            String[] facilities = {"AC, TV, WiFi, Mini Bar, Bathtub", "AC, TV, WiFi"};
            int[] floors = {2, 1};
            int[] roomsPerType = {2, 3};

            Map<Integer, Integer> floorCounter = new HashMap<>();

            for (int rt = 0; rt < 2; rt++) {
                RoomType roomType = RoomType.builder()
                    .roomTypeId(property.getPropertyId() + "-RT-" + String.format("%03d", rt + 1))
                    .name(roomTypeNames[rt])
                    .price(prices[rt])
                    .description("Comfortable " + roomTypeNames[rt].toLowerCase())
                    .capacity(capacities[rt])
                    .facility(facilities[rt])
                    .floor(floors[rt])
                    .property(property)
                    .activeStatus(1)
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .listRoom(new ArrayList<>())
                    .build();

                roomType = roomTypeRepository.save(roomType);

                Integer floor = floors[rt];
                if (!floorCounter.containsKey(floor)) {
                    floorCounter.put(floor, 1);
                }

                for (int j = 0; j < roomsPerType[rt]; j++) {
                    int roomNumber = floorCounter.get(floor);
                    String roomName = String.format("%d%02d", floor, roomNumber);

                    Room room = Room.builder()
                        .roomId(property.getPropertyId() + "-ROOM-" + roomName)
                        .name(roomName)
                        .availabilityStatus(1)
                        .activeRoom(1)
                        .roomType(roomType)
                        .createdDate(LocalDateTime.now())
                        .updatedDate(LocalDateTime.now())
                        .build();

                    roomRepository.save(room);
                    floorCounter.put(floor, roomNumber + 1);
                }
            }
        }
        // Ensure all persisted rooms/roomTypes/properties are flushed to the DB
        roomRepository.flush();
        roomTypeRepository.flush();
        propertyRepository.flush();

        log.info("Created 5 properties with 25 total rooms");
    }

    private void createBookingsWithEdgeCases() {
        List<Customer> customers = customerRepository.findAll();
        List<Room> allRooms = roomRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        int bookingIndex = 0;

        // Scenario 1: CONFIRMED bookings that are DONE (checkout date passed) - eligible for reviews
        for (int i = 0; i < 5; i++) {
            Room room = allRooms.get(i);
            // Ensure we use a managed Room instance (avoid transient/unsaved references)
            room = roomRepository.findById(room.getRoomId()).orElse(room);
            Customer customer = customers.get(i % customers.size());

            LocalDateTime checkIn = now.minusDays(10).withHour(14).withMinute(0);
            LocalDateTime checkOut = now.minusDays(7).withHour(12).withMinute(0);
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            // Eager initialize RoomType within transaction before accessing price
            int roomPrice = room.getRoomType().getPrice();
            int totalPrice = roomPrice * (int)days;

            AccommodationBooking booking = AccommodationBooking.builder()
                .bookingId(idGenerator.generateBookingId(room.getRoomId()))
                .room(room)
                .customerId(customer.getCustomerId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .totalDays((int)days)
                .totalPrice(totalPrice)
                .capacity(room.getRoomType().getCapacity())
                .isBreakfast(i % 2 == 0)
                .status(1) // CONFIRMED (Payment Confirmed)
                .activeStatus(1)
                .extraPay(0)
                .refund(0)
                .createdDate(checkIn.minusDays(5))
                .updatedDate(checkOut)
                .build();

            bookingRepository.save(booking);
            bookingIndex++;
        }
        log.info("Created 5 CONFIRMED bookings with past checkout dates (eligible for reviews)");

        // Scenario 2: CONFIRMED bookings - currently active
        for (int i = 5; i < 8; i++) {
            Room room = allRooms.get(i);
            room = roomRepository.findById(room.getRoomId()).orElse(room);
            Customer customer = customers.get(i % customers.size());

            LocalDateTime checkIn = now.minusDays(1).withHour(14).withMinute(0);
            LocalDateTime checkOut = now.plusDays(2).withHour(12).withMinute(0);
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            int totalPrice = room.getRoomType().getPrice() * (int)days;

            AccommodationBooking booking = AccommodationBooking.builder()
                .bookingId(idGenerator.generateBookingId(room.getRoomId()))
                .room(room)
                .customerId(customer.getCustomerId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .totalDays((int)days)
                .totalPrice(totalPrice)
                .capacity(room.getRoomType().getCapacity())
                .isBreakfast(true)
                .status(1) // CONFIRMED
                .activeStatus(1)
                .extraPay(0)
                .refund(0)
                .createdDate(now.minusDays(2))
                .updatedDate(now.minusDays(1))
                .build();

            bookingRepository.save(booking);
            room.setAvailabilityStatus(0); // Booked
            roomRepository.save(room);
            bookingIndex++;
        }
        log.info("Created 3 CONFIRMED bookings (currently active)");

        // Scenario 3: CONFIRMED bookings - future reservations
        for (int i = 8; i < 10; i++) {
            Room room = allRooms.get(i);
            room = roomRepository.findById(room.getRoomId()).orElse(room);
            Customer customer = customers.get(i % customers.size());

            LocalDateTime checkIn = now.plusDays(5).withHour(14).withMinute(0);
            LocalDateTime checkOut = now.plusDays(8).withHour(12).withMinute(0);
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            int totalPrice = room.getRoomType().getPrice() * (int)days;

            AccommodationBooking booking = AccommodationBooking.builder()
                .bookingId(idGenerator.generateBookingId(room.getRoomId()))
                .room(room)
                .customerId(customer.getCustomerId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .totalDays((int)days)
                .totalPrice(totalPrice)
                .capacity(room.getRoomType().getCapacity())
                .isBreakfast(false)
                .status(1) // CONFIRMED
                .activeStatus(1)
                .extraPay(0)
                .refund(0)
                .createdDate(now.minusHours(10))
                .updatedDate(now.minusHours(10))
                .build();

            bookingRepository.save(booking);
            bookingIndex++;
        }
        log.info("Created 2 CONFIRMED bookings (future reservations)");

        // Scenario 4: WAITING bookings (pending payment)
        for (int i = 10; i < 12; i++) {
            Room room = allRooms.get(i);
            room = roomRepository.findById(room.getRoomId()).orElse(room);
            Customer customer = customers.get(i % customers.size());

            LocalDateTime checkIn = now.plusDays(10).withHour(14).withMinute(0);
            LocalDateTime checkOut = now.plusDays(13).withHour(12).withMinute(0);
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            int totalPrice = room.getRoomType().getPrice() * (int)days;

            AccommodationBooking booking = AccommodationBooking.builder()
                .bookingId(idGenerator.generateBookingId(room.getRoomId()))
                .room(room)
                .customerId(customer.getCustomerId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .totalDays((int)days)
                .totalPrice(totalPrice)
                .capacity(room.getRoomType().getCapacity())
                .isBreakfast(false)
                .status(0) // WAITING
                .activeStatus(1)
                .extraPay(0)
                .refund(0)
                .createdDate(now.minusHours(2))
                .updatedDate(now.minusHours(2))
                .build();

            bookingRepository.save(booking);
            bookingIndex++;
        }
        log.info("Created 2 WAITING bookings (pending payment)");

        // Scenario 5: CANCELLED bookings
        for (int i = 12; i < 14; i++) {
            Room room = allRooms.get(i);
            room = roomRepository.findById(room.getRoomId()).orElse(room);
            Customer customer = customers.get(i % customers.size());

            LocalDateTime checkIn = now.plusDays(15).withHour(14).withMinute(0);
            LocalDateTime checkOut = now.plusDays(17).withHour(12).withMinute(0);
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            int totalPrice = room.getRoomType().getPrice() * (int)days;

            AccommodationBooking booking = AccommodationBooking.builder()
                .bookingId(idGenerator.generateBookingId(room.getRoomId()))
                .room(room)
                .customerId(customer.getCustomerId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .totalDays((int)days)
                .totalPrice(totalPrice)
                .capacity(room.getRoomType().getCapacity())
                .isBreakfast(false)
                .status(2) // CANCELLED
                .activeStatus(1)
                .extraPay(0)
                .refund(0)
                .createdDate(now.minusDays(3))
                .updatedDate(now.minusDays(1))
                .build();

            bookingRepository.save(booking);
            bookingIndex++;
        }
        log.info("Created 2 CANCELLED bookings");

        log.info("Total bookings created: {} (5 past/done, 3 active, 2 future, 2 waiting, 2 cancelled)", bookingIndex);
    }

    private void createMaintenanceSchedules() {
        List<Room> allRooms = roomRepository.findAll();
        LocalDate today = LocalDate.now();

        // Maintenance ongoing - affects room availability
        for (int i = 14; i < 16; i++) {
            Room room = allRooms.get(i);

            Maintenance maintenance = Maintenance.builder()
                .room(room)
                .startDate(today.minusDays(2))
                .startTime(LocalTime.of(8, 0))
                .endDate(today.plusDays(2))
                .endTime(LocalTime.of(17, 0))
                .activeStatus(1)
                .createdDate(LocalDateTime.now())
                .build();

            maintenanceRepository.save(maintenance);

            // Set room to maintenance mode
            room.setActiveRoom(0); // Inactive due to maintenance
            room.setMaintenanceStart(maintenance.getStartDate().atTime(maintenance.getStartTime()));
            room.setMaintenanceEnd(maintenance.getEndDate().atTime(maintenance.getEndTime()));
            roomRepository.save(room);
        }
        log.info("Created 2 ongoing maintenance schedules (rooms in maintenance)");

        // Future maintenance
        Room futureRoom = allRooms.get(16);
        Maintenance futureMaintenance = Maintenance.builder()
            .room(futureRoom)
            .startDate(today.plusDays(7))
            .startTime(LocalTime.of(8, 0))
            .endDate(today.plusDays(9))
            .endTime(LocalTime.of(17, 0))
            .activeStatus(1)
            .createdDate(LocalDateTime.now())
            .build();

        maintenanceRepository.save(futureMaintenance);
        log.info("Created 1 future maintenance schedule");
    }

    private void createReviews() {
        // Get bookings eligible for reviews (status=1 CONFIRMED, checkout date passed)
        List<AccommodationBooking> eligibleBookings = bookingRepository.findAll().stream()
            .filter(b -> b.getStatus() == 1 && b.getCheckOutDate().isBefore(LocalDateTime.now()))
            .toList();

        int reviewCount = 0;
        for (AccommodationBooking booking : eligibleBookings) {
            // Create reviews for first 3 eligible bookings
            if (reviewCount >= 3) break;

            // Generate realistic review ratings
            int cleanliness = 3 + random.nextInt(3); // 3-5
            int facility = 3 + random.nextInt(3); // 3-5
            int service = 3 + random.nextInt(3); // 3-5
            int valueForMoney = 3 + random.nextInt(3); // 3-5

            String[] comments = {
                "Great stay! The room was clean and comfortable. Staff were very helpful and friendly.",
                "Nice property with good facilities. Would definitely recommend to friends and family.",
                "Excellent experience overall. The location is convenient and the amenities are top-notch."
            };

            AccommodationReview review = AccommodationReview.builder()
                .bookingId(booking.getBookingId())
                .propertyId(booking.getRoom().getRoomType().getProperty().getPropertyId())
                .customerId(booking.getCustomerId())
                .cleanlinessRating(cleanliness)
                .facilityRating(facility)
                .serviceRating(service)
                .valueRating(valueForMoney)
                .comment(comments[reviewCount])
                .createdDate(LocalDateTime.now().minusDays(5 - reviewCount))
                .build();

            reviewRepository.save(review);
            reviewCount++;
        }

        log.info("Created {} reviews for completed bookings", reviewCount);
    }

    private void createSupportTickets() {
        // Use John Doe's fixed UUID for consistent frontend display
        UUID johnDoeId = UUID.fromString("263d012e-b86a-4813-be96-41e6da78e00d");
        List<Customer> customers = customerRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        // Ticket 1: OPEN - Accommodation service (John Doe)
        SupportTicket ticket1 = new SupportTicket();
        ticket1.setUserId(johnDoeId);
        ticket1.setSubject("Issue with room heating system");
        ticket1.setStatus(TicketStatus.OPEN);
        ticket1.setServiceSource(ServiceSource.ACCOMMODATION);
        ticket1.setExternalBookingId("EXT-ACC-" + UUID.randomUUID().toString().substring(0, 8));
        ticket1 = ticketRepository.save(ticket1);

        TicketMessage msg1 = new TicketMessage();
        msg1.setTicket(ticket1);
        msg1.setSenderType(SenderType.CUSTOMER);
        msg1.setSenderId(johnDoeId);
        msg1.setMessage("The heating in my room is not working properly. It's quite cold.");
        messageRepository.save(msg1);

        SupportProgress prog1 = new SupportProgress();
        prog1.setTicket(ticket1);
        prog1.setActionType(ActionType.CREATED);
        prog1.setDescription("Ticket created - Issue with room heating system");
        prog1.setPerformedBy(johnDoeId);
        progressRepository.save(prog1);

        log.info("Created OPEN ticket for accommodation service (John Doe)");

        // Ticket 2: IN_PROGRESS - Flight service
        SupportTicket ticket2 = new SupportTicket();
        ticket2.setUserId(customers.get(1).getCustomerId());
        ticket2.setSubject("Flight booking confirmation not received");
        ticket2.setStatus(TicketStatus.IN_PROGRESS);
        ticket2.setServiceSource(ServiceSource.FLIGHT);
        ticket2.setExternalBookingId("EXT-FLT-" + UUID.randomUUID().toString().substring(0, 8));
        ticket2 = ticketRepository.save(ticket2);

        TicketMessage msg2a = new TicketMessage();
        msg2a.setTicket(ticket2);
        msg2a.setSenderType(SenderType.CUSTOMER);
        msg2a.setSenderId(customers.get(1).getCustomerId());
        msg2a.setMessage("I booked a flight 2 days ago but haven't received the confirmation email yet.");
        messageRepository.save(msg2a);

        TicketMessage msg2b = new TicketMessage();
        msg2b.setTicket(ticket2);
        msg2b.setSenderType(SenderType.ADMIN);
        msg2b.setSenderId(UUID.randomUUID());
        msg2b.setMessage("Thank you for contacting us. We're checking with the airline now. Will update you within 24 hours.");
        messageRepository.save(msg2b);

        SupportProgress prog2a = new SupportProgress();
        prog2a.setTicket(ticket2);
        prog2a.setActionType(ActionType.CREATED);
        prog2a.setDescription("Ticket created - Flight booking confirmation not received");
        prog2a.setPerformedBy(customers.get(1).getCustomerId());
        progressRepository.save(prog2a);

        SupportProgress prog2b = new SupportProgress();
        prog2b.setTicket(ticket2);
        prog2b.setActionType(ActionType.STATUS_CHANGED);
        prog2b.setDescription("Status changed to IN_PROGRESS - Staff investigating with airline");
        prog2b.setPerformedBy(UUID.randomUUID());
        progressRepository.save(prog2b);

        log.info("Created IN_PROGRESS ticket for flight service");

        // Ticket 3: CLOSED - Rental service
        SupportTicket ticket3 = new SupportTicket();
        ticket3.setUserId(customers.get(2).getCustomerId());
        ticket3.setSubject("Car rental pickup location change");
        ticket3.setStatus(TicketStatus.CLOSED);
        ticket3.setServiceSource(ServiceSource.RENTAL);
        ticket3.setExternalBookingId("EXT-RNT-" + UUID.randomUUID().toString().substring(0, 8));
        ticket3 = ticketRepository.save(ticket3);

        TicketMessage msg3a = new TicketMessage();
        msg3a.setTicket(ticket3);
        msg3a.setSenderType(SenderType.CUSTOMER);
        msg3a.setSenderId(customers.get(2).getCustomerId());
        msg3a.setMessage("Can I change the pickup location for my car rental? Need to pick up at the airport instead.");
        messageRepository.save(msg3a);

        TicketMessage msg3b = new TicketMessage();
        msg3b.setTicket(ticket3);
        msg3b.setSenderType(SenderType.ADMIN);
        msg3b.setSenderId(UUID.randomUUID());
        msg3b.setMessage("We've updated your pickup location to the airport. Confirmation email sent.");
        messageRepository.save(msg3b);

        SupportProgress prog3a = new SupportProgress();
        prog3a.setTicket(ticket3);
        prog3a.setActionType(ActionType.CREATED);
        prog3a.setDescription("Ticket created - Car rental pickup location change");
        prog3a.setPerformedBy(customers.get(2).getCustomerId());
        progressRepository.save(prog3a);

        SupportProgress prog3b = new SupportProgress();
        prog3b.setTicket(ticket3);
        prog3b.setActionType(ActionType.STATUS_CHANGED);
        prog3b.setDescription("Status changed to CLOSED - Pickup location updated successfully");
        prog3b.setPerformedBy(UUID.randomUUID());
        progressRepository.save(prog3b);

        log.info("Created RESOLVED ticket for rental service");

        // Ticket 4: OPEN - Tour service
        SupportTicket ticket4 = new SupportTicket();
        ticket4.setUserId(customers.get(3).getCustomerId());
        ticket4.setSubject("Tour package details inquiry");
        ticket4.setStatus(TicketStatus.OPEN);
        ticket4.setServiceSource(ServiceSource.TOUR);
        ticket4.setExternalBookingId("EXT-TUR-" + UUID.randomUUID().toString().substring(0, 8));
        ticket4 = ticketRepository.save(ticket4);

        TicketMessage msg4 = new TicketMessage();
        msg4.setTicket(ticket4);
        msg4.setSenderType(SenderType.CUSTOMER);
        msg4.setSenderId(customers.get(3).getCustomerId());
        msg4.setMessage("What's included in the Bali cultural tour package? Does it include lunch and entrance fees?");
        messageRepository.save(msg4);

        SupportProgress prog4 = new SupportProgress();
        prog4.setTicket(ticket4);
        prog4.setActionType(ActionType.CREATED);
        prog4.setDescription("Ticket created - Tour package details inquiry");
        prog4.setPerformedBy(customers.get(3).getCustomerId());
        progressRepository.save(prog4);

        log.info("Created OPEN ticket for tour service");

        // Ticket 5: CLOSED - Insurance service
        SupportTicket ticket5 = new SupportTicket();
        ticket5.setUserId(customers.get(4).getCustomerId());
        ticket5.setSubject("Travel insurance claim process");
        ticket5.setStatus(TicketStatus.CLOSED);
        ticket5.setServiceSource(ServiceSource.INSURANCE);
        ticket5.setExternalBookingId("EXT-INS-" + UUID.randomUUID().toString().substring(0, 8));
        ticket5 = ticketRepository.save(ticket5);

        TicketMessage msg5a = new TicketMessage();
        msg5a.setTicket(ticket5);
        msg5a.setSenderType(SenderType.CUSTOMER);
        msg5a.setSenderId(customers.get(4).getCustomerId());
        msg5a.setMessage("How do I file a claim for my travel insurance? What documents are needed?");
        messageRepository.save(msg5a);

        TicketMessage msg5b = new TicketMessage();
        msg5b.setTicket(ticket5);
        msg5b.setSenderType(SenderType.ADMIN);
        msg5b.setSenderId(UUID.randomUUID());
        msg5b.setMessage("You need to submit: 1) Claim form, 2) Medical receipts (if applicable), 3) Flight cancellation proof. Email sent with full details.");
        messageRepository.save(msg5b);

        TicketMessage msg5c = new TicketMessage();
        msg5c.setTicket(ticket5);
        msg5c.setSenderType(SenderType.CUSTOMER);
        msg5c.setSenderId(customers.get(4).getCustomerId());
        msg5c.setMessage("Received. Thanks for the help!");
        messageRepository.save(msg5c);

        SupportProgress prog5a = new SupportProgress();
        prog5a.setTicket(ticket5);
        prog5a.setActionType(ActionType.CREATED);
        prog5a.setDescription("Ticket created - Travel insurance claim process");
        prog5a.setPerformedBy(customers.get(4).getCustomerId());
        progressRepository.save(prog5a);

        SupportProgress prog5b = new SupportProgress();
        prog5b.setTicket(ticket5);
        prog5b.setActionType(ActionType.CLOSED);
        prog5b.setDescription("Status changed to CLOSED - Customer satisfied with information provided");
        prog5b.setPerformedBy(UUID.randomUUID());
        progressRepository.save(prog5b);

        log.info("Created CLOSED ticket for insurance service");

        log.info("Total support tickets created: 5 (2 OPEN, 1 IN_PROGRESS, 2 CLOSED)");
        */ // END OF DISABLED SEEDER CODE
    }
}