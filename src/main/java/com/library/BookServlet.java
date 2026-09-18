package com.library;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/books")
public class BookServlet extends HttpServlet {

    // =========================
    // GET - VIEW / SEARCH BOOKS
    // =========================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String search = request.getParameter("search");

        String sql;

        if (search == null || search.trim().isEmpty()) {

            sql = """
                    SELECT id, isbn, title, author, category,
                           quantity, available_quantity
                    FROM books
                    ORDER BY id DESC
                    """;

        } else {

            sql = """
                    SELECT id, isbn, title, author, category,
                           quantity, available_quantity
                    FROM books
                    WHERE isbn LIKE ?
                       OR title LIKE ?
                       OR author LIKE ?
                       OR category LIKE ?
                    ORDER BY id DESC
                    """;
        }

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            if (search != null && !search.trim().isEmpty()) {

                String value = "%" + search.trim() + "%";

                statement.setString(1, value);
                statement.setString(2, value);
                statement.setString(3, value);
                statement.setString(4, value);
            }

            ResultSet result = statement.executeQuery();

            PrintWriter out = response.getWriter();

            out.print("[");

            boolean first = true;

            while (result.next()) {

                if (!first) {
                    out.print(",");
                }

                out.print("{");

                out.print("\"id\":" +
                        result.getInt("id") + ",");

                out.print("\"isbn\":\"" +
                        escapeJson(result.getString("isbn")) + "\",");

                out.print("\"title\":\"" +
                        escapeJson(result.getString("title")) + "\",");

                out.print("\"author\":\"" +
                        escapeJson(result.getString("author")) + "\",");

                out.print("\"category\":\"" +
                        escapeJson(result.getString("category")) + "\",");

                out.print("\"quantity\":" +
                        result.getInt("quantity") + ",");

                out.print("\"available_quantity\":" +
                        result.getInt("available_quantity"));

                out.print("}");

                first = false;
            }

            out.print("]");

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                    "{\"success\":false,\"message\":\"" +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // =========================
    // POST - ADD BOOK
    // =========================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String isbn = request.getParameter("isbn");
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String category = request.getParameter("category");
        String quantityText = request.getParameter("quantity");

        if (isbn == null ||
            title == null ||
            author == null ||
            quantityText == null) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                    "{\"success\":false,\"message\":\"Required fields are missing\"}"
            );

            return;
        }

        try {

            int quantity = Integer.parseInt(quantityText);

            if (quantity < 1) {
                throw new Exception(
                        "Quantity must be at least 1"
                );
            }

            String sql = """
                    INSERT INTO books
                    (isbn, title, author, category,
                     quantity, available_quantity)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(1, isbn.trim());
                statement.setString(2, title.trim());
                statement.setString(3, author.trim());
                statement.setString(4,
                        category == null ? "" : category.trim());
                statement.setInt(5, quantity);
                statement.setInt(6, quantity);

                statement.executeUpdate();
            }

            response.getWriter().print(
                    "{\"success\":true,\"message\":\"Book added successfully\"}"
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                    "{\"success\":false,\"message\":\"" +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // =========================
    // PUT - UPDATE BOOK
    // =========================
    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idText = request.getParameter("id");
        String isbn = request.getParameter("isbn");
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String category = request.getParameter("category");
        String quantityText = request.getParameter("quantity");

        try {

            int id = Integer.parseInt(idText);
            int newQuantity = Integer.parseInt(quantityText);

            String getSql =
                    "SELECT quantity, available_quantity " +
                    "FROM books WHERE id = ?";

            int oldQuantity;
            int oldAvailable;

            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(getSql)
            ) {

                statement.setInt(1, id);

                ResultSet result = statement.executeQuery();

                if (!result.next()) {

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    response.getWriter().print(
                            "{\"success\":false,\"message\":\"Book not found\"}"
                    );

                    return;
                }

                oldQuantity = result.getInt("quantity");
                oldAvailable = result.getInt("available_quantity");
            }

            int issuedBooks = oldQuantity - oldAvailable;

            if (newQuantity < issuedBooks) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                response.getWriter().print(
                        "{\"success\":false,\"message\":\"Quantity cannot be less than currently issued books\"}"
                );

                return;
            }

            int newAvailable =
                    newQuantity - issuedBooks;

            String sql = """
                    UPDATE books
                    SET isbn = ?,
                        title = ?,
                        author = ?,
                        category = ?,
                        quantity = ?,
                        available_quantity = ?
                    WHERE id = ?
                    """;

            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(1, isbn.trim());
                statement.setString(2, title.trim());
                statement.setString(3, author.trim());
                statement.setString(4,
                        category == null ? "" : category.trim());
                statement.setInt(5, newQuantity);
                statement.setInt(6, newAvailable);
                statement.setInt(7, id);

                statement.executeUpdate();
            }

            response.getWriter().print(
                    "{\"success\":true,\"message\":\"Book updated successfully\"}"
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                    "{\"success\":false,\"message\":\"" +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // =========================
    // DELETE - DELETE BOOK
    // =========================
    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idText = request.getParameter("id");

        try {

            int id = Integer.parseInt(idText);

            String sql =
                    "DELETE FROM books WHERE id = ?";

            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(1, id);

                int rows =
                        statement.executeUpdate();

                if (rows == 0) {

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    response.getWriter().print(
                            "{\"success\":false,\"message\":\"Book not found\"}"
                    );

                    return;
                }
            }

            response.getWriter().print(
                    "{\"success\":true,\"message\":\"Book deleted successfully\"}"
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                    "{\"success\":false,\"message\":\"" +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}