package com.jaimeprada.controller;

import com.jaimeprada.config.AppContext;
import com.jaimeprada.exceptions.AuthenticationException;
import com.jaimeprada.model.User;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

public class LoginController extends SelectorComposer<Component> {

    public static final String SESSION_USER = "currentUser";

    @Wire
    private Textbox tbUsername;

    @Wire
    private Textbox tbPassword;

    @Wire
    private Label lblError;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        if (Sessions.getCurrent().getAttribute(SESSION_USER) != null) {
            Executions.sendRedirect("user.zul");
        }
    }

    @Listen("onClick = #btnLogin")
    public void onClickLogin() {

        String username = tbUsername.getValue() == null ? "" : tbUsername.getValue().trim();
        String password = tbPassword.getValue() == null ? "" : tbPassword.getValue();

        lblError.setVisible(false);

        if (username.isBlank() || password.isBlank()) {
            showError("Username y password are required.");
            return;
        }

        try {
            User authenticatedUser = AppContext.getInstance()
                    .getAuthenticator()
                    .authenticate(username, password);

            Sessions.getCurrent().setAttribute(SESSION_USER, authenticatedUser);

            Executions.sendRedirect("user.zul");

        } catch (AuthenticationException e) {
            showError("Username o password are incorrect.");
        } catch (Exception e) {
            showError("A error occurred.");
        }
    }

    @Listen("onOK = #tbPassword")
    public void onEnterPassword() {
        onClickLogin();
    }

    private void showError(String message) {
        lblError.setValue(message);
        lblError.setVisible(true);
    }
}
