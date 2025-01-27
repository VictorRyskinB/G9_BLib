package gui;



import client.SubscriberUI;
import client.UserManager;
import common.CheckAvailabilityMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CheckAvailabilityWindowController extends BaseController {
    @FXML
    private Button btnOrderBook = null;
    @FXML
    private Button btnBack = null;
    @FXML
    private TextField bookName;


    /**
     * Retrieve the name of the book entered in the text field.
     * 
     * @return The name of the book entered by the user.
     */
    private String getbookName() {
        return bookName.getText();
    }

    /**
     * Handle the action of checking book availability when the user clicks the button.
     * 
     * @param event The action event triggered by the button click.
     * @throws Exception If an error occurs during the availability check process.
     */
    public void btnCheckAvailability(ActionEvent event) throws Exception {
        String bookName = getbookName();
        UserManager UM = UserManager.getInstance();
        
        if (bookName == null || bookName.trim().isEmpty()) {
            showAlert("Error", "Please enter a book name.");
            return;
        }

        // Create a message to send to the server via UserManager
        CheckAvailabilityMessage message = new CheckAvailabilityMessage();
        message.bookName = bookName;

        // Send the message to the server and wait for a response
        UM.send(message);
        Object response = UM.inb.getObj();

        handleAvailabilityResponse(response, event);
    }

    /**
     * Handle the server response after checking book availability.
     * 
     * @param response The response object from the server.
     * @param event    The action event triggered by the user.
     */
    private void handleAvailabilityResponse(Object response, ActionEvent event) {
        if (response instanceof Boolean) {
            boolean isAvailable = (Boolean) response;
            if (isAvailable) {
                openOrderWindow(event);
            } else {
                showAlert("Not Available", "This book is not available for ordering at this time.");
            }
        } else {
            showAlert("Error", "Unexpected response from server");
        }
    }

    /**
     * Open the order window when a book is available.
     * 
     * @param event The action event triggered by the user.
     */
    private void openOrderWindow(ActionEvent event) {

        String bookName = getbookName(); // Retrieve the book name entered by the user
        OrderWindowController.setTempBookName(bookName);
        SubscriberUI.mainController.switchView("/gui/OrderWindow.fxml");
    }

    /**
     * Handle the action of going back to the previous screen.
     * 
     * @param event The action event triggered by the button click.
     * @throws Exception If an error occurs while navigating back.
     */
    public void getBackBtn(ActionEvent event) throws Exception {
		

		SubscriberUI.mainController.goBack();
		
		

    }
	
//	//alert massage to present to user. 
//	private void showAlert(String title, String content) {
//        Alert alert = new Alert(AlertType.INFORMATION);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(content);
//        alert.showAndWait();
//    }
}
