package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.parkit.parkingsystem.constants.ParkingType.CAR;
import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processIncomingVehicleTest(){
        //Given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(CAR)).thenReturn(1);

        // WHEN
        parkingService.processIncomingVehicle();

        // THEN
        ArgumentCaptor<ParkingSpot> parkingSpotArgumentCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        ArgumentCaptor<Ticket> ticketArgumentCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(parkingSpotDAO, Mockito.times(1)).updateParking((parkingSpotArgumentCaptor.capture()));
        verify(ticketDAO, Mockito.times(1)).saveTicket((ticketArgumentCaptor.capture()));
        ParkingSpot updatedParkingSpot= parkingSpotArgumentCaptor.getValue();
        Ticket updatedTicket= ticketArgumentCaptor.getValue();
        assertEquals(updatedParkingSpot.isAvailable(), false);
        assertEquals(updatedParkingSpot.getParkingType(),CAR);
        assertEquals(updatedParkingSpot.getId(), 1);
        assertNotNull(updatedTicket.getInTime());
        assertEquals(updatedTicket.getOutTime(), null);
        assertEquals(1,updatedTicket.getParkingSpot().getId());
        assertEquals(updatedTicket.getPrice(), 0.0);
        assertEquals(updatedTicket.getVehicleRegNumber(),"ABCDEF");
        assertEquals(updatedTicket.getId(),0);
        assertEquals(updatedTicket.getParkingSpot().getId(),updatedParkingSpot.getId());
    }
    @Test
    public void processExitingVehicleTest(){
        // GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // WHEN
        parkingService.processExitingVehicle();
        // THEN
        ArgumentCaptor<ParkingSpot> parkingSpotArgumentCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        ArgumentCaptor<Ticket> ticketArgumentCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpotArgumentCaptor.capture());
        verify(ticketDAO, Mockito.times(1)).updateTicket((ticketArgumentCaptor.capture()));
        ParkingSpot updatedParkingSpot = parkingSpotArgumentCaptor.getValue();
        Ticket updatedTicket = ticketArgumentCaptor.getValue();
        assertEquals(updatedParkingSpot.isAvailable(), true);
        assertNotNull(updatedTicket.getOutTime());
        assertEquals(updatedTicket.getPrice(),1.5, 0.001);
        assertEquals(ticketDAO.getTicket("ABCDEF").getPrice(),1.5, 0.001);
        assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
    }
}
