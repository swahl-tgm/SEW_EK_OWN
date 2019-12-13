package client.GUI;

import javafx.scene.layout.GridPane;
import client.GUI.Ships.*;

import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.net.ssl.HostnameVerifier;
import java.util.LinkedList;

public class ClientModel
{

    private static final int VERTICAL = 1;
    private static final int HORIZONTAL = 0;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;



    private Ship[][] ships; // [0]: Schlachtschiff, [1]: Kreuzer, [2]: Fregatten, [3]: Minisuchboot
    private int copy;

    private Tile[][] actualGridEig;
    private Tile[][] actualGridEnm;

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

    private boolean activeLastOne( Ship[] arr ) {
        boolean out = false;
        for (int i = 0; i < arr.length; i++ ) {
            if ( !arr[i].isPlaced()) {
                out = true;
            }
        }

        return out;
    }



    public ClientModel() {
        this.copy = -1;
        ships = new Ship[4][];
        ships[0] = new Schlachtschiff[1];
        ships[1] = new Kreuzer[2];
        ships[2] = new Fregatte[3];
        ships[3] = new Minisuchboot[4];

        createShips();
        actualGridEig = new Tile[10][10];
        actualGridEnm = new Tile[10][10];
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
        System.out.println("Start");
        // find most left tile
        Tile leftTile = find(LEFT, currentTile);
        // find most right tile
        Tile rightTile = find(RIGHT, currentTile);

        // measure distance
        int len;
        int orentation; // 0: horizontal, 1: vertical
        if ( rightTile.getX() == leftTile.getX() ) {
            len = rightTile.getY() - leftTile.getY() +1;
            orentation = VERTICAL;
        }
        else {
            len = rightTile.getX() - leftTile.getX() + 1;
            orentation = HORIZONTAL;
        }
        if ( len > 1 ) {
            // also could be lowerst tile
            Tile workTile = rightTile;
            if ( orentation == VERTICAL ) {
                if ( checkFree( leftTile, len, VERTICAL ) )
                {
                    for ( int j = len-1; j > 0; j-- ) {
                        Tile after = null;
                        Tile before = this.actualGridEig[workTile.getX()+j-2][workTile.getY()-j-1];
                        if ( j != len-1) {
                            after = this.actualGridEig[workTile.getX()+j][workTile.getY()-j-1];
                        }
                        this.actualGridEig[workTile.getX()+j-1][workTile.getY()-j-1].setHasShip(true, after, before, workTile.getShipArrayIndex());

                        Tile temp = workTile.getBeforeTile();
                        workTile.setHasShip(false, null, null, -1);
                        workTile = temp;


                    }
                    // set first elment
                    this.actualGridEig[workTile.getX()-1][workTile.getY()-1].setHasShip(true, this.actualGridEig[workTile.getX()][workTile.getY()-1], null, this.actualGridEig[workTile.getX()][workTile.getY()-1].getShipArrayIndex());
                }
            }
            else {
                if ( checkFree( leftTile, len, HORIZONTAL) ) {
                    for ( int j = len-1; j > 0; j-- ) {
                        Tile after = null;
                        Tile before = this.actualGridEig[workTile.getX()-j-1][workTile.getY()+j-2];
                        if ( j != len-1) {
                            after = this.actualGridEig[workTile.getX()-j-1][workTile.getY()+j];
                        }
                        this.actualGridEig[workTile.getX()-j-1][workTile.getY()+j-1].setHasShip(true, after, before, workTile.getShipArrayIndex());

                        Tile temp = workTile.getBeforeTile();
                        workTile.setHasShip(false, null, null, -1);
                        workTile = temp;

                    }
                    // set first elment
                    this.actualGridEig[workTile.getX()-1][workTile.getY()-1].setHasShip(true, this.actualGridEig[workTile.getX()-1][workTile.getY()], null, this.actualGridEig[workTile.getX()-1][workTile.getY()].getShipArrayIndex());
                }
            }
        }
    }

    private void setShipsBorder() {
        for ( Ship[] shipsArr: this.ships) {
            for ( Ship ship : shipsArr ) {
                if ( ship.isPlaced() ) {
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

                    for ( int i = 0; i < len; i++ ) {

                    }
                }
            }
        }
    }

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

    public void addShip( ShipEnum which, int x, int y ) {
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
            setShipCord(this.ships[ship], x, y, len);
        }
    }

    private void setShipCord ( Ship[] arr, int x, int y, int len) {
        Ship toEdit = null;
        int ind = 0;
        for (Ship ship : arr) {
            if (!ship.isPlaced()) {
                toEdit = ship;
                break;
            }
            ind++;
        }

        if ( toEdit != null ) {
            System.out.println("x: " + x);
            System.out.println("y: " + y);
            toEdit.setStartX(x);
            toEdit.setStartY(y);
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
        }

    }

    private void createShips() {
        this.ships[0][0] = new Schlachtschiff(0,0,0,0);
        for ( int i = 0; i < 2; i++ ) {
            this.ships[1][i] = new Kreuzer(0,0,0,0);
        }
        for ( int i = 0; i < 3; i++ ) {
            this.ships[2][i] = new Fregatte(0,0,0,0);
        }
        for ( int i = 0; i < 4; i++ ) {
            this.ships[3][i] = new Minisuchboot(0,0,0,0);
        }
    }


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
                    Tile tile = new Tile(j, i, false);
                    outOwn[j-1][i-1] = tile;
                    actualGridEig[j-1][i-1] = tile;
                    fieldEig.add(tile, j,i);

                    Tile tile2 = new Tile(j, i, false);
                    actualGridEnm[j-1][i-1] = tile2;
                    outEnm[j-1][i-1] = tile2;
                    fieldEnm.add(tile2, j,i);
                }
            }
        }
        return out;
    }


}
