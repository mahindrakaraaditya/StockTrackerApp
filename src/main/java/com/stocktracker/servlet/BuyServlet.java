package com.stocktracker.servlet;

import com.stocktracker.dao.TransactionDAO;
import com.stocktracker.model.Stock;
import com.stocktracker.model.User;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/buyStock")
public class BuyServlet extends HttpServlet {

    private TransactionDAO transactionDAO;

    @Override
    public void init() {
        transactionDAO = new TransactionDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ✅ Ensure user is logged in
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            // ✅ Get stock data from form
            String symbol = request.getParameter("symbol");
            double price = Double.parseDouble(request.getParameter("price"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            // ✅ Create Stock object for logging/confirmation
            Stock stock = new Stock();
            stock.setSymbol(symbol);
            stock.setPrice(price);
            stock.setQuantity(quantity);

            // ✅ Insert into database
            boolean success = transactionDAO.addTransaction(user.getId(), stock);

            if (success) {
                // Success alert and redirect to portfolio
                request.setAttribute("message", "Stock purchased successfully!");
                RequestDispatcher dispatcher = request.getRequestDispatcher("portfolio.jsp");
                dispatcher.forward(request, response);
            } else {
                request.setAttribute("error", "Failed to buy stock. Please try again.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("recommendations.jsp");
                dispatcher.forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Something went wrong. Try again later.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("recommendations.jsp");
            dispatcher.forward(request, response);
        }
    }
}
