package client.GUI;

import javafx.animation.FillTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.imageio.plugins.tiff.TIFFDirectory;

public class Tile extends StackPane
{
    private int x;
    private int y;
    private boolean hasShip;

    private int shipArrayIndex;

    private Tile nextTile;
    private Tile beforeTile;

    private boolean alreadyHit;
    private boolean redBlockSet;

    private Rectangle border = new Rectangle(20, 20);
    private Text text = new Text();
    private ClientModel callback;

    // Setter / Getter
    public Tile getBeforeTile() {
        return beforeTile;
    }

    public void setBeforeTile(Tile beforeTile) {
        this.beforeTile = beforeTile;
    }

    public Tile getNextTile() {
        return nextTile;
    }

    public void setNextTile(Tile nextTile) {
        this.nextTile = nextTile;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getShipArrayIndex() {
        return shipArrayIndex;
    }

    public void setShipArrayIndex(int shipArrayIndex) {
        this.shipArrayIndex = shipArrayIndex;
    }

    public boolean isHasShip() {
        return hasShip;
    }

    public void setHasShip(boolean hasShip, Tile nextTile, Tile beforeTile, int shipArrayIndex) {
        if ( hasShip ) {
            this.border.setFill(Color.color(130.0/255,130.0/255,130.0/255,1));
            this.shipArrayIndex = shipArrayIndex;
        }
        else {
            this.border.setFill(Color.color(0.95,0.95,0.95));
            this.shipArrayIndex = -1;
        }
        this.nextTile = nextTile;
        this.beforeTile = beforeTile;
        this.hasShip = hasShip;
    }

    public boolean isAlreadyHit() {
        return alreadyHit;
    }

    public void setAlreadyHit(boolean alreadyHit) {
        this.alreadyHit = alreadyHit;
    }

    public Rectangle getRecBorder() {
        return border;
    }

    public void setRecBorder(Rectangle border) {
        this.border = border;
    }

    public ClientModel getCallback() {
        return callback;
    }

    public void setCallback(ClientModel callback) {
        this.callback = callback;
    }

    public boolean isRedBlockSet() {
        return redBlockSet;
    }

    public void setRedBlockSet(boolean readBlockSet) {
        this.redBlockSet = readBlockSet;
    }

    /**
     * Setzt das Tile auf Rot (mit einem Fade ein)
     */
    public void setRedBlock() {
        this.redBlockSet = true;
        FillTransition ft = new FillTransition(Duration.millis(300), border, Color.color(0.95,0.95,0.95), Color.color(1, 183.0/255,183.0/255));
        ft.setCycleCount(1);
        ft.setAutoReverse(true);
        ft.play();
    }

    /**
     * Setzt das Tile von Rot auf nomal (mit einem Fade aus)
     */
    public void resetRedBlock() {
        this.redBlockSet = false;
        FillTransition ft = new FillTransition(Duration.millis(300), border, Color.color(1, 183.0/255,183.0/255),  Color.color(0.95,0.95,0.95));
        ft.setCycleCount(1);
        ft.setAutoReverse(true);
        ft.play();
    }

    /**
     * Setzt das Tile wieder auf normal
     */
    public void setNormal() {
        this.redBlockSet = false;
        border.setStroke(Color.DARKGRAY);
        border.setFill(Color.color(0.95,0.95,0.95));
    }

    public Tile ( int x, int y, boolean hasShip, boolean isEnm ) {
        this.x = x;
        this.y = y;
        this.hasShip = hasShip;
        this.nextTile = null;
        this.beforeTile = null;
        this.alreadyHit = false;
        this.shipArrayIndex = -1;
        border.setStroke(Color.DARKGRAY);
        if ( isEnm ) {
            border.setFill(Color.color(0.85,0.85,0.85));
        }
        else {
            border.setFill(Color.color(0.95,0.95,0.95));
        }

        getChildren().addAll(border, text);
    }

    /**
     * Setzt das Tile auf Dunkel
     */
    public void setDark() {
        border.setFill(Color.color(0.85,0.85,0.85));
    }

    /**
     * Setzt das Tile auf Hell
     */
    public void setLight() {
        border.setFill(Color.color(0.95,0.95,0.95));
    }


    /**
     * Setzt das Tile auf Hit. Setzt in "X" in das Feld
     * @param realHit wenn realHit true: "X" wird rot
     */
    public void setHit( boolean realHit ) {
        if ( !this.isHasShip() ) {
            this.setDark();
        }
        this.text.setText("X");
        this.alreadyHit = true;
        if ( realHit ) {
            this.text.setFill(Color.color(1,0,0));
        }
        else {
            this.text.setFill(Color.color(0,0,0));
        }
    }

    /**
     * Setzt das Feld auf ein dunkles Rot um ein komplett zerstörtes Schiff zu visualisieren
     */
    public void setEndHit() {
        this.border.setFill(Color.color((float)80/255,0,0, 0.5));
    }
}
