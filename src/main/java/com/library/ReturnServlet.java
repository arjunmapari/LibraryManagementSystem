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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@WebServlet("/api/returns")
public class ReturnServlet extends HttpServlet {

    // Fine per late day.
    // Change this value if your college project
    // requires a different fine.
    private static final double FINE_PER_DAY = 5.0;


    // =====================================================
    // GET CURRENTLY ISSUED BOOKS
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String search =
                request.getParameter("search");


        StringBuilder sql =
                new StringBuilder("""
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
                    """);


        if (search != null &&
                !search.trim().isEmpty()) {

            sql.append("""
                    AND (
                        b.title LIKE ?
                        OR b.isbn LIKE ?
                        OR m.name LIKE ?
                    )
                    """);

        }


        sql.append(
                " ORDER BY i.due_date ASC"
        );


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql.toString()
                        )
        ) {


            if (search != null &&
                    !search.trim().isEmpty()) {

                String value =
                        "%" + search.trim() + "%";


                statement.setString(
                        1,
                        value
                );


                statement.setString(
                        2,
                        value
                );


                statement.setString(
                        3,
                        value
                );

            }


            ResultSet result =
                    statement.executeQuery();


            StringBuilder json =
                    new StringBuilder();


            json.append("[");


            boolean first = true;


            LocalDate today =
                    LocalDate.now();


            while (result.next()) {

                if (!first) {

                    json.append(",");

                }


                LocalDate dueDate =
                        result.getDate(
                                "due_date"
                        ).toLocalDate();


                long lateDays =
                        Math.max(
                                0,
                                ChronoUnit.DAYS.between(
                                        dueDate,
                                        today
                                )
                        );


                double fine =
                        lateDays *
                        FINE_PER_DAY;


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
                        result.getDate(
                                "issue_date"
                        ) +
                        "\","
                );


                json.append(
                        "\"due_date\":\"" +
                        result.getDate(
                                "due_date"
                        ) +
                        "\","
                );


                json.append(
                        "\"title\":\"" +
                        escapeJson(
                                result.getString(
                                        "title"
                                )
                        ) +
                        "\","
                );


                json.append(
                        "\"isbn\":\"" +
                        escapeJson(
                                result.getString(
                                        "isbn"
                                )
                        ) +
                        "\","
                );


                json.append(
                        "\"member_name\":\"" +
                        escapeJson(
                                result.getString(
                                        "member_name"
                                )
                        ) +
                        "\","
                );


                json.append(
                        "\"late_days\":" +
                        lateDays +
                        ","
                );


                json.append(
                        "\"fine\":" +
                        String.format(
                                java.util.Locale.US,
                                "%.2f",
                                fine
                        )
                );


                json.append("}");


                first = false;

            }


            json.append("]");


            response.getWriter()
                    .print(
                            json.toString()
                    );


        } catch (Exception e) {

            sendError(
                    response,
                    e.getMessage()
            );

        }

    }


    // =====================================================
    // RETURN BOOK
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String issueIdText =
                request.getParameter(
                        "issue_id"
                );


        if (issueIdText == null ||
                issueIdText.trim().isEmpty()) {

            sendError(
                    response,
                    "Issue ID is required"
            );

            return;

        }


        try {

            int issueId =
                    Integer.parseInt(
                            issueIdText
                    );


            Connection connection =
                    DBConnection.getConnection();


            try {

                connection.setAutoCommit(false);


                // -----------------------------------------
                // Get issue
                // -----------------------------------------

                String issueSql = """
                        SELECT
                            book_id,
                            status
                        FROM issues
                        WHERE id = ?
                        FOR UPDATE
                        """;


                int bookId;

                String status;


                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        issueSql
                                )
                ) {

                    statement.setInt(
                            1,
                            issueId
                    );


                    ResultSet result =
                            statement.executeQuery();


                    if (!result.next()) {

                        connection.rollback();


                        sendError(
                                response,
                                "Issue record not found"
                        );


                        return;

                    }


                    bookId =
                            result.getInt(
                                    "book_id"
                            );


                    status =
                            result.getString(
                                    "status"
                            );

                }


                // -----------------------------------------
                // Prevent duplicate return
                // -----------------------------------------

                if (!"ISSUED".equalsIgnoreCase(
                        status
                )) {

                    connection.rollback();


                    sendError(
                            response,
                            "This book has already been returned"
                    );


                    return;

                }


                // -----------------------------------------
                // Update issue
                // -----------------------------------------

                String updateIssueSql = """
                        UPDATE issues
                        SET
                            return_date = CURDATE(),
                            status = 'RETURNED'
                        WHERE id = ?
                        """;


                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        updateIssueSql
                                )
                ) {

                    statement.setInt(
                            1,
                            issueId
                    );


                    statement.executeUpdate();

                }


                // -----------------------------------------
                // Increase book availability
                // -----------------------------------------

                String updateBookSql = """
                        UPDATE books
                        SET available_quantity =
                            available_quantity + 1
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
                        "\"message\":\"Book returned successfully\"}"
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


        } catch (NumberFormatException e) {

            sendError(
                    response,
                    "Invalid issue ID"
            );


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
                                ? "Unable to process request"
                                : message
                ) +
                "\"}"
        );

    }

}