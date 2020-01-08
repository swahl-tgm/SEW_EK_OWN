package client.GUI;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Spezielle Tiles für das Hinzufügen von Schiffen
 */
public class ShipAddTile extends StackPane {
    private Rectangle border = new Rectangle(20, 20);
    private boolean isSet;
    private boolean isDeact;

    // Setter / Getter

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

    /**
     * Setzt das Tile auf clicked (ändert die Farbe)
     */
    public void setClicked() {
        this.isSet = true;
        this.isDeact = false;
        border.setFill(Color.DARKGRAY);
    }

    /**
     * Setzt das Tile wieder auf den normalen Ausgangszustand zurück
     */
    public void setDismissed() {
        this.isSet = false;
        this.isDeact = false;
        border.setFill(Color.color(130.0/255,130.0/255,130.0/255,1));
    }

    /**
     * Setzt das Tile auf Deaktiviert
     */
    public void setDeactivated() {
        this.isSet = false;
        this.isDeact = true;
        border.setFill(Color.color(220.0/255,220.0/255,220.0/255,1));
    }

}
