package client.GUI;

import javafx.scene.layout.GridPane;
import client.GUI.Ships.*;
import msg.MessageProtocol;

import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.net.ssl.HostnameVerifier;
import java.awt.*;
import java.security.KeyRep;
import java.security.spec.ECField;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

public class ClientModel
{

    private static final int VERTICAL = 1;
    private static final int HORIZONTAL = 0;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private Ship[][] ships; // [0]: Schlachtschiff, [1]: Kreuzer, [2]: Fregatten, [3]: Minisuchboot
    private Ship[][] enmShips; // [0]: Schlachtschiff, [1]: Kreuzer, [2]: Fregatten, [3]: Minisuchboot
    private int enmShipsAdded;
    private int copy;
    private boolean firstShipAdded;
    private boolean turning;

    private boolean alreadyHit;

    private Tile[][] actualGridEig;
    private Tile[][] actualGridEnm;

    // Setter / Getter
    public boolean isTurning() {
        return turning;
    }

    public void setTurning(boolean turning) {
        this.turning = turning;
    }

    public boolean isFirstShipAdded() {
        return firstShipAdded;
    }

    public void setFirstShipAdded(boolean firstShipAdded) {
        this.firstShipAdded = firstShipAdded;
    }

    public int getCopy() {
        return copy;
    }

    /**
     * @param copy:
     *          0: Schlachtschiff
     *          1: Kreuzer
     *          2: Fragette
     *          3: Minisuchboot
     */
    public boolean setCopy(int copy) {
        boolean out = false;
        switch ( copy ) {
            case 0:
                out = activeLastOne(ships[0]);
                System.out.println(out);
                break;
            case 1:
                out = activeLastOne(ships[1]);
                break;
            case 2:
                out = activeLastOne(ships[2]);
                break;
            case 3:
                out = activeLastOne(ships[3]);
                break;
        }
        if ( out ) {
            this.copy = copy;
        }

        return out;
    }

    /**
     * Wird aufgerufen um ein Schiff zu plazieren, aktiviert das letzte in dem array arr
     * @param arr ist das Array
     * @return true wenn der Vorgang funktioniet hat, false wenn anders
     */
    private boolean activeLastOne( Ship[] arr ) {
        boolean out = false;
        for (int i = 0; i < arr.length; i++ ) {
            if ( !arr[i].isPlaced()) {
                out = true;
            }
        }

        return out;
    }

    /**
     * @return gibt ein Array mit String für jedes Schiff zurück {@link #getShipsToStringSing(Ship)} )}
     */
    public String[] getShipsToString() {
        String[] out = new String[10];

        int i = 0;
        for (Ship[] shipArr : this.ships) {
            for (Ship ship : shipArr) {
                out[i] = this.getShipsToStringSing(ship);
                i++;
            }
        }

        return out;
    }

    /**
     * @param ship ist das Schiff das in einen String umgewandelt wird
     * @return gibt den String für ein Schiff zurück, dieser String wird dann von Client weiter geschickt
     */
    private String getShipsToStringSing(Ship ship) {
        return MessageProtocol.SHIP + " " + ship.getClass() + ": " + ship.getStartX() + ", " + ship.getStartY() + "; " + ship.getEndX() + ", " + ship.getEndY();
    }

    /**
     * @return Gibt true zurück wenn alle Schiffe des Gegners zerstört sind
     */
    public boolean checkAllShipDestroyed() {
        for (Ship[] arr: this.enmShips) {
            for (Ship ship: arr) {
                if ( !ship.isDestroyed() ) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Hit eigenes Feld
     * @param x x wert
     * @param y y wert
     * @param trueHit ob true hit oder nicht
     */
    public void setOwnTileHit( int x, int y, boolean trueHit ) {
        this.alreadyHit = false;
        this.actualGridEig[x-1][y-1].setHit(trueHit);
        if ( trueHit ) {
            checkIfHoleShipHit(x,y, this.actualGridEig, true);
        }
    }

    public boolean getAlreadyHit() {
        return this.alreadyHit;
    }
    public void setAlreadyHit( boolean to ) {
        System.out.println("Set already hit to " + to);
        this.alreadyHit = to;
    }

    /**
     * Hit gegner Feld
     * @param x x wert
     * @param y x wert
     * @return ob true hit oder nicht
     */
    public boolean setEnmTileHit( int x, int y ) {
        this.alreadyHit = true;
        boolean trueHit = checkIfOnEnmField(x,y);
        System.out.println(trueHit);
        this.actualGridEnm[x-1][y-1].setHit(trueHit);
        if ( trueHit ) {
            // check if hole ship hit
            checkIfHoleShipHit(x,y, this.actualGridEnm, false);
        }
        return trueHit;
    }

    /**
     * @param x x wert
     * @param y y wert
     * @param shipsArr ist das Array mit allen Schiffen
     * @return Gibt ein Ship von den bekommenen Kordinaten zurück
     */
    private Ship getShipFromCord( int x, int y,  Ship[][] shipsArr ) {
        for (Ship[] arr: shipsArr) {
            for (Ship ship: arr) {
                int startX = ship.getStartX();
                if ( startX == ship.getEndX() ) {
                    if ( startX == x ) {
                        // vertical
                        for ( int i = ship.getStartY(); i <= ship.getEndY(); i++ ) {
                            if ( i == y ) {
                                return ship;
                            }
                        }
                    }
                }
                else {
                    // horizontal
                    if ( ship.getStartY() == y ) {
                        for ( int i = ship.getStartX(); i <= ship.getEndX(); i++ ) {
                            if ( i == x ) {
                                return ship;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Schaut ob ein ganzes Schiff zerstört ist, wenn ja werden dessen Tiles besonders markiert
     * @param x x wert
     * @param y y wert
     * @param arr Array der Tiles
     * @param own ob es sich um die Eigenen Schiffe oder die des Gegners Handelt
     */
    private void checkIfHoleShipHit( int x, int y, Tile[][] arr, boolean own ) {
        Ship currentShip;
        if ( !own ) {
            currentShip = this.getShipFromCord(x,y, this.enmShips);
        }
        else
        {
            currentShip = this.getShipFromCord(x,y, this.ships);
        }
        boolean allHit = true;
        if ( currentShip != null ) {
            int startX = currentShip.getStartX();
            if ( startX == currentShip.getEndX() ) {
                // vertical
                for ( int i = currentShip.getStartY(); i <= currentShip.getEndY(); i++ ) {
                    if ( !arr[startX-1][i-1].isAlreadyHit() )  {
                        allHit = false;
                    }
                }
                if ( allHit ) {
                    currentShip.setDestroyed(true);
                    for ( int i = currentShip.getStartY(); i <= currentShip.getEndY(); i++ ) {
                        arr[startX-1][i-1].setEndHit();
                    }
                }
            }
            else
            {
                // hoizontal
                for ( int i = currentShip.getStartX(); i <= currentShip.getEndX(); i++ ) {
                    if ( !arr[i-1][currentShip.getStartY()-1].isAlreadyHit() )  {
                        allHit = false;
                    }
                }
                if ( allHit ) {
                    currentShip.setDestroyed(true);
                    for ( int i = currentShip.getStartX(); i <= currentShip.getEndX(); i++ ) {
                        arr[i-1][currentShip.getStartY()-1].setEndHit();
                    }
                }
            }
        }
    }

    /**
     * Schaut ob ein Schiff auf dem Feld ist
     * @param x x wert
     * @param y y wert
     * @param arr Array der Schifft
     * @return true wenn ja, false wenn nein
     */
    private boolean checkIfOnField( int x, int y, Ship[] arr ) {
        for (Ship ship: arr) {
            int startX = ship.getStartX();
            int endX = ship.getEndX();

            if ( startX == endX ) {
                // vertical
                // important: y!
                if ( x == startX ) {
                    for (int i = ship.getStartY(); i <= ship.getEndY(); i++ ) {
                        if ( i == y ) {
                            // hit
                            return true;
                        }
                    }
                }
            }
            else {
                // horizontal
                // important: x!
                if ( y ==  ship.getStartY() ) {
                    for (int i = ship.getStartX(); i <= ship.getEndX(); i++ ) {
                        if ( i == x ) {
                            // hit
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param x x wert
     * @param y y wert
     * @return {@link #checkIfOnField(int, int, Ship[])}
     */
    private boolean checkIfOnEnmField( int x, int y ) {
        for (Ship[] arr: this.enmShips) {
            if ( checkIfOnField(x,y, arr) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Setzt ein Schiff des Gegners in das eigene Array
     * @param text ist ein String nach "!SHIP Klasse: " + ship.getStartX() + ", " + ship.getStartY() + "; " + ship.getEndX() + ", " + ship.getEndY()
     * @return true wenn alle Schiffe hinzugefügt wurden
     */
    public boolean setEnmShip( String text) {
        // Aufgebaut nach: "!SHIP Klasse: " + ship.getStartX() + ", " + ship.getStartY() + "; " + ship.getEndX() + ", " + ship.getEndY();
        System.out.println("Text: " +text);
        text = text.substring(text.indexOf(".")+1);
        text = text.substring(text.indexOf(".")+1);
        String type = text.substring(text.indexOf(".")+1,text.indexOf(":"));
        text = text.substring(text.indexOf(":")+1);
        int starX = Integer.parseInt(text.substring(1,text.indexOf(",")));
        text = text.substring(text.indexOf(",")+1);
        int startY = Integer.parseInt(text.substring(1, text.indexOf(";")));
        text = text.substring(text.indexOf(";")+1);
        int endX = Integer.parseInt(text.substring(1, text.indexOf(",")));
        text = text.substring(text.indexOf(",")+1);
        int endY = Integer.parseInt(text.substring(1));

        int inArr = 0;
        switch (type) {
            case "Schlachtschiff":
                inArr = 0;
                break;
            case "Kreuzer":
                inArr = 1;
                break;
            case "Fregatte":
                inArr = 2;
                break;
            case "Minisuchboot":
                inArr = 3;
                break;
        }
        for (Ship ship: this.enmShips[inArr]) {
            if ( !ship.isPlaced() ) {
                ship.setEndY(endY);
                ship.setEndX(endX);
                ship.setStartY(startY);
                ship.setStartX(starX);
                ship.setPlaced(true);

                break;
            }
        }
        enmShipsAdded++;

        if ( enmShipsAdded == 10 && this.checkEnmShipAll() ) {
            return true;
        }

        return false;
    }

    /**
     * Schaut ob alle Schiffe des gegners Plaziert sind
     * @return true wenn ja, false wenn nein
     */
    private boolean checkEnmShipAll()  {
        for (Ship[] shipArr: this.enmShips) {
            for (Ship ship: shipArr) {
                if ( !ship.isPlaced() ) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Konstruktor
     */
    public ClientModel() {
        this.copy = -1;
        ships = new Ship[4][];
        ships[0] = new Schlachtschiff[1];
        ships[1] = new Kreuzer[2];
        ships[2] = new Fregatte[3];
        ships[3] = new Minisuchboot[4];

        enmShipsAdded = 0;
        enmShips = new Ship[4][];
        enmShips[0] = new Schlachtschiff[1];
        enmShips[1] = new Kreuzer[2];
        enmShips[2] = new Fregatte[3];
        enmShips[3] = new Minisuchboot[4];


        createShips();
        actualGridEig = new Tile[10][10];
        actualGridEnm = new Tile[10][10];
        firstShipAdded = false;
        turning = false;
    }

    /**
     * Restets die Values des Models
     */
    public void resetValues() {
        this.copy = -1;
        ships = new Ship[4][];
        ships[0] = new Schlachtschiff[1];
        ships[1] = new Kreuzer[2];
        ships[2] = new Fregatte[3];
        ships[3] = new Minisuchboot[4];

        createShips();
        actualGridEig = new Tile[10][10];
        actualGridEnm = new Tile[10][10];
        firstShipAdded = false;
        turning = false;
    }

    /**
     * @param type 0: most left tile, 1: most right tile
     * @param currentTile
     * @return
     */
    private Tile find ( int type, Tile currentTile ) {
        if ( type == LEFT ) {
            // find most left tile
            Tile leftTile;
            if ( currentTile.getBeforeTile() != null ) {
                leftTile = currentTile.getBeforeTile();
                while ( true ) {
                    if ( leftTile.getBeforeTile() == null ) {
                        break;
                    }
                    leftTile = leftTile.getBeforeTile();
                }
            }
            else {
                leftTile = currentTile;
            }
            return leftTile;
        }
        else {
            Tile rightTile;
            if ( currentTile.getNextTile() != null ) {
                rightTile = currentTile.getNextTile();
                while ( true ) {
                    if ( rightTile.getNextTile() == null ) {
                        break;
                    }
                    rightTile = rightTile.getNextTile();
                }
            }
            else {
                rightTile = currentTile;
            }
            return rightTile;
        }
    }

    /**
     * Dreht das Schiff von vertical auf horizontal oder umgekehrt, wenn der weg unten blokiert ist, dann nicht
     * @param currentTile
     */
    public void turnShip( Tile currentTile ) {
        if ( !turning ) {

            this.turning = true;
            // find most left tile
            Tile leftTile = find(LEFT, currentTile);
            // find most right tile
            Tile rightTile = find(RIGHT, currentTile);

            // measure distance
            int len;
            int orentation; // 0: horizontal, 1: vertical
            if (rightTile.getX() == leftTile.getX()) {
                len = rightTile.getY() - leftTile.getY() + 1;
                orentation = VERTICAL;
            } else {
                len = rightTile.getX() - leftTile.getX() + 1;
                orentation = HORIZONTAL;
            }
            if (len > 1) {
                // also could be lowerst tile
                Tile workTile = rightTile;
                int toEditShip = 0;
                switch (len) {
                    case 4:
                        toEditShip = 0;
                        break;
                    case 3:
                        toEditShip = 1;
                        break;
                    case 2:
                        toEditShip = 2;
                        break;
                    // 1 not needed, to turn function
                }
                if (orentation == VERTICAL) {
                    if (checkFree(leftTile, len, VERTICAL)) {
                        this.ships[toEditShip][workTile.getShipArrayIndex()].setEndX(workTile.getX() + len - 1);
                        this.ships[toEditShip][workTile.getShipArrayIndex()].setEndY(workTile.getY() - len + 1);
                        for (int j = len - 1; j > 0; j--) {
                            Tile after = null;
                            Tile before = this.actualGridEig[workTile.getX() + j - 2][workTile.getY() - j - 1];
                            if (j != len - 1) {
                                after = this.actualGridEig[workTile.getX() + j][workTile.getY() - j - 1];
                            }
                            this.actualGridEig[workTile.getX() + j - 1][workTile.getY() - j - 1].setHasShip(true, after, before, workTile.getShipArrayIndex());

                            Tile temp = workTile.getBeforeTile();
                            workTile.setHasShip(false, null, null, -1);
                            workTile = temp;


                        }
                        // set first elment
                        this.actualGridEig[workTile.getX() - 1][workTile.getY() - 1].setHasShip(true, this.actualGridEig[workTile.getX()][workTile.getY() - 1], null, this.actualGridEig[workTile.getX()][workTile.getY() - 1].getShipArrayIndex());
                    }
                    else {
                        borderTurn();
                    }
                } else {
                    if (checkFree(leftTile, len, HORIZONTAL)) {
                        this.ships[toEditShip][workTile.getShipArrayIndex()].setEndX(workTile.getX() - len + 1);
                        this.ships[toEditShip][workTile.getShipArrayIndex()].setEndY(workTile.getY() + len - 1);
                        for (int j = len - 1; j > 0; j--) {
                            Tile after = null;
                            Tile before = this.actualGridEig[workTile.getX() - j - 1][workTile.getY() + j - 2];
                            if (j != len - 1) {
                                after = this.actualGridEig[workTile.getX() - j - 1][workTile.getY() + j];
                            }
                            this.actualGridEig[workTile.getX() - j - 1][workTile.getY() + j - 1].setHasShip(true, after, before, workTile.getShipArrayIndex());

                            Tile temp = workTile.getBeforeTile();
                            workTile.setHasShip(false, null, null, -1);
                            workTile = temp;

                        }
                        // set first elment
                        this.actualGridEig[workTile.getX() - 1][workTile.getY() - 1].setHasShip(true, this.actualGridEig[workTile.getX() - 1][workTile.getY()], null, this.actualGridEig[workTile.getX() - 1][workTile.getY()].getShipArrayIndex());
                    }
                    else {
                        borderTurn();
                    }
                }
            }
            this.setTurning(false);
        }
    }

    /**
     * Setzt die roten Ränder der Schiffe beim Drehen
     */
    private void borderTurn() {
        this.setShipsBorder(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.setShipsBorder(false);
    }

    /**
     * Löscht alle roten Ränder um die Schiffe
     */
    public void cleanGridFromRed() {
        for ( int i = 0; i < 10; i++ ) {
            for ( int j = 0; j < 10; j++ ) {
                if ( !this.actualGridEig[i][j].isHasShip() ) {
                    this.actualGridEig[i][j].setNormal();
                }
            }
        }
    }

    /**
     * Checkt ob alle eigenen Schiffe Plaziert sind
     * @return true wenn ja, false wenn nein
     */
    public boolean allShipsPlaced() {
        for (Ship[] ship : this.ships) {
            for (Ship shipSing : ship) {
                if (!shipSing.isPlaced()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Setzt die Ränder der Schiffe
     * @param red wenn true ja rot, wenn false löscht es sie wieder
     */
    public void setShipsBorder( boolean red ) {
        if ( firstShipAdded) {
            for ( Ship[] shipsArr: this.ships) {
                for ( Ship ship : shipsArr ) {
                    int x = ship.getStartX();
                    int y = ship.getStartY();

                    int endX = ship.getEndX();
                    int endY = ship.getEndY();

                    int len = ship.getLength();

                    int orientation;

                    if ( x == endX ) {
                        orientation = VERTICAL;
                    }
                    else {
                        orientation = HORIZONTAL;
                    }
                    if ( ship.isPlaced() ) {
                        for ( int i = -1; i <= len; i++ ) {
                            if ( orientation == VERTICAL ) {
                                if ( i == -1  ) {
                                    if ( y-2 >= 0 ) {
                                        if ( red ) {
                                            if ( !this.actualGridEig[x-1][y-2].isRedBlockSet()) {
                                                this.actualGridEig[x-1][y-2].setRedBlock();
                                            }
                                        }
                                        else {
                                            if ( this.actualGridEig[x-1][y-2].isRedBlockSet()) {
                                                this.actualGridEig[x - 1][y - 2].resetRedBlock();
                                            }
                                        }
                                    }

                                }
                                else if ( i == len && y-1+i < 10 && y-1+i >= 0 ) {
                                    if ( red ) {
                                        if ( !this.actualGridEig[x-1][y-1+i].isRedBlockSet()) {
                                            this.actualGridEig[x-1][y - 1+i].setRedBlock();
                                        }
                                    }
                                    else {
                                        if ( this.actualGridEig[x-1][y-1+i].isRedBlockSet()) {
                                            this.actualGridEig[x-1][y - 1+i].resetRedBlock();
                                        }
                                    }
                                }
                                if ( x - 2 >= 0 && y-1+i < 10 && y-1+i >= 0) {
                                    if ( red ) {
                                        System.out.println("Red block set: " + this.actualGridEig[x - 2][y-1+ i].isRedBlockSet());
                                        if ( !this.actualGridEig[x - 2][y-1+ i].isRedBlockSet()) {
                                            this.actualGridEig[x - 2][y -1 + i].setRedBlock();
                                        }
                                    }
                                    else {
                                        if ( this.actualGridEig[x - 2][y - 1 + i].isRedBlockSet()) {
                                            this.actualGridEig[x - 2][y - 1+ i].resetRedBlock();
                                        }
                                    }
                                }
                                if ( x < 10 && y-1+i < 10 && y-1+i >= 0 ) {
                                    if ( red ) {
                                        if ( !this.actualGridEig[x][y-1+ i].isRedBlockSet()) {
                                            this.actualGridEig[x][y-1+ i].setRedBlock();
                                        }
                                    }
                                    else {
                                        if ( this.actualGridEig[x][y-1+ i].isRedBlockSet()) {
                                            this.actualGridEig[x][y-1+ i].resetRedBlock();
                                        }
                                    }
                                }
                            }
                            else {
                                if ( i == -1  ) {
                                    if ( x-2 >= 0 ) {
                                        if ( red ) {
                                            System.out.println("BLock set: " + this.actualGridEig[x-2][y-1].isRedBlockSet());
                                            if ( !this.actualGridEig[x-2][y-1].isRedBlockSet()) {
                                                this.actualGridEig[x-2][y-1].setRedBlock();
                                            }
                                        }
                                        else {
                                            System.out.println("BLock set false: " + this.actualGridEig[x-2][y-1].isRedBlockSet());
                                            if ( this.actualGridEig[x-2][y-1].isRedBlockSet()) {
                                                this.actualGridEig[x - 2][y - 1].resetRedBlock();
                                            }
                                        }
                                    }

                                }
                                else if ( i == len && x-1+i < 10 && x-1+i >= 0) {
                                    if ( red ) {
                                        if ( !this.actualGridEig[x-1+i][y-1].isRedBlockSet()) {
                                            this.actualGridEig[x-1+i][y - 1].setRedBlock();
                                        }
                                    }
                                    else {
                                        if ( this.actualGridEig[x-1+i][y-1].isRedBlockSet()) {
                                            this.actualGridEig[x-1+i][y - 1].resetRedBlock();
                                        }
                                    }
                                }
                                if ( x-1+i < 10 && x-1+i >= 0){
                                    if ( y - 2 >= 0 ) {
                                        if ( red ) {
                                            System.out.println("Why: " + x + ", " + (x-1+i));
                                            System.out.println("Red block set: " + this.actualGridEig[x - 1 + i][y-2].isRedBlockSet());
                                            if ( !this.actualGridEig[x - 1 + i][y-2].isRedBlockSet()) {
                                                this.actualGridEig[x - 1 + i][y - 2].setRedBlock();
                                            }
                                        }
                                        else {
                                            if ( this.actualGridEig[x - 1 + i][y-2].isRedBlockSet()) {
                                                this.actualGridEig[x - 1 + i][y - 2].resetRedBlock();
                                            }
                                        }
                                    }
                                    if ( y < 10 ) {
                                        if ( red ) {
                                            if ( !this.actualGridEig[x - 1 + i][y].isRedBlockSet()) {
                                                this.actualGridEig[x - 1 + i][y].setRedBlock();
                                            }
                                        }
                                        else {
                                            if ( this.actualGridEig[x - 1 + i][y].isRedBlockSet()) {
                                                this.actualGridEig[x - 1 + i][y].resetRedBlock();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else {
                    }
                }
            }
        }
    }

    /**
     * Schaut ob Frei ist
     * @param leftTile ist das linkeste Tile
     * @param len die länge des Schiffes
     * @param orientation (horizontal = {@link ClientModel#HORIZONTAL} | hertivel = {@link ClientModel#VERTICAL}
     * @return true wenn frei, false wenn nicht
     */
    private boolean checkFree( Tile leftTile, int len, int orientation ) {
        boolean out = true;
        if ( orientation == HORIZONTAL ) {
            // goes from horizontal to vertical
            if ( leftTile.getY()-2+len >= 10 ) {
                return false;
            }
        }
        else {
            // Vertical
            // goes from vertical to horizontal
            if ( leftTile.getX()-2+len >= 10 ) {
                return false;
            }
        }
        boolean rep = true;
        for ( int i = 1; i < len+1 && rep; i++) {
            if ( orientation == HORIZONTAL ) {
                // goes from horizontal to vertical
                if ( leftTile.getY()+i-1 < 10  ) {
                    if ( this.actualGridEig[leftTile.getX()-1][leftTile.getY()+i-1].isHasShip() ) {
                        out = false;

                        rep = false;
                    }
                    if ( leftTile.getX()-2 > 0 && this.actualGridEig[leftTile.getX()-2][leftTile.getY()+i-1].isHasShip()) {
                        out = false;

                        rep = false;
                    }
                    if ( leftTile.getX() < 10 && this.actualGridEig[leftTile.getX()][leftTile.getY()+i-1].isHasShip()) {
                        out = false;

                        rep = false;
                    }
                }

            }
            else {
                // Vertical
                // goes from vertical to horizontal
                if ( leftTile.getX()+i-1 < 10 ) {
                    if ( this.actualGridEig[leftTile.getX()+i-1][leftTile.getY()-1].isHasShip() ) {
                        out = false;
                        break;
                    }
                    if ( leftTile.getY()-2 > 0 && this.actualGridEig[leftTile.getX()+i-1][leftTile.getY()-2].isHasShip()) {
                        out = false;

                        rep = false;
                    }
                    if ( leftTile.getY() < 10 && this.actualGridEig[leftTile.getX()+i-1][leftTile.getY()].isHasShip()) {
                        out = false;

                        rep = false;
                    }
                }
            }
        }

        return out;
    }

    /**
     * Löscht ein Schiff von dem Feld
     * @param currentTile Feld auf dem sich das Schiff befindet
     * @return welches Schiff entfernt wurde {@link ShipEnum}
     */
    public ShipEnum removeShip( Tile currentTile ) {
        Tile leftTile = find(0, currentTile);
        int index = leftTile.getShipArrayIndex();
        int len = 0;
        while ( true ) {
            if ( leftTile.getNextTile() == null ) {
                leftTile.setHasShip(false, null, null, -1);
                len++;
                break;
            }
            Tile temp = leftTile.getNextTile();
            leftTile.setHasShip(false, null, null, -1);
            leftTile = temp;
            len++;
        }
        switch ( len ) {
            case 4:
                this.ships[0][index].setPlaced(false);
                return ShipEnum.Schlachtschiff;
            case 3:
                this.ships[1][index].setPlaced(false);
                return ShipEnum.Kreuzer;
            case 2:
                this.ships[2][index].setPlaced(false);
                return ShipEnum.Fragette;
            case 1:
                this.ships[3][index].setPlaced(false);
                return ShipEnum.Minisuchboot;
        }
        return null;
    }

    /**
     * Fügt ein Schiff hinzuf
     * @param which {@link ShipEnum} welches Schiff
     * @param x x wert
     * @param y y wert
     * @return true wenns funktioniert hat, false wenn nicht
     */
    public boolean addShip( ShipEnum which, int x, int y ) {
        int len = 0;
        int ship = 0;
        switch ( which ) {
            case Schlachtschiff:
                len = 4;
                ship = 0;
                break;
            case Kreuzer:
                len = 3;
                ship = 1;
                break;
            case Fragette:
                len = 2;
                ship = 2;
                break;
            case Minisuchboot:
                len = 1;
                ship = 3;
                break;
        }
        if ( len != 0 ) {
            if ( x-1 + len > 10 ) {
                x = x - ((x-1+len)-10);
            }
            return setShipCord(this.ships[ship], x, y, x+len-1, y, len);
        }
        else {
            return false;
        }
    }

    /**
     * Setzt für ein Schiff die Koordinaten
     * @param arr Array für die Schiffe
     * @param x x wert
     * @param y y wert
     * @param endX xEnd wert
     * @param endY yEnd Wert
     * @param len länge des Schiffes
     * @return true wenn erfolgreich, false wenn nicht
     */
    private boolean setShipCord ( Ship[] arr, int x, int y, int endX, int endY, int len) {
        Ship toEdit = null;
        int ind = 0;
        for (Ship ship : arr) {
            if (!ship.isPlaced()) {
                toEdit = ship;
                break;
            }
            ind++;
        }

        if ( toEdit != null && checkAddFree(x, y, endX, endY, len)) {
            System.out.println("x: " + x);
            System.out.println("y: " + y);
            toEdit.setStartX(x);
            toEdit.setStartY(y);
            toEdit.setEndX(endX);
            toEdit.setEndY(endY);
            toEdit.setPlaced(true);

            for (int i = x-1; i < x + len-1; i++ ) {
                Tile before = null;
                if ( i-1 >= x-1 ) {
                    System.out.println("Before: x: " + i + ", y: " + y);
                    before = this.actualGridEig[i-1][y-1];
                }
                else {
                    System.out.println("Before null");
                }
                Tile after = null;
                if ( i+1 < x + len-1 ) {
                    System.out.println("After: x: " + (i+2) + ", y: " + (y));
                    after = this.actualGridEig[i+1][y-1];
                }
                else {
                    System.out.println("After null");
                }
                this.actualGridEig[i][y-1].setHasShip(true, after, before, ind);
            }
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * CHeckt ob beim hinzufügn genügen freue Felder vorhanden sind
     * @param startX startX
     * @param startY startY
     * @param endX endX
     * @param endY endY
     * @param len Länge des Schiffs
     * @return true wenn ja, false wenn nicht
     */
    private boolean checkAddFree(int startX, int startY, int endX, int endY, int len) {
        int orientation;
        boolean out = true;
        if ( startX == endX ) {
            orientation = VERTICAL;
        }
        else {
            orientation = HORIZONTAL;
        }

        for ( int i = 0; i < len; i++ ) {
            if ( orientation == VERTICAL) {
                if ( actualGridEig[startX-1][startY-1+i].isRedBlockSet() ) {
                    out = false;
                }
            }
            else {
                if ( actualGridEig[startX-1+i][startY-1].isRedBlockSet() ) {
                    out = false;
                }
            }
        }

        return out;
    }

    /**
     * Generiert leere Schiffe, die nicht plaziert sind um das Array zu füllen
     */
    private void createShips() {
        this.ships[0][0] = new Schlachtschiff(0,0,0,0);
        this.enmShips[0][0] = new Schlachtschiff(0,0,0,0);
        for ( int i = 0; i < 2; i++ ) {
            this.ships[1][i] = new Kreuzer(0,0,0,0);
            this.enmShips[1][i] = new Kreuzer(0,0,0,0);
        }
        for ( int i = 0; i < 3; i++ ) {
            this.ships[2][i] = new Fregatte(0,0,0,0);
            this.enmShips[2][i] = new Fregatte(0,0,0,0);
        }
        for ( int i = 0; i < 4; i++ ) {
            this.ships[3][i] = new Minisuchboot(0,0,0,0);
            this.enmShips[3][i] = new Minisuchboot(0,0,0,0);
        }
    }

    /**
     * Generiet die Tiles
     * @param fieldEig eigenes Feld
     * @param fieldEnm Feld des Gegners
     * @return eine Liste mit 2 Arrays, 2 * Tiles[][] (eigenes Feld, gegner Feld)
     */
    public LinkedList<Tile[][]> createContent(GridPane fieldEig, GridPane fieldEnm ) {
        LinkedList<Tile[][]> out = new LinkedList<>();

        Tile[][] outOwn = new Tile[10][10];
        Tile[][] outEnm = new Tile[10][10];

        out.add(0,outOwn);
        out.add(1,outEnm);

        char start = 'A';
        for( int i = 0; i < 11; i ++ ) {

            for ( int j = 0; j < 11; j++) {
                if ( i == 0 ) {
                    if ( j != 0 ) {
                        EdgeTile edgeTile = new EdgeTile(""+(j));
                        fieldEig.add(edgeTile, j,i);

                        EdgeTile edgeTile2 = new EdgeTile(""+(j));
                        fieldEnm.add(edgeTile2, j,i);
                    }
                }
                else if ( j == 0 ) {
                    EdgeTile edgeTile = new EdgeTile("" + start);
                    fieldEig.add(edgeTile, j, i);

                    EdgeTile edgeTile2 = new EdgeTile("" + start);
                    fieldEnm.add(edgeTile2, j, i);

                    start++;
                }
                else {
                    Tile tile = new Tile(j, i, false, false);
                    outOwn[j-1][i-1] = tile;
                    actualGridEig[j-1][i-1] = tile;
                    fieldEig.add(tile, j,i);

                    Tile tile2 = new Tile(j, i, false, true);
                    actualGridEnm[j-1][i-1] = tile2;
                    outEnm[j-1][i-1] = tile2;
                    fieldEnm.add(tile2, j,i);
                }
            }
        }
        return out;
    }


}
