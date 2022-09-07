package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorWithDataBaseIT {
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }
    @AfterAll
    private static void tearDown(){
    }



    @Test
    public void processExitingVehicleWithRegularCustomerDiscountTest(){
        /*
        Verify that when we query the database for a previous customer the discount applies to the price.
        So we make one car come
         */
        //GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket ticketToSetUpTime = ticketDAO.getTicket("ABCDEF");
        ticketToSetUpTime.setInTime(ticketToSetUpTime.getInTime().minusDays(1));
        ticketDAO.updateTicketInTime(ticketToSetUpTime);
        parkingService.processExitingVehicle();
        parkingService.processIncomingVehicle();
        Ticket ticketToSetUpTime2 = ticketDAO.getTicket("ABCDEF");
        ticketToSetUpTime2.setInTime(ticketToSetUpTime2.getInTime().minusHours(1));
        ticketDAO.updateTicketInTime(ticketToSetUpTime2);
        // WHEN
        parkingService.processExitingVehicle();
        // THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals((1* Fare.CAR_RATE_PER_HOUR*0.95), ticket.getPrice(),0.001);
    }

}
