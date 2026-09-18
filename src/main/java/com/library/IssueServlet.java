package com.library;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/issues")
public class IssueServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String sql = """
                SELECT
                    i.id,
                    i.book_id,
                    i.member_id,
                    i.issue_date,
                    i.due_date,
                    i.return_date,
                    i.status,
                    b.title,
                    b.isbn,
                    m.name AS member_name
                FROM issues i
                INNER JOIN books b
                    ON i.book_id = b.id
                INNER JOIN members m
                    ON i.member_id = m.id
                WHERE i.status = 'ISSUED'
                ORDER BY i.id DESC
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {

            StringBuilder json =
                    new StringBuilder();


            json.append("[");


            boolean first = true;


            while (result.next()) {

                if (!first) {

                    json.append(",");

                }


                json.append("{");


                json.append(
                        "\"id\":" +
                        result.getInt("id") +
                        ","
                );


                json.append(
                        "\"book_id\":" +
                        result.getInt("book_id") +
                        ","
                );


                json.append(
                        "\"member_id\":" +
                        result.getInt("member_id") +
                        ","
                );


                json.append(
                        "\"issue_date\":\"" +
                        result.getString("issue_date") +
                        "\","
                );


                json.append(
                        "\"due_date\":\"" +
                        result.getString("due_date") +
                        "\","
                );


                String returnDate =
                        result.getString(
                                "return_date"
                        );


                json.append(
                        "\"return_date\":" +
                        (
                            returnDate == null
                                ? "null"
                                : "\"" +
                                  escapeJson(returnDate) +
                                  "\""
                        ) +
                        ","
                );


                json.append(
                        "\"status\":\"" +
                        escapeJson(
                                result.getString("status")
                        ) +
                        "\","
                );


                json.append(
                        "\"title\":\"" +
                        escapeJson(
                                result.getString("title")
                        ) +
                        "\","
                );


                json.append(
                        "\"isbn\":\"" +
                        escapeJson(
                                result.getString("isbn")
                        ) +
                        "\","
                );


                json.append(
                        "\"member_name\":\"" +
                        escapeJson(
                                result.getString("member_name")
                        ) +
                        "\""
                );


                json.append("}");


                first = false;

            }


            json.append("]");


            response.getWriter()
                    .print(json.toString());


        } catch (Exception e) {

            sendError(
                    response,
                    e.getMessage()
            );

        }

    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String bookIdText =
                request.getParameter("book_id");


        String memberIdText =
                request.getParameter("member_id");


        String issueDate =
                request.getParameter("issue_date");


        String dueDate =
                request.getParameter("due_date");


        if (bookIdText == null ||
                memberIdText == null ||
                issueDate == null ||
                dueDate == null) {

            sendError(
                    response,
                    "Required fields are missing"
            );

            return;
        }


        try {

            int bookId =
                    Integer.parseInt(bookIdText);


            int memberId =
                    Integer.parseInt(memberIdText);


            Connection connection =
                    DBConnection.getConnection();


            try {

                connection.setAutoCommit(false);


                // -----------------------------------------
                // Check book
                // -----------------------------------------

                String bookSql = """
                        SELECT available_quantity
                        FROM books
                        WHERE id = ?
                        FOR UPDATE
                        """;


                int availableQuantity;


                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        bookSql
                                )
                ) {

                    statement.setInt(
                            1,
                            bookId
                    );


                    ResultSet result =
                            statement.executeQuery();


                    if (!result.next()) {

                        connection.rollback();


                        sendError(
                                response,
                                "Book not found"
                        );

                        return;

                    }


                    availableQuantity =
                            result.getInt(
                                    "available_quantity"
                            );

                }


                if (availableQuantity <= 0) {

                    connection.rollback();


                    sendError(
                            response,
                            "This book is not available"
                    );

                    return;

                }


                // -----------------------------------------
                // Check member
                // -----------------------------------------

                String memberSql =
                        "SELECT id FROM members WHERE id = ?";


                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        memberSql
                                )
                ) {

                    statement.setInt(
                            1,
                            memberId
                    );


                    ResultSet result =
                            statement.executeQuery();


                    if (!result.next()) {

                        connection.rollback();


                        sendError(
                                response,
                                "Member not found"
                        );

                        return;

                    }

                }


                // -----------------------------------------
                // Insert issue
                // -----------------------------------------

                String issueSql = """
                        INSERT INTO issues
                        (
                            book_id,
                            member_id,
                            issue_date,
                            due_date,
                            status
                        )
                        VALUES (?, ?, ?, ?, 'ISSUED')
                        """;


                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        issueSql
                                )
                ) {

                    statement.setInt(
                            1,
                            bookId
                    );


                    statement.setInt(
                            2,
                            memberId
                    );


                    statement.setDate(
                            3,
                            java.sql.Date.valueOf(
                                    issueDate
                            )
                    );


                    statement.setDate(
                            4,
                            java.sql.Date.valueOf(
                                    dueDate
                            )
                    );


                    statement.executeUpdate();

                }


                // -----------------------------------------
                // Decrease available quantity
                // -----------------------------------------

                String updateBookSql = """
                        UPDATE books
                        SET available_quantity =
                            available_quantity - 1
                        WHERE id = ?
                        """;


                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        updateBookSql
                                )
                ) {

                    statement.setInt(
                            1,
                            bookId
                    );


                    statement.executeUpdate();

                }


                connection.commit();


                response.getWriter().print(
                        "{\"success\":true," +
                        "\"message\":\"Book issued successfully\"}"
                );


            } catch (Exception e) {

                try {

                    connection.rollback();

                } catch (Exception ignored) {
                }


                sendError(
                        response,
                        e.getMessage()
                );


            } finally {

                try {

                    connection.setAutoCommit(true);

                    connection.close();

                } catch (Exception ignored) {
                }

            }


        } catch (Exception e) {

            sendError(
                    response,
                    e.getMessage()
            );

        }

    }


    private String escapeJson(
            String value) {

        if (value == null) {

            return "";

        }


        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

    }


    private void sendError(
            HttpServletResponse response,
            String message)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
        );


        response.getWriter().print(
                "{\"success\":false,\"message\":\"" +
                escapeJson(
                        message == null
                                ? "Unable to issue book"
                                : message
                ) +
                "\"}"
        );

    }

}