package client.GUI;


import javafx.scene.control.Label;

public class CommandLineCapsule {
    private Label commandLine;
    private boolean editable;

    public CommandLineCapsule( Label c) {
        this.commandLine = c;
        this.editable = true;
    }

    public void stop() {
        this.editable = false;
    }

    public void start() {
        this.editable = true;
    }

    public void setText( String text, boolean force ) {
        if ( editable || force ) {
            this.commandLine.setText(text);
        }
    }

}
