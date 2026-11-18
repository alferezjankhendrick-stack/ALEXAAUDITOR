package com.scc.menu;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner sc;
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final int currentAdminId;

    public AdminMenu(Scanner sc, UserDao userDao, ActivityLogDao logDao, int currentUserId) {
        this.sc = sc;
        this.userDao = userDao;
        this.logDao = logDao;
        this.currentAdminId = currentUserId;
    }

    public void show() {
        boolean running = true;

        while (running) {
            System.out.println("\n===================================");
            System.out.println("       GLAMOURA ADMIN DASHBOARD");
            System.out.println("===================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve an Account");
            System.out.println("4. Reject an Account");
            System.out.println("5. Update User Role and Status");
            System.out.println("6. View System Audit Logs");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    displayUsers(userDao.getAllUsers(), "ALL SYSTEM USERS");
                    break;

                case "2":
                    displayUsers(userDao.getPendingUsers(), "PENDING REGISTRATIONS");
                    break;

                case "3":
                    approveAccount();
                    break;

                case "4":
                    rejectAccount();
                    break;

                case "5":
                    updateUserAccount();
                    break;

                case "6":
                    auditLogMenu();
                    break;

                case "7":
                    logDao.logAction(currentAdminId, "USER_LOGOUT",
                            "User admin (ID: " + currentAdminId + ") logged out.");
                    running = false;
                    System.out.println("\nAdmin logged out.");
                    break;

                default:
                    System.out.println("Invalid menu option.");
            }
        }
    }

    private void displayUsers(List<User> users, String title) {
        System.out.println("\n================ " + title + " ================");

        System.out.printf("%-8s %-20s %-20s %-18s %-12s%n",
                "USER ID", "NAME", "USERNAME", "ROLE", "STATUS");

        System.out.println(
                "-------------------------------------------------------------------------------"
        );

        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            for (User user : users) {
                System.out.printf("%-8d %-20s %-20s %-18s %-12s%n",
                        user.id(),
                        shorten(user.name(), 19),
                        shorten(user.username(), 19),
                        shorten(user.role(), 17),
                        shorten(user.status(), 11));
            }
        }

        System.out.println(
                "-------------------------------------------------------------------------------"
        );
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }

    private void approveAccount() {
        System.out.println("\n=== APPROVE ACCOUNT ===");

        List<User> pendingUsers = userDao.getPendingUsers();

        displayUsers(pendingUsers, "PENDING REGISTRATIONS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        System.out.print("Enter the ID to approve (0 to cancel): ");
        String input = sc.nextLine().trim();

        try {
            int userId = Integer.parseInt(input);

            if (userId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean isPending = pendingUsers.stream()
                    .anyMatch(user -> user.id() == userId);

            if (!isPending) {
                System.out.println(
                        "That ID is not in the pending registrations list."
                );
                return;
            }

            boolean success = userDao.approveUser(userId);

            if (success) {
                System.out.println(
                        "Account ID " + userId + " approved successfully!"
                );
                logDao.logAction(currentAdminId, "USER_APPROVED",
                        "Admin [" + currentAdminId + "] approved account for User ID " + userId);
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Please enter a number.");
        }
    }

    private void rejectAccount() {
        System.out.println("\n=== REJECT ACCOUNT ===");

        List<User> pendingUsers = userDao.getPendingUsers();

        displayUsers(pendingUsers, "PENDING REGISTRATIONS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        System.out.print("Enter the ID to reject (0 to cancel): ");
        String input = sc.nextLine().trim();

        try {
            int userId = Integer.parseInt(input);

            if (userId == 0) {
                System.out.println("Rejection cancelled.");
                return;
            }

            boolean isPending = pendingUsers.stream()
                    .anyMatch(user -> user.id() == userId);

            if (!isPending) {
                System.out.println(
                        "That ID is not in the pending registrations list."
                );
                return;
            }

            boolean success = userDao.updateUserStatus(userId, "REJECTED");

            if (success) {
                System.out.println(
                        "Account ID " + userId + " rejected successfully."
                );
                logDao.logAction(currentAdminId, "USER_REJECTED",
                        "Admin [" + currentAdminId + "] rejected account for User ID " + userId);
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Please enter a number.");
        }
    }

    private void updateUserAccount() {
        System.out.println("\n=== UPDATE USER ROLE AND STATUS ===");

        List<User> users = userDao.getAllUsers();

        displayUsers(users, "ALL SYSTEM USERS");

        if (users.isEmpty()) {
            return;
        }

        System.out.print("Enter the User ID to update (0 to cancel): ");
        String input = sc.nextLine().trim();

        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Please enter a number.");
            return;
        }

        if (userId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User selectedUser = users.stream()
                .filter(user -> user.id() == userId)
                .findFirst()
                .orElse(null);

        if (selectedUser == null) {
            System.out.println("User ID not found.");
            return;
        }

        if (userId == currentAdminId) {
            System.out.println("You cannot update your own admin account.");
            return;
        }

        System.out.println("\nSelected Account:");
        System.out.println("Name: " + selectedUser.name());
        System.out.println("Username: " + selectedUser.username());
        System.out.println("Current Role: " + selectedUser.role());
        System.out.println("Current Status: " + selectedUser.status());

        System.out.println("\nAvailable Roles:");
        System.out.println("1. ADMIN");
        System.out.println("2. CUSTOMER");
        System.out.println("3. MAKEUP_ARTIST");
        System.out.print("Enter new role (0 to cancel): ");

        String roleInput = sc.nextLine().trim();

        if (roleInput.equals("0")) {
            System.out.println("Update cancelled.");
            return;
        }

        String newRole;

        switch (roleInput.toUpperCase(Locale.ROOT)) {
            case "1":
            case "ADMIN":
                newRole = "ADMIN";
                break;

            case "2":
            case "CUSTOMER":
                newRole = "CUSTOMER";
                break;

            case "3":
            case "MAKEUP_ARTIST":
                newRole = "MAKEUP_ARTIST";
                break;

            default:
                System.out.println("Invalid role selection.");
                return;
        }

        System.out.println("\nAvailable Statuses:");
        System.out.println("1. APPROVED");
        System.out.println("2. PENDING");
        System.out.println("3. REJECTED");
        System.out.print("Enter new status (0 to cancel): ");

        String statusInput = sc.nextLine().trim();

        if (statusInput.equals("0")) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus;

        switch (statusInput.toUpperCase(Locale.ROOT)) {
            case "1":
            case "APPROVED":
                newStatus = "APPROVED";
                break;

            case "2":
            case "PENDING":
                newStatus = "PENDING";
                break;

            case "3":
            case "REJECTED":
                newStatus = "REJECTED";
                break;

            default:
                System.out.println("Invalid status selection.");
                return;
        }

        System.out.println("\n=== CONFIRM ACCOUNT UPDATE ===");
        System.out.println("User ID: " + userId);
        System.out.println("New Role: " + newRole);
        System.out.println("New Status: " + newStatus);
        System.out.print("Confirm update? (Y/N): ");

        String confirmation = sc.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean success = userDao.updateUserRoleAndStatus(
                userId, newRole, newStatus
        );

        if (success) {
            System.out.println("User account updated successfully!");
            logDao.logAction(currentAdminId, "USER_UPDATED",
                    "Admin [" + currentAdminId + "] changed User " + userId
                            + " from role " + selectedUser.role() + "/" + selectedUser.status()
                            + " to role " + newRole + "/" + newStatus);
        } else {
            System.out.println("Unable to update user account.");
        }
    }

    private void auditLogMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- SYSTEM AUDIT LOGS ---");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            switch (sc.nextLine().trim()) {
                case "1":
                    displayLogs(logDao.getAllLogs(), "ALL SYSTEM LOGS");
                    break;

                case "2":
                    filterLogsByUser();
                    break;

                case "3":
                    clearLogs();
                    break;

                case "0":
                    back = true;
                    break;

                default:
                    System.out.println("Invalid menu option.");
            }
        }
    }

    private void filterLogsByUser() {
        System.out.print("Enter User ID to filter (or 0 to cancel): ");
        String input = sc.nextLine().trim();

        try {
            int userId = Integer.parseInt(input);

            if (userId == 0) {
                System.out.println("Filter cancelled.");
                return;
            }

            displayLogs(logDao.getLogsByUserId(userId), "LOGS FOR USER ID " + userId);

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Please enter a number.");
        }
    }

    private void clearLogs() {
        int total = logDao.getAllLogs().size();

        if (total == 0) {
            System.out.println("There are no logs to clear.");
            return;
        }

        System.out.println("\nWARNING: This permanently deletes all " + total
                + " audit log entries.");
        System.out.print("Type CLEAR to confirm (anything else cancels): ");

        if (!sc.nextLine().trim().equals("CLEAR")) {
            System.out.println("Clear cancelled. No logs were deleted.");
            return;
        }

        int deleted = logDao.clearLogs();

        if (deleted >= 0) {
            System.out.println(deleted + " log entries cleared.");
            // Leave a trace so the clearing itself is always auditable
            logDao.logAction(currentAdminId, "LOGS_CLEARED",
                    "Admin [" + currentAdminId + "] cleared " + deleted + " audit log entries");
        }
    }

    private void displayLogs(List<ActivityLog> logs, String title) {
        String format = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
        String line = "-".repeat(101);

        System.out.println();
        System.out.println(line);
        System.out.printf(format, "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println(line);

        if (logs.isEmpty()) {
            System.out.println("No logs found.");
        } else {
            for (ActivityLog log : logs) {
                List<String> descLines = wrap(log.getDescription(), 35);

                System.out.printf(format,
                        log.getLogId(),
                        log.getUserId() == null ? "-" : String.valueOf(log.getUserId()),
                        shorten(log.getActionType(), 18),
                        descLines.get(0),
                        shorten(log.getTimestamp(), 20));

                // Long descriptions continue on extra rows so no audit detail is lost
                for (int i = 1; i < descLines.size(); i++) {
                    System.out.printf(format, "", "", "", descLines.get(i), "");
                }
            }
        }

        System.out.println(line);
        System.out.println(logs.size() + " log entries shown.");
    }

    private List<String> wrap(String text, int width) {
        List<String> lines = new java.util.ArrayList<>();
        String remaining = text == null ? "" : text.trim();

        while (remaining.length() > width) {
            int cut = remaining.lastIndexOf(' ', width);
            if (cut <= 0) {
                cut = width;
            }
            lines.add(remaining.substring(0, cut).trim());
            remaining = remaining.substring(cut).trim();
        }

        lines.add(remaining);
        return lines;
    }
}