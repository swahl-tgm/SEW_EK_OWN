package client.GUI;

import javafx.scene.layout.GridPane;
import client.GUI.Ships.*;

import javax.imageio.plugins.tiff.TIFFDirectory;
import java.util.LinkedList;

public class ClientModel
{


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

    public void removeShip(int x, int y) {

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
        for (Ship ship : arr) {
            if (!ship.isPlaced()) {
                toEdit = ship;
                break;
            }
        }

        if ( toEdit != null ) {
            System.out.println("x: " + x);
            System.out.println("y: " + y);
            toEdit.setStartX(x);
            toEdit.setStartY(y);
            toEdit.setPlaced(true);

            for (int i = x-1; i < x + len-1; i++ ) {
                System.out.println(i);
                Tile before = null;
                System.out.println("Before: " + (i-2));
                if ( i-2 >= 0 ) {
                    before = this.actualGridEig[i-2][y-1];
                }
                Tile after = null;
                System.out.println("After: " + i);
                if ( i < 10 ) {
                    after = this.actualGridEig[i][y-1];
                }
                this.actualGridEig[i][y-1].setHasShip(true, after, before );
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
