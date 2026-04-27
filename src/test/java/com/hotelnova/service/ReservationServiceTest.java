package com.hotelnova.service;

import com.hotelnova.dao.GuestDAO;
import com.hotelnova.dao.ReservationDAO;
import com.hotelnova.dao.RoomDAO;
import com.hotelnova.exception.*;
import com.hotelnova.model.entity.Guest;
import com.hotelnova.model.entity.Reservation;
import com.hotelnova.model.entity.Room;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService — Business Rules")
class ReservationServiceTest {

    @Mock private ReservationDAO reservationDAO;
    @Mock private RoomDAO        roomDAO;
    @Mock private GuestDAO       guestDAO;

    @InjectMocks private ReservationService service;

    private Room  availableRoom;
    private Guest activeGuest;

    @BeforeEach
    void setUp() {
        availableRoom = new Room(1, "101", "DOUBLE", new BigDecimal("200000"), 2, true, "Room 101");
        activeGuest   = new Guest(1, "12345678", "Juan Pérez", "juan@mail.com", "3001234567", true);
    }

    @Test
    @DisplayName("T1: Debería lanzar RoomNotAvailableException cuando la habitación no está disponible")
    void shouldThrowWhenRoomNotAvailable() {
        Room unavailable = new Room(2, "102", "SINGLE", new BigDecimal("100000"), 1, false, "Occupied");
        when(guestDAO.findById(1)).thenReturn(Optional.of(activeGuest));
        when(roomDAO.findById(2)).thenReturn(Optional.of(unavailable));

        assertThrows(RoomNotAvailableException.class, () ->
                service.create(1, 2, 1,
                        LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "")
        );
    }

    @Test
    @DisplayName("T2: Debería lanzar ValidationException cuando el huésped está inactivo")
    void shouldThrowWhenGuestInactive() {
        Guest inactiveGuest = new Guest(2, "99999999", "María López", "m@mail.com", "", false);
        when(guestDAO.findById(2)).thenReturn(Optional.of(inactiveGuest));

        assertThrows(ValidationException.class, () ->
                service.create(2, 1, 1,
                        LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "")
        );
    }

    @Test
    @DisplayName("T3: Debería lanzar ValidationException cuando check-out <= check-in")
    void shouldThrowWhenCheckoutBeforeCheckin() {
        LocalDate today    = LocalDate.now().plusDays(2);
        LocalDate yesterday = today.minusDays(1);

        assertThrows(ValidationException.class, () ->
                service.create(1, 1, 1, today, yesterday, "")
        );
    }

    @Test
    @DisplayName("T3b: Debería lanzar ValidationException cuando check-in == check-out")
    void shouldThrowWhenSameDates() {
        LocalDate date = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () ->
                service.create(1, 1, 1, date, date, "")
        );
    }

    @Test
    @DisplayName("T4: Debería lanzar ReservationConflictException cuando hay solapamiento")
    void shouldThrowWhenOverlappingReservation() {
        when(guestDAO.findById(1)).thenReturn(Optional.of(activeGuest));
        when(roomDAO.findById(1)).thenReturn(Optional.of(availableRoom));
        when(reservationDAO.hasOverlap(1,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), 0))
                .thenReturn(true);

        assertThrows(ReservationConflictException.class, () ->
                service.create(1, 1, 1,
                        LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "")
        );
    }

    @Test
    @DisplayName("T5: Debería lanzar CheckoutException si la reserva no está activa")
    void shouldThrowCheckoutWhenNotActive() {
        Reservation checkedOut = new Reservation(10, 1, 1, 1,
                LocalDate.now().minusDays(3), LocalDate.now().minusDays(1),
                "CHECKED_OUT", new BigDecimal("400000"), "");
        when(reservationDAO.findById(10)).thenReturn(Optional.of(checkedOut));

        assertThrows(CheckoutException.class, () -> service.checkOut(10));
    }

    @Test
    @DisplayName("T6: Cálculo correcto del costo total con IVA 19%")
    void shouldCalculateCostWithTax() {
        BigDecimal price    = new BigDecimal("200000");
        LocalDate  checkIn  = LocalDate.of(2025, 1, 10);
        LocalDate  checkOut = LocalDate.of(2025, 1, 12);

        BigDecimal result = service.calculateCost(price, checkIn, checkOut);

        assertEquals(new BigDecimal("476000.00"), result);
    }

    @Test
    @DisplayName("T6b: Cálculo correcto con 5 noches")
    void shouldCalculateCostFiveNights() {
        BigDecimal price    = new BigDecimal("150000");
        LocalDate  checkIn  = LocalDate.of(2025, 3, 1);
        LocalDate  checkOut = LocalDate.of(2025, 3, 6);

        BigDecimal result = service.calculateCost(price, checkIn, checkOut);

        assertEquals(new BigDecimal("892500.00"), result);
    }

    @Test
    @DisplayName("T7: RoomService debe lanzar DuplicateException si el número ya existe")
    void shouldThrowWhenRoomNumberDuplicate() {
        RoomService roomService = new RoomService(roomDAO, reservationDAO);
        when(roomDAO.existsByRoomNumber("101")).thenReturn(true);

        assertThrows(DuplicateException.class, () ->
                roomService.create("101", "SINGLE", new BigDecimal("100000"), 1, "")
        );
    }

    @Test
    @DisplayName("T7b: RoomService debe guardar la habitación si el número es único")
    void shouldSaveRoomWhenNumberIsUnique() {
        RoomService roomService = new RoomService(roomDAO, reservationDAO);
        when(roomDAO.existsByRoomNumber("202")).thenReturn(false);
        Room saved = new Room(5, "202", "DOUBLE", new BigDecimal("200000"), 2, true, "");
        when(roomDAO.save(any())).thenReturn(saved);

        Room result = roomService.create("202", "DOUBLE", new BigDecimal("200000"), 2, "");

        assertNotNull(result);
        assertEquals("202", result.getRoomNumber());
        verify(roomDAO, times(1)).save(any(Room.class));
    }
}