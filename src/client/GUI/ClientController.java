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
import jdk.jfr.BooleanFlag;
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
    private String name;
    private StackPane enmTextBase;
    private Text enmText;
    private String enmName;


    private CommandLineCapsule commandLineCapsule;
    @FXML
    private Label commandLine;
    private CommandCounter counter;
    private Thread counterThread;
    private Button readyBut;
    private boolean enmFound;
    private boolean started;
    private boolean startedEnm;

    // Callback
    private Client c;


    /**
     * Setzt einen Text in die Commandline
     * @param msg ist der Text
     */
    public void setCommandLineText( String msg ) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                commandLine.setText(msg);
            }
        });
    }

    /**
     * Wird aufgerufen wen man selbst auf bereit klickt, wenn der gegner ebenso bereit ist wird {@link #startGameBothReady()} aufgerufen
     */
    private void startGame() {
        this.started = true;

        this.c.send(MessageProtocol.READY);

        if ( this.startedEnm ) {
            this.startGameBothReady();
        }
        else {
            this.c.send(MessageProtocol.FIRST);

        }

        this.readyBut.setDisable(true);
        this.setOwnFieldColorDark();
    }

    /**
     * Setzt, das der gegner gestartet hat wenn man selbst auch bereit ist, wird {@link #startGameBothReady()} aufgerufen
     */
    public void setEnmStarted() {
        this.startedEnm = true;
        if ( this.started ) {
            this.startGameBothReady();
        }
    }

    /**
     * Startet das Spiel, wenn beide bereit sind
     */
    public void startGameBothReady() {
        System.out.println("Game started");

        switchFieldColors();

        System.out.println("Already hit in start: " + this.model.getAlreadyHit());
        if ( this.model.getAlreadyHit() ) {
            System.out.println("Already hit: turn");
            this.setCommandLineText("Your enemies turn");
        }
        else {
            System.out.println("Already hit: your turn");
            this.setCommandLineText("Your turn");
        }

        String[] shipStrings = this.model.getShipsToString();

        for (String string: shipStrings) {
            System.out.println(string);
            this.c.send(string);
        }
    }

    /**
     * Setzt das Eigene Feld auf dunkel
     */
    private void setOwnFieldColorDark() {
        for (Tile[] arr: this.ownClick) {
            for ( Tile tile: arr ) {
                if (! tile.isHasShip() ) {
                    tile.setDark();
                }
            }
        }
    }

    /**
     * Dreht die Farbe der Felder um, wird nach dem Start des Spiels aufgerufen
     */
    private void switchFieldColors() {
        for (Tile[] arr: this.enmClick) {
            for ( Tile tile: arr) {
                if (! tile.isHasShip() ) {
                    tile.setNormal();
                }
            }
        }
        this.setOwnFieldColorDark();
    }

    /**
     * Setzt ob der Gegner oder der eigene Spieler dran ist
     * @param to wird auf true oder false gesetzt (true: gegner ist dran, false: du bist dran)
     */
    public void setAlreadyHit( boolean to ) {
        if ( startedEnm && started ) {
            if (to) {
                this.setCommandLineText("Your enemies turn");
            } else {
                this.setCommandLineText("Your turn");
            }
        }
        this.model.setAlreadyHit(to);
    }

    /**
     * Setzt das der Server down ist. Spieler wird zum Start Fenster geleitet
     */
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

    /**
     * Setzt das der Gegner disconnected ist, spiel wird zurückgesetzt {@link #resetModel()}
     */
    public void enmDisconnected() {
        this.resetModel();
        this.enmFound = false;
        // start counter
        this.startCommandCounterAgain();
        deactivateAddShipField(ShipEnum.Minisuchboot);
        deactivateAddShipField(ShipEnum.Schlachtschiff);
        deactivateAddShipField(ShipEnum.Fragette);
        deactivateAddShipField(ShipEnum.Kreuzer);
    }

    /**
     * Setzt das ein Gegner gefunden wurde, spiel kann beginnen
     */
    public void foundEnm() {
        this.enmFound = true;
        this.closeCommandCounter();
        resetTile(ShipEnum.Minisuchboot);
        resetTile(ShipEnum.Schlachtschiff);
        resetTile(ShipEnum.Fragette);
        resetTile(ShipEnum.Kreuzer);
    }

    /**
     * Setzt den Namen des Gegners in der GUI
     * @param name name des Gegners
     */
    public void setEnmName(String name ) {
        this.enmName = name;
        this.enmText.setText(name + "'s Spielfeld");
    }

    /**
     * Setzt den eigenen Namen in der GUI
     * @param name Name
     */
    public void setName ( String name ) {
        this.name = name;
        this.eigText.setText(name + "'s Spielfeld");
    }

    /**
     * @return den eigenen Namen
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setzt ein Schiff des gegners
     * @param text ist ein String wie !SHIP ....
     */
    public void setEnmShip( String text) {
        if ( this.model.setEnmShip(text) ) {
            System.out.println("All Ships shiped");
        }
    }


    /**
     * Wird aufgeruden wenn das eigene Feld angeklickt wird
     * @param currentTile das Feld das geklickt wurde
     * @param mouseEvent mouseEvent, mit dem überprüft wird, ob Links- oder Rechtsklick
     */
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

    /**
     * Wenn alle Schiffe gesetzt wurden, wird diese Methode aufgerufen. Deaktiviert die Tiles
     * @param which welche Schiff Tiles
     */
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

    /**
     * Setzt alle Tiles auf Dismissed
     * @param which welche Schiff Tiles dismissed werden sollen
     */
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

    /**
     * Setzt den Text einen Schiffes das man Plszieren kann neu
     * @param which welches Schiff
     * @param change ob eins gesetzt wurde (-1), oder ob es wieder gelöscht -> frei ist (1)
     */
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

    /**
     * Wird aufgerufen wenn das Feld das Gegners angeklickt wird
     * @param currentTile Tile angeklickt wurde
     */
    private void enmTileClicked( Tile currentTile ) {
        if ( started && startedEnm ) {
            System.out.println("Already Hit: " + !this.model.getAlreadyHit());
            if ( !this.model.getAlreadyHit() ) {
                // do
                System.out.println("x: " + currentTile.getX());
                System.out.println("y: " + currentTile.getY());

                int x = currentTile.getX();
                int y = currentTile.getY();
                boolean trueHit = this.model.setEnmTileHit(x,y);
                this.c.send(MessageProtocol.HIT + " " + trueHit + ": " + x + ", " + y);
                this.setCommandLineText("Your enemies turn");

                if ( this.model.checkAllShipDestroyed()) {
                    // gewonnen
                    this.setWin();
                }
            }
        }
        else {
            System.out.println("Not your turn!");
        }

    }

    /**
     * Setzt den lose
     */
    public void setLose() {
        // damit keine züge mehr möglich sind
        this.started = false;
        this.enmFound = false;

        // beide Felder grau
        this.switchFieldColors();
        this.setOwnFieldColorDark();

        this.commandLineCapsule.setText("Du hast verloren! " + this.enmName + " hat gewonnen! Vielleich nächstes Mal :)", true);
    }

    /**
     * Wird aufgerufen wenn die win Bedingung erfüllt wurde
     * Leitet das auch an den gegner weiter
     */
    private void setWin() {
        // damit keine züge mehr möglich sind
        this.started = false;
        this.enmFound = false;

        // beide Felder grau
        this.switchFieldColors();
        this.setOwnFieldColorDark();

        this.commandLineCapsule.setText(this.name + " hast gewonnen!!!", true);

        this.c.send(MessageProtocol.LOSE);
    }

    /**
     * Setzt ein Feld auf hit, wird aufgerufen wenn der gegner einen Hit schickt über {@link Client}
     * @param msg sieht flgendermaßen aus: "!HIT ture: 0, 2
     */
    public void setTileHit( String msg ) {
        // !HIT true: 0, 2
        try {
            int x = Integer.parseInt(msg.substring(msg.indexOf(":")+2,msg.indexOf(",")));
            int y = Integer.parseInt(msg.substring(msg.indexOf(",")+2));
            String trueHitString = msg.substring(msg.indexOf(" ")+1, msg.indexOf(":"));
            boolean trueHit = false;
            if ( trueHitString.equals("true") ) {
                trueHit = true;
            }
            System.out.println("TrueHit: " + trueHit);
            this.model.setOwnTileHit(x, y, trueHit);
            this.setCommandLineText("Your turn!");
            System.out.println("Alread Your turn");
        }
        catch (Exception ex ) {
            ex.printStackTrace();
        }
    }


    /**
     * Wird aufgerufen wenn die view created wird. Erstellt alle GUI elemente
     */
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


    /**
     * Init methode um ein Callback an den {@link Client} zu setzen
     * @param c Controller {@link Client}
     */
    public void init(Client c){
        this.c = c;
    }

    /**
     * Standart initialize methode, werden ebenso startwerte und Elemente der GUI gesetzt
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        model = new ClientModel();


        started = false;
        startedEnm = false;
        enmFound = false;

        commandLineCapsule = new CommandLineCapsule(this.commandLine);
        counter = new CommandCounter(commandLineCapsule);
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

    /**
     * Generiert die Tiles
     * @param toAddGrid Grid zu dem die Tile geaddet werden
     * @param toAdd ist das Array in das das Tile gesezt wird
     */
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

    /**
     * Nimmt einen Int wert entgegen
     * @param id der int wert
     * @return gibt das entsprechende {@link ShipEnum} const zurück
     */
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

    /**
     * Wird aufgerufen wenn man ein Schiff platzieren will und auf die Schiff Tiles klickt
     */
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

    /**
     * Handelt die onclicks auf die Schiff tiles
     * @param event Event des Onclicks
     */
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


    /**
     * Setzt die entsprechenden Schiff Tiles auf dismissed
     * @param which welche Schiff tiles man dismissen will
     */
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

    /**
     * Setzt das Model und die GUI und den Startzustand
     */
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

    /**
     * Setzt den Counter für die Spielersuche auf stop
     */
    public void closeCommandCounter() {
        this.commandLineCapsule.stop();
        this.counter.stop();
        try {
            this.counterThread.join();
            System.out.println("Closing Thread");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    commandLine.setText("");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Startet den Counter für die SPielersuche wieder
     */
    public void startCommandCounterAgain() {
        System.out.println("Starting again");
        this.counter.startAgain();

        this.commandLineCapsule.start();
        this.counter = new CommandCounter(commandLineCapsule);
        counterThread = new Thread(counter);
        counterThread.setDaemon(true);
        counterThread.start();
    }


    /**
     * Counter für die Spielersuche
     */
    class CommandCounter implements Runnable {

        private CommandLineCapsule line;
        private int min;
        private int sec;

        private boolean running;

        public CommandCounter( CommandLineCapsule line) {
            this.line = line;
            this.running = true;
            min = 0;
            sec = 0;
        }

        /**
         * Startet den Counter wieder
         */
        public void startAgain() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    line.setText("Enemy Player disconnected! Trying to lookout for new Players", true);
                }
            });
        }

        /**
         * Stopt den Couter
         */
        public void stop() {
            this.running = false;
            this.line.stop();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    line.setText("Player found!", true);
                    running = false;
                }
            });
        }

        /**
         * Run methode, laufender Counter
         */
        @Override
        public void run() {
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
                            line.setText("Searching for Players: " + String.format("%02d", min) + ":"+String.format("%02d", sec), false);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
