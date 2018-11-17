package com.restTrial.client;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.dispatcher.DefaultFilterawareDispatcher;
import org.fusesource.restygwt.client.dispatcher.DispatcherFilter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.googlecode.gwt.crypto.bouncycastle.util.encoders.Base64;
import com.restyGWT.dto.User;

public class RESTyGWT implements EntryPoint {
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network " + "connection and try again.";

	private static final UserService SERVICE = GWT.create(UserService.class);

	private Button saveButton, dialogCloseButton, searchButton;
	private TextBox nameField, searchField;
	private Label textToServerLabel;
	private HTML serverResponseLabel;
	private DialogBox dialogBox;

	private void createDialogBox() {
		dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);

		dialogCloseButton = new Button("Close");
		dialogCloseButton.getElement().setId("closeButton");
		textToServerLabel = new Label();
		serverResponseLabel = new HTML();

		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");

		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);

		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);

		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(dialogCloseButton);
		dialogBox.setWidget(dialogVPanel);

		dialogCloseButton.addClickHandler(event -> {
			dialogBox.hide();
			saveButton.setEnabled(true);
			saveButton.setFocus(true);
		});
	}

	@Override
	public void onModuleLoad() {
		saveButton = new Button("Save");
		nameField = new TextBox();

		searchButton = new Button("Search");
		Button loadAllButton = new Button("Load All");
		searchField = new TextBox();

		saveButton.addStyleName("sendButton");
		searchButton.addStyleName("sendButton");

		RootPanel.get("saveNameFieldContainer").add(nameField);
		RootPanel.get("saveSendButtonContainer").add(saveButton);
		RootPanel.get("loadNameFieldContainer").add(searchField);
		RootPanel.get("loadSendButtonContainer").add(searchButton);
		RootPanel.get("fetchAllContainer").add(loadAllButton);

		nameField.setFocus(true);
		nameField.selectAll();

		createDialogBox();

		saveButton.addClickHandler(event -> {
			textToServerLabel.setText(nameField.getText());
			callSaveService();
		});

		loadAllButton.addClickHandler(event -> callLoadService());
		addAuthHeaders();
	}

	private void addAuthHeaders() {
		Defaults.setDispatcher(new DefaultFilterawareDispatcher(new DispatcherFilter() {

			@Override
			public boolean filter(Method method, RequestBuilder builder) {
				try {
					String basicAuthHeaderValue = createBasicAuthHeader("user", "user");
					builder.setHeader("Authorization", basicAuthHeaderValue);
				} catch (UnsupportedEncodingException e) {
					return false;
				}
				return true;
			}

			private String createBasicAuthHeader(String userName, String password) throws UnsupportedEncodingException {
				String credentials = userName + ":" + password;
				String encodedCredentials = new String(Base64.encode(credentials.getBytes()), "UTF-8");
				return "Basic " + encodedCredentials;
			}
		}));

	}

	private void callLoadService() {
		SERVICE.getAllUsers(new MethodCallback<List<User>>() {

			@Override
			public void onSuccess(Method method, List<User> response) {
				serverResponseLabel.removeStyleName("serverResponseLabelError");
				String names = response.stream()
						.map(e -> "<li>" + "Username:" + e.getUserName() + ", ID: " + e.getId() + "</li>")
						.collect(Collectors.joining(""));
				showDialogBox("REST endpoint call", "<ul>" + names + "<ul>");
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				serverResponseLabel.addStyleName("serverResponseLabelError");
				showDialogBox("REST endpoint call - Failure", SERVER_ERROR);
			}
		});
	}

	private void callSaveService() {
		String usernmae = nameField.getText();
		SERVICE.saveUser(usernmae, new MethodCallback<User>() {

			@Override
			public void onSuccess(Method method, User response) {
				serverResponseLabel.removeStyleName("serverResponseLabelError");
				showDialogBox("REST endpoint call", "Username: " + response.getUserName() + " ID: " + response.getId());
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				serverResponseLabel.addStyleName("serverResponseLabelError");
				showDialogBox("REST endpoint call - Failure", SERVER_ERROR);
			}
		});
	}

	private void showDialogBox(String title, String htmlText) {
		dialogBox.setText(title);
		serverResponseLabel.setHTML(htmlText);
		dialogBox.center();
		dialogCloseButton.setFocus(true);
	}
}
