package listview.ListItem;

import dto.objectdata.CustomerAlertData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class ListItem {

    private CustomerAlertData data;

    @FXML
    private HBox item;

    @FXML
    private Label title, timeInYaz, notificationDesc;

    @FXML
    private Pane alertBackground;

    public ListItem()
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ListItem.fxml"));
        fxmlLoader.setController(this);
        try
        {
            fxmlLoader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setInfo(CustomerAlertData data)
    {
        this.data = data;
        this.title.setText(this.data.getMessage());
        this.notificationDesc.setText(this.data.getHeadline());
        this.timeInYaz.setText("Time to receive: " + this.data.getTimeInYaz());
    }

    public HBox getItem() {
        return item;
    }
}
