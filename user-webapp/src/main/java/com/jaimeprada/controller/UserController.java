package com.jaimeprada.controller;

import com.jaimeprada.config.AppContext;
import com.jaimeprada.exceptions.DuplicateUserException;
import com.jaimeprada.exceptions.UserNotFoundException;
import com.jaimeprada.model.Role;
import com.jaimeprada.model.User;
import com.jaimeprada.service.UserService;
import org.zkoss.zk.ui.Component;
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

    // --- Modal Form ---
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

    // --- Detail Panel ---
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
    private Button btnDeleteFromDetails;

    @Wire
    private Button btnCloseDetails;

    private Long editingId;
    private Long selectedId;

    private List<User> allUsers;
    private String currentSearch = "";

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        userService = AppContext.getInstance().getUserService();

        loadUsers();
        showEmptyDetails();
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
        winUserForm.doModal();
    }

    private void closeForm() {
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
        allUsers = userService.findAll();
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
            Label emailLabel = new Label(u.getEmail());
            emailLabel.setSclass("email-link");
            emailCell.appendChild(emailLabel);
            item.appendChild(emailCell);


            Listcell roleCell = new Listcell();
            Label roleLabel = new Label(u.getRole().name());
            roleLabel.setSclass("badge badge-role");
            roleCell.appendChild(roleLabel);
            item.appendChild(roleCell);


            Listcell statusCell = new Listcell();
            Label statusLabel = new Label(u.isActive() ? "Active" : "Inactive");
            statusLabel.setSclass(u.isActive() ? "badge badge-active" : "badge badge-inactive");
            statusCell.appendChild(statusLabel);
            item.appendChild(statusCell);


            Listcell actionCell = new Listcell();
            Hlayout actionBox = new Hlayout();
            actionBox.setSclass("action-cell");

            Button btnView = new Button("Edit");
            Button btnDelete = new Button("Save");

            btnView.setSclass("btn btn-sm btn-edit");
            btnDelete.setSclass("btn btn-sm btn-delete");

            btnView.addEventListener(
                    Events.ON_CLICK,
                    e -> {
                        lbUsers.setSelectedItem(item);
                        showDetails(u.getId());
                    }
            );

            btnDelete.addEventListener(
                    Events.ON_CLICK,
                    e -> SoftdeleteUser(u.getId())
            );

            actionBox.appendChild(btnView);
            actionBox.appendChild(btnDelete);
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

            lblDetailUsername.setValue(user.getUsername());
            lblDetailFullName.setValue(user.getFullName());
            lblDetailEmail.setValue(user.getEmail());
            lblDetailStatus.setValue(user.isActive() ? "Active" : "Inactive");

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

    @Listen("onClick = #btnDeleteFromDetails")
    public void onClickDeleteFromDetails() {
        if (selectedId != null) {
            SoftdeleteUser(selectedId);
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

    private void SoftdeleteUser(Long id) {

        Messagebox.show(
                "¿Do you want to delete this user?",
                "Confirmation",
                Messagebox.YES | Messagebox.NO,
                Messagebox.QUESTION,
                event -> {

                    if (Messagebox.ON_YES.equals(event.getName())) {

                        try {

                            userService.softDeleteUser(id);

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