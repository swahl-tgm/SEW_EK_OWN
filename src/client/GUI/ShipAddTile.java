package client.GUI;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ShipAddTile extends StackPane {
    private Rectangle border = new Rectangle(20, 20);


    public ShipAddTile () {
        border.setFill(Color.color(130.0/255,130.0/255,130.0/255,1));
        getChildren().addAll(border);
    }

    public void setClicked() {
        border.setFill(Color.DARKGRAY);
    }

    public void setDismissed() {
        System.out.println("Teest");
        border.setFill(Color.color(130.0/255,130.0/255,130.0/255,1));
    }

    public void setDeactivated() {
        System.out.println("Hollas");
        border.setFill(Color.color(220.0/255,220.0/255,220.0/255,1));
    }

}
