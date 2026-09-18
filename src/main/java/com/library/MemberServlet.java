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

@WebServlet("/api/members")
public class MemberServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String search =
                request.getParameter("search");

        String sql;

        if (search == null ||
                search.trim().isEmpty()) {

            sql = """
                    SELECT id, name, email, phone, address
                    FROM members
                    ORDER BY id DESC
                    """;

        } else {

            sql = """
                    SELECT id, name, email, phone, address
                    FROM members
                    WHERE name LIKE ?
                       OR email LIKE ?
                       OR phone LIKE ?
                       OR address LIKE ?
                    ORDER BY id DESC
                    """;
        }


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {


            if (search != null &&
                    !search.trim().isEmpty()) {

                String value =
                        "%" + search.trim() + "%";

                statement.setString(1, value);
                statement.setString(2, value);
                statement.setString(3, value);
                statement.setString(4, value);
            }


            ResultSet result =
                    statement.executeQuery();


            PrintWriter out =
                    response.getWriter();


            out.print("[");


            boolean first = true;


            while (result.next()) {

                if (!first) {

                    out.print(",");

                }


                out.print("{");


                out.print(
                        "\"id\":" +
                        result.getInt("id") +
                        ","
                );


                out.print(
                        "\"name\":\"" +
                        escapeJson(
                                result.getString("name")
                        ) +
                        "\","
                );


                out.print(
                        "\"email\":\"" +
                        escapeJson(
                                result.getString("email")
                        ) +
                        "\","
                );


                out.print(
                        "\"phone\":\"" +
                        escapeJson(
                                result.getString("phone")
                        ) +
                        "\","
                );


                out.print(
                        "\"address\":\"" +
                        escapeJson(
                                result.getString("address")
                        ) +
                        "\""
                );


                out.print("}");


                first = false;
            }


            out.print("]");


        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
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


        String name =
                request.getParameter("name");


        String email =
                request.getParameter("email");


        String phone =
                request.getParameter("phone");


        String address =
                request.getParameter("address");


        if (name == null ||
                name.trim().isEmpty()) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Member name is required"
            );

            return;
        }


        String sql = """
                INSERT INTO members
                (name, email, phone, address)
                VALUES (?, ?, ?, ?)
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    name.trim()
            );


            statement.setString(
                    2,
                    emptyToNull(email)
            );


            statement.setString(
                    3,
                    emptyToNull(phone)
            );


            statement.setString(
                    4,
                    emptyToNull(address)
            );


            statement.executeUpdate();


            sendSuccess(
                    response,
                    "Member added successfully"
            );


        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        }

    }


    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String idText =
                request.getParameter("id");


        String name =
                request.getParameter("name");


        String email =
                request.getParameter("email");


        String phone =
                request.getParameter("phone");


        String address =
                request.getParameter("address");


        if (idText == null ||
                name == null ||
                name.trim().isEmpty()) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Member ID and name are required"
            );

            return;
        }


        try {

            int id =
                    Integer.parseInt(idText);


            String sql = """
                    UPDATE members
                    SET name = ?,
                        email = ?,
                        phone = ?,
                        address = ?
                    WHERE id = ?
                    """;


            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        name.trim()
                );


                statement.setString(
                        2,
                        emptyToNull(email)
                );


                statement.setString(
                        3,
                        emptyToNull(phone)
                );


                statement.setString(
                        4,
                        emptyToNull(address)
                );


                statement.setInt(
                        5,
                        id
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );


                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Member not found"
                    );


                    return;
                }

            }


            sendSuccess(
                    response,
                    "Member updated successfully"
            );


        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid member ID"
            );


        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        }

    }


    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String idText =
                request.getParameter("id");


        if (idText == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Member ID is required"
            );

            return;
        }


        try {

            int id =
                    Integer.parseInt(idText);


            String sql =
                    "DELETE FROM members WHERE id = ?";


            try (
                    Connection connection =
                            DBConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        id
                );


                int rows =
                        statement.executeUpdate();


                if (rows == 0) {

                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Member not found"
                    );

                    return;
                }

            }


            sendSuccess(
                    response,
                    "Member deleted successfully"
            );


        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Member cannot be deleted. They may have issued books."
            );

        }

    }


    private String emptyToNull(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return null;

        }

        return value.trim();
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


    private void sendSuccess(
            HttpServletResponse response,
            String message)
            throws IOException {

        response.getWriter().print(
                "{\"success\":true,\"message\":\"" +
                escapeJson(message) +
                "\"}"
        );

    }


    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.getWriter().print(
                "{\"success\":false,\"message\":\"" +
                escapeJson(
                        message == null
                                ? "Unknown error"
                                : message
                ) +
                "\"}"
        );

    }

}