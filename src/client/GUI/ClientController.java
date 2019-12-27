package client.GUI;

import client.Client;
import client.GUI.Ships.*;
import client.GUI.Start.ClientConnect;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import msg.MessageProtocol;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @FXML
    private Label commandLine;
    private CommandCounter counter;
    private Thread counterThread;
    private Button readyBut;
    private boolean enmFound;
    private boolean started;
    private boolean startedEnm;
    private boolean shipsSet;

    // Callback
    private Client c;

    private void startGame() {
        this.started = true;

        this.c.send(MessageProtocol.READY);

        try {
            if ( this.startedEnm ) {
                this.startGameBothReady();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        this.readyBut.setDisable(true);
    }

    public void setEnmStarted() {
        this.startedEnm = true;
        if ( this.started ) {
            this.startGameBothReady();
        }
    }

    public void startGameBothReady() {
        System.out.println("Game started");

        switchFieldColors();

        String[] shipStrings = this.model.getShipsToString();

        for (String string: shipStrings) {
            System.out.println(string);
            this.c.send(string);
        }
    }

    private void switchFieldColors() {
        for (Tile[] arr: this.enmClick) {
            for ( Tile tile: arr) {
                if (! tile.isHasShip() ) {
                    tile.setNormal();
                }
            }
        }
        for (Tile[] arr: this.ownClick) {
            for ( Tile tile: arr ) {
                if (! tile.isHasShip() ) {
                    tile.setDark();
                }
            }
        }
    }

    public void srvDisconnected() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Start/start.fxml"));
            Parent root = null;
            root = loader.load();
            Scene scene = new Scene(root);

            ClientConnect controller = loader.getController();
            controller.initCommandLine("Verbindung zum Server unterbrochen!");
            Stage stage = (Stage) fieldEig.getScene().getWindow();
            // Swap screen
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enmDisconnected() {
        this.resetModel();
        this.enmFound = false;
        this.shipsSet = true;
        // start counter
        this.startCommandCounterAgain();
        deactivateAddShipField(ShipEnum.Minisuchboot);
        deactivateAddShipField(ShipEnum.Schlachtschiff);
        deactivateAddShipField(ShipEnum.Fragette);
        deactivateAddShipField(ShipEnum.Kreuzer);
    }

    public void foundEnm() {
        this.enmFound = true;
        this.closeCommandCounter();
        resetTile(ShipEnum.Minisuchboot);
        resetTile(ShipEnum.Schlachtschiff);
        resetTile(ShipEnum.Fragette);
        resetTile(ShipEnum.Kreuzer);
    }


    public void setEnmShip( String text) {
        if ( this.model.setEnmShip(text) ) {
            System.out.println("All Ships shiped");
        }
    }


    private void ownTileClicked( Tile currentTile, MouseEvent mouseEvent ) {
        if ( !started ) {
            System.out.println("Selected: " +actv);
            if ( actv != ShipEnum.KeinBoot ) {
                if ( currentTile.isHasShip() ) {
                    ShipEnum shipEnum = actv;
                    dismissActive(actv);
                    ownTileClicked(currentTile, mouseEvent);
                    System.out.println("Selected end: " +actv);
                }
                else {
                    if ( model.addShip(actv, currentTile.getX(), currentTile.getY()) ) {
                        ShipEnum tempEnm = actv;
                        this.model.setShipsBorder(true);
                        // first ship added
                        this.model.setFirstShipAdded(true);
                        resetText(tempEnm, -1);
                    }
                    if ( model.allShipsPlaced() ) {
                        readyBut = new Button("Ready?");
                        readyBut.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                // komm nocht
                                startGame();
                            }
                        });
                        toPlace.add(readyBut, 4,0, 2,2);
                    }
                }
            }
            else {
                if ( currentTile.isHasShip() ) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY)
                    {
                        // Remove ship
                        ShipEnum currentDeleted = model.removeShip(currentTile);
                        resetText(currentDeleted, 1);
                        this.model.cleanGridFromRed();
                        // Only temp, will bi changed in dismissActive
                        actv = currentDeleted;
                        dismissActive(actv);
                        actv = currentDeleted;
                        setActivated();
                        model.setShipsBorder(true);

                        if (toPlace.getChildren().indexOf(readyBut) != -1 ) {
                            toPlace.getChildren().remove(readyBut);
                        }
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY)
                    {
                        this.model.cleanGridFromRed();
                        model.turnShip(currentTile);
                    }
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

    private void resetTile( ShipEnum which ) {
        switch ( which ) {
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
    }

    private void resetText( ShipEnum which, int change ) {
        int anz = 0;
        switch ( which ) {
            case Schlachtschiff:
                anz = Integer.parseInt(schlachtText.getText().substring(0,1));
                if ( anz == 0 ) {
                    resetTile(which);
                }
                anz += change;
                this.schlachtText.setText(anz + "x Schlachtschiff");
                if ( anz == 0 ) {
                    deactivateAddShipField(ShipEnum.Schlachtschiff);
                }
                break;
            case Kreuzer:
                anz = Integer.parseInt(kreuzerText.getText().substring(0,1));
                if ( anz == 0 ) {
                    resetTile(which);
                }
                anz += change;
                this.kreuzerText.setText(anz + "x Kreuzer");
                if ( anz == 0 ) {
                    deactivateAddShipField(ShipEnum.Kreuzer);
                }
                break;
            case Fragette:
                anz = Integer.parseInt(fragText.getText().substring(0,1));
                if ( anz == 0 ) {
                    resetTile(which);
                }
                anz += change;
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
                anz = Integer.parseInt(miniText.getText().substring(0,1));
                if ( anz == 0 ) {
                    resetTile(which);
                }
                anz += change;
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
        if ( anz == 0 ) {
            this.model.setShipsBorder(false);
        }
    }

    private void enmTileClicked( Tile currentTile ) {
        if ( started && startedEnm ) {
            // do
            System.out.println("x: " + currentTile.getX());
            System.out.println("y: " + currentTile.getY());

            int x = currentTile.getX();
            int y = currentTile.getY();
            this.model.setEnmTileHit(x-1,y-1);
            this.c.send(MessageProtocol.HIT + " " + x + ", " + y);
        }

    }

    public void setTileHit( String msg ) {
        // !HIT 0, 2
        int x = Integer.parseInt(msg.substring(msg.indexOf(" ")+1,msg.indexOf(",")));
        int y = Integer.parseInt(msg.substring(msg.indexOf(",")+2));

        this.model.setOwnTileHit(x-1,y-1);
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



    public void init(Client c){
        this.c = c;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        model = new ClientModel();


        started = false;
        startedEnm = false;
        enmFound = false;

        counter = new CommandCounter(this.commandLine);
        counterThread = new Thread(counter);
        counterThread.setDaemon(true);
        counterThread.start();

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
            newTile.setDeactivated();
            newTile.setId(""+toAdd.length);
            newTile.setOnMouseClicked(this);
            toAdd[i] = newTile;
            toAddGrid.add(newTile, i,0);
        }
    }

    private ShipEnum getEnumToId( int id ){
        ShipEnum out = ShipEnum.KeinBoot;
        switch (id ) {
            case 4:
                // schlachtschiff
                out = ShipEnum.Schlachtschiff;
                break;
            case 3:
                out = ShipEnum.Kreuzer;
                break;
            case 2:
                out = ShipEnum.Fragette;
                break;
            case 1:
                out = ShipEnum.Minisuchboot;
                break;
        }

        return out;
    }

    private void setActivated() {
        boolean setRed = true;
        boolean setWhite = false;
        switch ( actv ) {
            case Schlachtschiff:
                // schlachtschiff
                if ( this.model.setCopy(0)) {
                    for (ShipAddTile tile : this.schlachtTiles) {
                        if ( tile.isSet()) {
                            tile.setDismissed();
                            setWhite = true;
                            actv = ShipEnum.KeinBoot;
                        }
                        else {
                            tile.setClicked();
                            actv = ShipEnum.Schlachtschiff;
                        }
                    }
                }
                else {
                    setRed = false;
                }
                break;
            case Kreuzer:
                if ( this.model.setCopy(1)) {
                    for (ShipAddTile tile : this.kreuzTiles) {
                        if ( tile.isSet()) {
                            tile.setDismissed();
                            setWhite = true;
                            actv = ShipEnum.KeinBoot;
                        }
                        else {
                            tile.setClicked();
                            actv = ShipEnum.Kreuzer;
                        }
                    }
                }
                else {
                    setRed = false;
                }
                break;
            case Fragette:
                if ( this.model.setCopy(2)) {
                    for (ShipAddTile tile : this.fragTiles) {
                        if ( tile.isSet()) {
                            tile.setDismissed();
                            setWhite = true;
                            actv = ShipEnum.KeinBoot;
                        }
                        else {
                            tile.setClicked();
                            actv = ShipEnum.Fragette;
                        }
                    }
                }
                else {
                    setRed = false;
                }
                break;
            case Minisuchboot:
                if ( this.model.setCopy(3)) {
                    for (ShipAddTile tile : this.miniTiles) {
                        if ( tile.isSet()) {
                            tile.setDismissed();
                            setWhite = true;
                            actv = ShipEnum.KeinBoot;
                        }
                        else {
                            tile.setClicked();
                            actv = ShipEnum.Minisuchboot;
                        }
                    }
                }
                else {
                    setRed = false;
                }
                break;
        }
        if ( setRed ) {
            this.model.setShipsBorder(true);
        }
        if ( setWhite ){
            this.model.setShipsBorder(false);
        }
    }

    @Override
    public void handle(Event event) {
        if ( !started && enmFound ) {
            int id = Integer.parseInt(((ShipAddTile)event.getSource()).getId());

            ShipEnum actvTemp = actv;
            if ( actvTemp != ShipEnum.KeinBoot && actvTemp != getEnumToId(id) ) {
                dismissActive(actvTemp);
            }
            actv = getEnumToId(id);

            setActivated();
        }
    }



    private void dismissActive( ShipEnum which ) {
        switch ( which ) {
            case Schlachtschiff:
                for (ShipAddTile tile : this.schlachtTiles) {
                    if ( !tile.isDeact()) {
                        tile.setDismissed();
                    }
                }
                break;
            case Kreuzer:
                for (ShipAddTile tile : this.kreuzTiles) {
                    if ( !tile.isDeact()) {
                        tile.setDismissed();
                    }
                }
                break;
            case Fragette:
                for (ShipAddTile tile : this.fragTiles) {
                    if ( !tile.isDeact()) {
                        tile.setDismissed();
                    }
                }
                break;
            case Minisuchboot:
                for (ShipAddTile tile : this.miniTiles) {
                    if ( !tile.isDeact()) {
                        tile.setDismissed();
                    }
                }
                break;
        }
        actv = ShipEnum.KeinBoot;

    }

    private void resetModel() {
        this.started = false;
        this.enmFound = false;
        this.model.resetValues();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (toPlace.getChildren().indexOf(readyBut) != -1 ) {
                    toPlace.getChildren().remove(readyBut);
                }
                schlachtText.setText("1x Schlachtschiff");
                kreuzerText.setText("2x Kreuzer");
                fragText.setText("3x Fragetten");
                miniText.setText("4x Minisuchboote");
                fieldEig.getChildren().clear();
                fieldEnm.getChildren().clear();
                System.out.println("Removed");
                createView();
                System.out.println("View created");
            }
        });
    }

    public void closeCommandCounter() {
        this.counter.stop();
    }

    public void startCommandCounterAgain() {
        this.counter.startAgain();
        this.counter = new CommandCounter(this.commandLine);
        counterThread = new Thread(counter);
        counterThread.setDaemon(true);
        counterThread.start();
    }


    class CommandCounter implements Runnable {

        private Label line;
        private int min;
        private int sec;

        private boolean running;

        public CommandCounter( Label line) {
            this.line = line;
            min = 0;
            sec = 0;
        }

        public void startAgain() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    line.setText("Enemy Player disconnected");
                }
            });
            try {
                Thread.sleep(1000);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        line.setText("Try to lookout for new Players");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            this.running = false;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    line.setText("Player found!");
                }
            });
            try {
                Thread.sleep(1000);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        line.setText("");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            this.running = true;
            while ( running ) {
                try {
                    Thread.sleep(1000);
                    sec++;
                    if ( sec == 60 ) {
                        min++;
                        sec = 0;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            line.setText("Searching for Players: " + String.format("%02d", min) + ":"+String.format("%02d", sec));
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
