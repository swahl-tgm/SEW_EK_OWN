package client.GUI;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import javax.imageio.plugins.tiff.TIFFDirectory;

public class Tile extends StackPane
{
    private int x;
    private int y;
    private boolean hasShip;

    private Tile nextTile;
    private Tile beforeTile;

    private boolean alreadyHit;


    private Rectangle border = new Rectangle(20, 20);
    private Text text = new Text();
    private ClientModel callback;

    // Setter / Getter
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

    public boolean isHasShip() {
        return hasShip;
    }

    public void setHasShip(boolean hasShip, Tile nextTile, Tile beforeTile) {
        if ( hasShip ) {
            this.border.setFill(Color.color(130.0/255,130.0/255,130.0/255,1));
            this.nextTile = nextTile;
            this.beforeTile = beforeTile;
        }
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


    public Tile ( int x, int y, boolean hasShip ) {
        this.x = x;
        this.y = y;
        this.hasShip = hasShip;
        this.nextTile = null;
        this.beforeTile = null;
        border.setStroke(Color.DARKGRAY);
        border.setFill(Color.color(0.95,0.95,0.95));

        getChildren().addAll(border, text);
    }


    public void hitTile() {

    }
}
