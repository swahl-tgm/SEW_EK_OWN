package client.GUI;

import client.GUI.Ships.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class ClientController implements Initializable, EventHandler {

    private ClientModel model;

    @FXML
    private GridPane root;
    @FXML
    private GridPane fieldEig;
    // Tiles
    private Tile[][] ownClick;
    @FXML
    private GridPane fieldEnm;
    // Tiles
    private Tile[][] enmClick;
    @FXML
    private GridPane toPlace;
    // To Place
    private GridPane schlachtGrid;
    private ShipAddTile[] schlachtTiles;
    private GridPane kreuzGrid;
    private ShipAddTile[] kreuzTiles;
    private GridPane fragGrid;
    private ShipAddTile[] fragTiles;
    private GridPane miniGrid;
    private ShipAddTile[] miniTiles;

    private ShipEnum actv;

    private Text schlachtText;
    private Text kreuzerText;
    private Text fragText;
    private Text miniText;
    // Text
    private StackPane eigTextBase;
    private Text eigText;
    private StackPane enmTextBase;
    private Text enmText;


    private void ownTileClicked( Tile currentTile, MouseEvent mouseEvent ) {
        if ( actv != ShipEnum.KeinBoot ) {
            ShipEnum tempEnm = actv;
            model.addShip(actv, currentTile.getX(), currentTile.getY());
            dismissActive();
            resetText(tempEnm, -1);
        }
        else {
            if ( currentTile.isHasShip() ) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY)
                {
                    ShipEnum currentDeleted = model.removeShip(currentTile);
                    resetText(currentDeleted, 1);
                    // Only temp, will bi changed in dismissActive
                    actv = currentDeleted;
                    dismissActive();

                } else if (mouseEvent.getButton() == MouseButton.SECONDARY)
                {
                    model.turnShip(currentTile);
                }
            }
        }

    }


    private void deactivateAddShipField( ShipEnum which) {
        switch ( which ) {
            case Schlachtschiff:
                for (ShipAddTile tile: schlachtTiles) {
                    tile.setDeactivated();
                }
                break;
            case Kreuzer:
                for (ShipAddTile tile: kreuzTiles) {
                    tile.setDeactivated();
                }
                break;
            case Fragette:
                for (ShipAddTile tile: fragTiles) {
                    tile.setDeactivated();
                }
                break;
            case Minisuchboot:
                for (ShipAddTile tile: miniTiles) {
                    tile.setDeactivated();
                }
                break;
        }
    }

    private void resetText( ShipEnum which, int change ) {
        int anz;
        switch ( which ) {
            case Schlachtschiff:
                anz = Integer.parseInt(schlachtText.getText().substring(0,1))+change;
                this.schlachtText.setText(anz + "x Schlachtschiff");
                if ( anz == 0 ) {
                    deactivateAddShipField(ShipEnum.Schlachtschiff);
                }
                break;
            case Kreuzer:
                anz = Integer.parseInt(kreuzerText.getText().substring(0,1))+change;
                this.kreuzerText.setText(anz + "x Kreuzer");
                if ( anz == 0 ) {
                    deactivateAddShipField(ShipEnum.Kreuzer);
                }
                break;
            case Fragette:
                anz = Integer.parseInt(fragText.getText().substring(0,1))+change;
                if ( anz == 1 ) {
                    this.fragText.setText(anz + "x Fragette");
                }
                else {
                    this.fragText.setText(anz + "x Fragetten");
                }
                if ( anz == 0 ) {
                    deactivateAddShipField(ShipEnum.Fragette);
                }
                break;
            case Minisuchboot:
                anz = Integer.parseInt(miniText.getText().substring(0,1))+change;
                this.miniText.setText(anz + "x Minisuchb");
                if ( anz == 1 ) {
                    this.miniText.setText(anz + "x Minisuchboot");
                }
                else {
                    this.miniText.setText(anz + "x Minisuchboote");
                }
                if ( anz == 0 ) {
                    deactivateAddShipField(ShipEnum.Minisuchboot);
                }
                break;
        }
    }

    private void enmTileClicked( Tile currentTile ) {
        System.out.println("x: " + currentTile.getX());
        System.out.println("y: " + currentTile.getY());
    }


    private void createView() {
        LinkedList<Tile[][]> actionClickList = model.createContent(fieldEig, fieldEnm);

        ownClick = actionClickList.get(0);
        enmClick = actionClickList.get(1);

        for (int i = 0; i < ownClick.length; i++ ) {
            for ( int j = 0; j < ownClick[i].length; j++ ) {
                ownClick[i][j].setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        Tile currentTile = (Tile)mouseEvent.getSource();

                        ownTileClicked(currentTile, mouseEvent);
                    }
                });
                enmClick[i][j].setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        Tile currentTile = (Tile)mouseEvent.getSource();

                        enmTileClicked(currentTile);
                    }
                });
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        model = new ClientModel();


        //eigText.setContentDisplay(ContentDisplay.TOP);

        this.enmText = new Text("Gegner Spielfeld");
        enmTextBase = new StackPane();
        enmTextBase.getChildren().add(enmText);

        this.eigText = new Text("Eigenes Spielfeld");
        eigTextBase = new StackPane();
        eigTextBase.getChildren().add(eigText);


        this.root.add(eigTextBase, 0,0);
        this.root.add(enmTextBase, 1,0);


        this.schlachtText = new Text("1x Schlachtschiff");
        this.kreuzerText = new Text("2x Kreuzer");
        this.fragText = new Text("3x Fragetten");
        this.miniText = new Text("4x Minisuchboote");

        this.schlachtGrid = new GridPane();
        this.kreuzGrid = new GridPane();
        this.fragGrid = new GridPane();
        this.miniGrid = new GridPane();

        this.schlachtTiles = new ShipAddTile[4];
        this.kreuzTiles = new ShipAddTile[3];
        this.fragTiles = new ShipAddTile[2];
        this.miniTiles = new ShipAddTile[1];

        createTiles(schlachtGrid, schlachtTiles);
        createTiles(kreuzGrid, kreuzTiles);
        createTiles(fragGrid, fragTiles);
        createTiles(miniGrid, miniTiles);

        this.toPlace.add(schlachtText, 0,0);
        this.toPlace.add(schlachtGrid, 1,0);
        this.toPlace.add(kreuzerText, 0,1);
        this.toPlace.add(kreuzGrid, 1,1);
        this.toPlace.add(fragText, 2,0);
        this.toPlace.add(fragGrid, 3,0);
        this.toPlace.add(miniText, 2,1);
        this.toPlace.add(miniGrid, 3,1);


        this.actv = ShipEnum.KeinBoot;

        this.createView();
    }

    private void createTiles(GridPane toAddGrid, ShipAddTile[] toAdd ) {
        for ( int i = 0; i < toAdd.length; i++ ) {
            ShipAddTile newTile = new ShipAddTile();
            newTile.setId(""+toAdd.length);
            newTile.setOnMouseClicked(this);
            toAdd[i] = newTile;
            toAddGrid.add(newTile, i,0);
        }
    }

    @Override
    public void handle(Event event) {
        int id = Integer.parseInt(((ShipAddTile)event.getSource()).getId());
        if ( actv != ShipEnum.KeinBoot ) {
            dismissActive();
        }
        switch (id ) {
            case 4:
                // schlachtschiff
                if ( this.model.setCopy(0)) {
                    for (ShipAddTile tile : this.schlachtTiles) {
                        tile.setClicked();
                        actv = ShipEnum.Schlachtschiff;
                    }
                }
                break;
            case 3:
                if ( this.model.setCopy(1)) {
                    for (ShipAddTile tile : this.kreuzTiles) {
                        tile.setClicked();
                        actv = ShipEnum.Kreuzer;
                    }
                }
                break;
            case 2:
                if ( this.model.setCopy(2)) {
                    for (ShipAddTile tile : this.fragTiles) {
                        tile.setClicked();
                        actv = ShipEnum.Fragette;
                    }
                }
                break;
            case 1:
                if ( this.model.setCopy(3)) {
                    for (ShipAddTile tile : this.miniTiles) {
                        tile.setClicked();
                        actv = ShipEnum.Minisuchboot;
                    }
                }
                break;
        }

    }

    private void dismissActive() {
        switch ( actv ) {
            case Schlachtschiff:
                for (ShipAddTile tile : this.schlachtTiles) {
                    tile.setDismissed();
                }
                break;
            case Kreuzer:
                for (ShipAddTile tile : this.kreuzTiles) {
                    tile.setDismissed();
                }
                break;
            case Fragette:
                for (ShipAddTile tile : this.fragTiles) {
                    tile.setDismissed();
                }
                break;
            case Minisuchboot:
                for (ShipAddTile tile : this.miniTiles) {
                    tile.setDismissed();
                }
                break;
        }
        actv = ShipEnum.KeinBoot;

    }
}
