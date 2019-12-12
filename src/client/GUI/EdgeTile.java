package client.GUI;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class EdgeTile extends StackPane
{
    private Text text = new Text();
    private Rectangle border = new Rectangle(20, 20);


    public EdgeTile ( String text ) {
        this.text.setText(text);
        border.setFill(Color.TRANSPARENT);
        getChildren().addAll(this.text, border);
    }
}
