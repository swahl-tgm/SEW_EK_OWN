package client.GUI;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ShipAddTile extends StackPane {
    private Rectangle border = new Rectangle(20, 20);
    private boolean isSet;
    private boolean isDeact;

    public boolean isSet() {
        return this.isSet;
    }

    public boolean isDeact() {
        return isDeact;
    }

    public ShipAddTile () {
        border.setFill(Color.color(130.0/255,130.0/255,130.0/255,1));
        isSet = false;
        isDeact = false;
        getChildren().addAll(border);
    }

    public void setClicked() {
        this.isSet = true;
        this.isDeact = false;
        border.setFill(Color.DARKGRAY);
    }

    public void setDismissed() {
        this.isSet = false;
        this.isDeact = false;
        System.out.println("Set to false");
        border.setFill(Color.color(130.0/255,130.0/255,130.0/255,1));
    }

    public void setDeactivated() {
        System.out.println("Set to false");
        this.isSet = false;
        this.isDeact = true;
        border.setFill(Color.color(220.0/255,220.0/255,220.0/255,1));
    }

}
