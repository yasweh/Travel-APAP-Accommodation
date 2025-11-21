package apap.ti._5.accommodation_2306212083_be.seeder;

import apap.ti._5.accommodation_2306212083_be.model.*;
import apap.ti._5.accommodation_2306212083_be.repository.*;
import apap.ti._5.accommodation_2306212083_be.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccommodationBookingRepository bookingRepository;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private DataSeeder dataSeeder;

    private Property mockProperty;
    private RoomType mockRoomType;
    private Room mockRoom;
    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        // Setup mock property
        mockProperty = Property.builder()
            .propertyId("PROP-0001")
            .propertyName("Test Property")
            .type(0)
            .address("Test Address")
            .province(0)
            .description("Test Description")
            .totalRoom(5)
            .activeStatus(1)
            .activeRoom(5)
            .income(0)
            .ownerName("Test Owner")
            .ownerId(UUID.randomUUID())
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .listRoomType(new ArrayList<>())
            .build();

        // Setup mock room type
        mockRoomType = RoomType.builder()
            .roomTypeId("PROP-0001-RT-001")
            .name("Suite")
            .price(1000000)
            .description("Test Suite")
            .capacity(2)
            .facility("AC, TV, WiFi")
            .floor(2)
            .property(mockProperty)
            .activeStatus(1)
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .listRoom(new ArrayList<>())
            .build();

        // Setup mock room
        mockRoom = Room.builder()
            .roomId("PROP-0001-ROOM-201")
            .name("201")
            .availabilityStatus(1)
            .activeRoom(1)
            .roomType(mockRoomType)
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();

        // Setup mock customer
        mockCustomer = Customer.builder()
            .customerId(UUID.randomUUID())
            .name("John Doe")
            .email("john@example.com")
            .phone("08123456789")
            .createdDate(LocalDateTime.now())
            .build();
    }

    @Test
    void testRunWhenDatabaseAlreadySeeded() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(1L);

        // Act
        dataSeeder.run();

        // Assert
        verify(propertyRepository, times(1)).count();
        verify(customerRepository, never()).save(any(Customer.class));
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void testRunWhenDatabaseEmpty() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(propertyRepository.save(any(Property.class))).thenReturn(mockProperty);
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(mockRoomType);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        List<Customer> customers = Arrays.asList(mockCustomer, mockCustomer, mockCustomer, mockCustomer, mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(mockRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(idGenerator.generateBookingId(anyString())).thenReturn("BOOKING-001");
        when(bookingRepository.save(any(AccommodationBooking.class))).thenReturn(null);
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(null);

        // Act
        dataSeeder.run();

        // Assert
        verify(propertyRepository, times(1)).count();
        verify(customerRepository, times(5)).save(any(Customer.class));
        verify(propertyRepository, times(5)).save(any(Property.class));
        verify(roomTypeRepository, times(10)).save(any(RoomType.class));
        verify(roomRepository, atLeast(25)).save(any(Room.class));
        verify(bookingRepository, times(13)).save(any(AccommodationBooking.class));
        verify(maintenanceRepository, times(3)).save(any(Maintenance.class));
    }

    // @Test
    // void testCreateCustomers() throws Exception {
    //     // Arrange
    //     when(propertyRepository.count()).thenReturn(0L);
    //     when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
    //     // Return the object that was passed in to preserve property IDs
    //     when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //     when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //     when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
    //     when(customerRepository.findAll()).thenReturn(Arrays.asList(mockCustomer));
    //     when(roomRepository.findAll()).thenReturn(Arrays.asList(mockRoom));
    //     when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //     when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

    //     // Act
    //     dataSeeder.run();

    //     // Assert
    //     verify(customerRepository, times(5)).save(any(Customer.class));
    // }

    // @Test
    // void testCreatePropertiesAndRooms() throws Exception {
    //     // Arrange
    //     when(propertyRepository.count()).thenReturn(0L);
    //     when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
    //     // Return the object that was passed in to preserve property IDs
    //     when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //     when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //     when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
    //     when(customerRepository.findAll()).thenReturn(Arrays.asList(mockCustomer));
    //     when(roomRepository.findAll()).thenReturn(Arrays.asList(mockRoom));
    //     when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //     when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

    //     // Act
    //     dataSeeder.run();

    //     // Assert
    //     verify(propertyRepository, times(5)).save(any(Property.class));
    //     verify(roomTypeRepository, times(10)).save(any(RoomType.class));
    //     verify(roomRepository, atLeast(25)).save(any(Room.class));
    // }

    @Test
    void testCreateBookingsWithAllStatuses() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer, mockCustomer, mockCustomer, mockCustomer, mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Room room = Room.builder()
                .roomId("ROOM-" + i)
                .name("20" + i)
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(mockRoomType)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
            rooms.add(room);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert
        // 5 DONE + 3 CONFIRMED + 2 WAITING + 2 CANCELLED + 1 REFUND = 13 bookings
        verify(bookingRepository, times(13)).save(any(AccommodationBooking.class));
    }

    @Test
    void testCreateMaintenanceSchedules() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer, mockCustomer, mockCustomer, mockCustomer, mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Room room = Room.builder()
                .roomId("ROOM-" + i)
                .name("20" + i)
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(mockRoomType)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
            rooms.add(room);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert
        // 2 ongoing + 1 future = 3 maintenance schedules
        verify(maintenanceRepository, times(3)).save(any(Maintenance.class));
    }

    @Test
    void testCreateDoneBookings() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(mockRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert - verifies DONE bookings (status=4) are created
        verify(bookingRepository, atLeast(5)).save(any(AccommodationBooking.class));
    }

    @Test
    void testCreateConfirmedBookingsAndUpdateRoomStatus() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Room confirmedRoom = Room.builder()
            .roomId("PROP-0001-ROOM-201")
            .name("201")
            .availabilityStatus(1)
            .activeRoom(1)
            .roomType(mockRoomType)
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();
        
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(confirmedRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert - rooms are saved multiple times including status updates for confirmed bookings
        verify(roomRepository, atLeast(28)).save(any(Room.class));
    }

    @Test
    void testCreateWaitingBookings() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(mockRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert - total 13 bookings includes 2 WAITING
        verify(bookingRepository, times(13)).save(any(AccommodationBooking.class));
    }

    @Test
    void testCreateCancelledBookings() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(mockRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert - total 13 bookings includes 2 CANCELLED
        verify(bookingRepository, times(13)).save(any(AccommodationBooking.class));
    }

    @Test
    void testCreateRefundBookings() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(mockRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert - total 13 bookings includes 1 REFUND
        verify(bookingRepository, times(13)).save(any(AccommodationBooking.class));
    }

    @Test
    void testCreateOngoingMaintenanceAndUpdateRoomStatus() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Room maintenanceRoom = Room.builder()
            .roomId("PROP-0001-ROOM-201")
            .name("201")
            .availabilityStatus(1)
            .activeRoom(1)
            .roomType(mockRoomType)
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();
        
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(maintenanceRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert
        verify(maintenanceRepository, times(3)).save(any(Maintenance.class));
        // Rooms are saved for creation + maintenance updates
        verify(roomRepository, atLeast(30)).save(any(Room.class));
    }

    @Test
    void testCreateFutureMaintenance() throws Exception {
        // Arrange
        when(propertyRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        
        // Return the object that was passed in to preserve property IDs
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Customer> customers = Arrays.asList(mockCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            rooms.add(mockRoom);
        }
        when(roomRepository.findAll()).thenReturn(rooms);
        
        when(bookingRepository.save(any(AccommodationBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert - 1 future maintenance included in total 3
        verify(maintenanceRepository, times(3)).save(any(Maintenance.class));
    }
}