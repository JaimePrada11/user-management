package com.jaimeprada.controller;

import com.jaimeprada.config.AppContext;
import com.jaimeprada.exceptions.DuplicateUserException;
import com.jaimeprada.exceptions.UserNotFoundException;
import com.jaimeprada.model.Role;
import com.jaimeprada.model.User;
import com.jaimeprada.service.UserService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import java.util.ArrayList;
import java.util.List;


public class UserController extends SelectorComposer<Component> {

    private UserService userService;

    @Wire
    private Listbox lbUsers;

    @Wire
    private Textbox tbSearch;

    @Wire
    private Label lblUserCount;

    @Wire
    private Label lblLogged;

    @Wire
    private Button btnNewUser;

    private boolean isAdmin;

    // Modal Form
    @Wire("#winUserForm")
    private Window winUserForm;

    @Wire("#winUserForm #tbFullName")
    private Textbox tbFullName;

    @Wire("#winUserForm #tbUsername")
    private Textbox tbUsername;

    @Wire("#winUserForm #tbEmail")
    private Textbox tbEmail;

    @Wire("#winUserForm #tbPassword")
    private Textbox tbPassword;


    @Wire("#winUserForm #lblPasswordHint")
    private Label lblPasswordHint;

    // Detail Panel
    @Wire
    private Vlayout detailsPanel;

    @Wire
    private Label lblDetailUsername;

    @Wire
    private Label lblDetailFullName;

    @Wire
    private Label lblDetailEmail;

    @Wire
    private Label lblDetailStatus;

    @Wire
    private Button btnEditFromDetails;

    @Wire
    private Button btnDeactivateFromDetails;

    @Wire
    private Button btnDeleteFromDetails;

    @Wire
    private Button btnCloseDetails;

    private Long editingId;
    private Long selectedId;
    private boolean selectedActive;

    private List<User> allUsers;
    private String currentSearch = "";

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        User loggedInUser = (User) Sessions.getCurrent().getAttribute(LoginController.SESSION_USER);

        if (loggedInUser == null) {
            Executions.sendRedirect("login.zul");
            return;
        }

        lblLogged.setValue(loggedInUser.getFullName());
        isAdmin = loggedInUser.getRole() == Role.ADMIN;

        btnNewUser.setVisible(isAdmin);
        btnEditFromDetails.setVisible(true);
        btnDeactivateFromDetails.setVisible(isAdmin);
        btnDeleteFromDetails.setVisible(isAdmin);

        userService = AppContext.getInstance().getUserService();

        loadUsers();
        showEmptyDetails();
    }

    @Listen("onClick = #btnLogout")
    public void onClickLogout() {
        Sessions.getCurrent().invalidate();
        Executions.sendRedirect("login.zul");
    }

    // Modal

    @Listen("onClick = #btnNewUser")
    public void onClickNewUser() {
        editingId = null;
        clearForm();
        winUserForm.setTitle("New User");
        lblPasswordHint.setVisible(false);
        openForm();
    }

    @Listen("onClick = #winUserForm #btnSave")
    public void onClickSave() {
        try {

            if (editingId == null) {
                createUser();
            } else {
                updateUser();
            }

            loadUsers();

            if (editingId != null && editingId.equals(selectedId)) {
                showDetails(editingId);
            }

            editingId = null;
            clearForm();
            closeForm();

            Messagebox.show(
                    "User saved successfully.",
                    "Information",
                    Messagebox.OK,
                    Messagebox.INFORMATION
            );

        } catch (DuplicateUserException | IllegalArgumentException e) {

            Messagebox.show(
                    e.getMessage(),
                    "Warning",
                    Messagebox.OK,
                    Messagebox.EXCLAMATION
            );

        } catch (Exception e) {

            Messagebox.show(
                    e.getMessage(),
                    "Error",
                    Messagebox.OK,
                    Messagebox.ERROR
            );
        }
    }

    @Listen("onClick = #winUserForm #btnCancel")
    public void onClickCancel() {
        editingId = null;
        clearForm();
        closeForm();
    }

    private void openForm() {
        winUserForm.setVisible(true);
        winUserForm.setMode(Window.Mode.MODAL);
    }

    private void closeForm() {
        winUserForm.setMode(Window.Mode.EMBEDDED);
        winUserForm.setVisible(false);
    }

    private void createUser() {

        User user = new User();

        user.setFullName(tbFullName.getValue().trim());
        user.setUsername(tbUsername.getValue().trim());
        user.setEmail(tbEmail.getValue().trim());
        user.setPassword(tbPassword.getValue());
        user.setRole(Role.USER);

        userService.create(user);
    }

    private void updateUser() {

        User user = new User();

        user.setId(editingId);
        user.setFullName(tbFullName.getValue().trim());
        user.setUsername(tbUsername.getValue().trim());
        user.setEmail(tbEmail.getValue().trim());
        user.setRole(Role.USER);

        String newPassword = tbPassword.getValue();

        userService.updateUser(
                user,
                newPassword.isBlank() ? null : newPassword
        );
    }

    // Search

    @Listen("onChange = #tbSearch")
    public void onSearch() {
        currentSearch = tbSearch.getValue();
        applyFilterAndRender();
    }

    // User List

    private void loadUsers() {
        allUsers = isAdmin ? userService.findAll() : userService.findAllActive();
        applyFilterAndRender();
    }

    private void applyFilterAndRender() {

        List<User> filtered = filterUsers(allUsers, currentSearch);

        renderUsers(filtered);

        lbUsers.setActivePage(0);
        lblUserCount.setValue(String.valueOf(filtered.size()));
    }

    private List<User> filterUsers(List<User> users, String query) {

        String q = (query == null) ? "" : query.trim().toLowerCase();

        if (q.isEmpty()) {
            return new ArrayList<>(users);
        }

        List<User> result = new ArrayList<>();

        for (User u : users) {

            boolean matchesQuery = u.getUsername().toLowerCase().contains(q)
                    || u.getFullName().toLowerCase().contains(q)
                    || u.getEmail().toLowerCase().contains(q);

            if (matchesQuery) {
                result.add(u);
            }
        }

        return result;
    }

    private void renderUsers(List<User> users) {

        lbUsers.getItems().clear();

        for (User u : users) {

            Listitem item = new Listitem();
            item.setValue(u);

            item.appendChild(new Listcell(String.valueOf(u.getId())));
            item.appendChild(new Listcell(u.getUsername()));
            item.appendChild(new Listcell(u.getFullName()));

            Listcell emailCell = new Listcell();
            Label emailLabel = new Label(u.getEmail().toLowerCase());
            emailLabel.setSclass("email-link");
            emailCell.appendChild(emailLabel);
            item.appendChild(emailCell);


            Listcell statusCell = new Listcell();
            Label statusLabel = new Label(u.isActive() ? "Active" : "Inactive");
            statusLabel.setSclass(u.isActive() ? "badge badge-active" : "badge badge-inactive");
            statusCell.appendChild(statusLabel);
            item.appendChild(statusCell);


            Listcell actionCell = new Listcell();
            Hlayout actionBox = new Hlayout();
            actionBox.setSclass("action-cell");

            Button btnView = new Button("View");
            btnView.setSclass("btn btn-sm btn-edit");
            btnView.addEventListener(
                    Events.ON_CLICK,
                    e -> {
                        lbUsers.setSelectedItem(item);
                        showDetails(u.getId());
                    }
            );
            actionBox.appendChild(btnView);

            if (isAdmin) {
                Button btnDeactivate = new Button(u.isActive() ? "Deactivate" : "Activate");
                Button btnDelete = new Button("Delete");

                btnDeactivate.setSclass("btn btn-sm btn-secondary");
                btnDelete.setSclass("btn btn-sm btn-delete");

                btnDeactivate.addEventListener(
                        Events.ON_CLICK,
                        e -> toggleActiveStatus(u.getId(), u.isActive())
                );

                btnDelete.addEventListener(
                        Events.ON_CLICK,
                        e -> permanentlyDeleteUser(u.getId())
                );

                actionBox.appendChild(btnDeactivate);
                actionBox.appendChild(btnDelete);
            }

            actionCell.appendChild(actionBox);

            item.appendChild(actionCell);

            lbUsers.appendChild(item);
        }
    }

    @Listen("onSelect = #lbUsers")
    public void onSelectUser() {

        Listitem selected = lbUsers.getSelectedItem();

        if (selected == null || !(selected.getValue() instanceof User)) {
            showEmptyDetails();
            return;
        }

        User u = (User) selected.getValue();
        showDetails(u.getId());
    }

    // Detail Panel

    private void showDetails(Long id) {

        try {

            User user = userService.getUserById(id);

            selectedId = user.getId();
            selectedActive = user.isActive();

            lblDetailUsername.setValue(user.getUsername());
            lblDetailFullName.setValue(user.getFullName());
            lblDetailEmail.setValue(user.getEmail().toLowerCase());
            lblDetailStatus.setValue(user.isActive() ? "Active" : "Inactive");

            btnDeactivateFromDetails.setLabel(user.isActive() ? "Deactivate" : "Activate");

            detailsPanel.setVisible(true);

        } catch (UserNotFoundException e) {

            showEmptyDetails();

            Messagebox.show(
                    e.getMessage(),
                    "Error",
                    Messagebox.OK,
                    Messagebox.ERROR
            );
        }
    }

    private void showEmptyDetails() {
        selectedId = null;
        detailsPanel.setVisible(false);
    }

    @Listen("onClick = #btnCloseDetails")
    public void onClickCloseDetails() {
        lbUsers.setSelectedItem(null);
        showEmptyDetails();
    }

    @Listen("onClick = #btnEditFromDetails")
    public void onClickEditFromDetails() {
        if (selectedId != null) {
            editUser(selectedId);
        }
    }

    @Listen("onClick = #btnDeactivateFromDetails")
    public void onClickDeactivateFromDetails() {
        if (selectedId != null) {
            toggleActiveStatus(selectedId, selectedActive);
        }
    }

    @Listen("onClick = #btnDeleteFromDetails")
    public void onClickDeleteFromDetails() {
        if (selectedId != null) {
            permanentlyDeleteUser(selectedId);
        }
    }

    // Edit - Delete

    private void editUser(Long id) {

        try {

            User user = userService.getUserById(id);

            editingId = user.getId();

            tbFullName.setValue(user.getFullName());
            tbUsername.setValue(user.getUsername());
            tbEmail.setValue(user.getEmail());

            tbPassword.setValue("");

            winUserForm.setTitle("Edit User");
            lblPasswordHint.setVisible(true);

            openForm();

        } catch (UserNotFoundException e) {

            Messagebox.show(
                    e.getMessage(),
                    "Error",
                    Messagebox.OK,
                    Messagebox.ERROR
            );
        }
    }

    private void toggleActiveStatus(Long id, boolean currentlyActive) {

        String action = currentlyActive ? "deactivate" : "activate";

        Messagebox.show(
                "Do you want to " + action + " this user?",
                "Confirmation",
                Messagebox.YES | Messagebox.NO,
                Messagebox.QUESTION,
                event -> {

                    if (Messagebox.ON_YES.equals(event.getName())) {

                        try {

                            if (currentlyActive) {
                                userService.softDeleteUser(id);
                            } else {
                                userService.restoreUser(id);
                            }

                            loadUsers();

                            if (id.equals(selectedId)) {
                                showDetails(id);
                            }

                        } catch (UserNotFoundException e) {

                            Messagebox.show(
                                    e.getMessage(),
                                    "Error",
                                    Messagebox.OK,
                                    Messagebox.ERROR
                            );
                        }
                    }
                }
        );
    }

    private void permanentlyDeleteUser(Long id) {

        Messagebox.show(
                "Do you want to permanently delete this user? This action cannot be undone.",
                "Confirmation",
                Messagebox.YES | Messagebox.NO,
                Messagebox.QUESTION,
                event -> {

                    if (Messagebox.ON_YES.equals(event.getName())) {

                        try {

                            userService.deleteUser(id);

                            loadUsers();

                            if (id.equals(selectedId)) {
                                showEmptyDetails();
                            }

                        } catch (UserNotFoundException e) {

                            Messagebox.show(
                                    e.getMessage(),
                                    "Error",
                                    Messagebox.OK,
                                    Messagebox.ERROR
                            );
                        }
                    }
                }
        );
    }


    private void clearForm() {

        tbFullName.setValue("");
        tbUsername.setValue("");
        tbEmail.setValue("");
        tbPassword.setValue("");

    }
}