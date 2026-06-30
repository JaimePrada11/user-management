package com.jaimeprada.controller;

import com.jaimeprada.config.AppContext;
import com.jaimeprada.exceptions.DuplicateUserException;
import com.jaimeprada.model.Role;
import com.jaimeprada.model.User;
import com.jaimeprada.service.UserService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

public class RegisterController extends SelectorComposer<Component> {

    @Wire
    private Textbox tbFullName;

    @Wire
    private Textbox tbUsername;

    @Wire
    private Textbox tbEmail;

    @Wire
    private Textbox tbPassword;

    @Wire
    private Textbox tbConfirmPassword;

    @Wire
    private Label lblError;

    private UserService userService;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        userService = AppContext.getInstance().getUserService();
    }

    @Listen("onClick = #btnRegister")
    public void onClickRegister() {

        lblError.setVisible(false);

        String fullName = safeTrim(tbFullName.getValue());
        String username = safeTrim(tbUsername.getValue());
        String email = safeTrim(tbEmail.getValue());
        String password = tbPassword.getValue() == null ? "" : tbPassword.getValue();
        String confirmPassword = tbConfirmPassword.getValue() == null ? "" : tbConfirmPassword.getValue();

        if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            showError("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        try {

            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(Role.USER);

            userService.create(newUser);

            Messagebox.show(
                    "Account successfully created. You can now log in",
                    "Registration successful",
                    Messagebox.OK,
                    Messagebox.INFORMATION,
                    event -> Executions.sendRedirect("login.zul")
            );

        } catch (DuplicateUserException | IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("An error occurred while creating the account. Please try again.");
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showError(String message) {
        lblError.setValue(message);
        lblError.setVisible(true);
    }
}