package controller.user;

import com.google.gson.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Booking;
import model.BookingDetail;
import model.Ticket;
import repository.BookingDAO;
import repository.TicketDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "BookingController", value = "/booking")
public class BookingController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        int bookingId = Integer.parseInt(request.getParameter("bookingId"));
        JsonObject jsonResponse = new JsonObject();
        Gson gson = new Gson();
        BookingDAO bookingDAO = new BookingDAO();
        BookingDetail bookingDetail = bookingDAO.getBookingDetailById(bookingId);
        if (bookingDetail == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonResponse.addProperty("error", "Booking not found for ID: " + bookingId);
        } else {
            jsonResponse.add("bookingDetail", gson.toJsonTree(bookingDetail));
            response.setStatus(HttpServletResponse.SC_OK);
        }
        String json = gson.toJson(jsonResponse);
        response.getWriter().write(json);
        response.getWriter().flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JsonObject data= gson.fromJson(request.getReader(), JsonObject.class);
        System.out.println(data);
        Booking booking = gson.fromJson(data.get("booking"), Booking.class);
        JsonArray ticketsJson = data.get("tickets").getAsJsonArray();

        BookingDAO bookingDAO = new BookingDAO();
        boolean success = true;
        JsonObject responseData = new JsonObject();
        if(!bookingDAO.addBooking(booking)) {
            success = false;
        }
        else {
            booking= bookingDAO.getNewestBookingByUser(booking.getUser().getUID());
            responseData.addProperty("bookingId", booking.getBookingId());
            List<Ticket> tickets = new ArrayList<>();
            for(JsonElement ticket : ticketsJson){
                Ticket t = gson.fromJson(ticket, Ticket.class);
                t.setBooking(booking);
                tickets.add(t);
            }
            TicketDAO ticketDAO = new TicketDAO();
            for(Ticket t : tickets){
                if(!ticketDAO.addTicket(t)){
                    success = false;
                }
            }
            success = true;
        }
        responseData.addProperty("success", success);
        String json = gson.toJson(responseData);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}