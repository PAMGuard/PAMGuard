package PamView;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;


/**
 * A JPanel that accepts file and folder drag-and-drop. Note: Override the processFile method to handle dropped files.
 */
public class PamFileDropPanel extends JPanel {

    private static final long serialVersionUID = 1L;
	private final Color DEFAULT_COLOR = new Color(240, 240, 240);
	private final Color HOVER_COLOR = new Color(200, 230, 255);

    public PamFileDropPanel() {
        setBackground(DEFAULT_COLOR);
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        setLayout(new GridBagLayout());
        
        add(getDefaultCenterComponent());

        // We use DropTarget for EVERYTHING to avoid event conflicts
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    setBackground(HOVER_COLOR);
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                setBackground(DEFAULT_COLOR);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                setBackground(DEFAULT_COLOR);
                try {
                    // 1. Check if we can accept this drop
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        
                        // 2. Extract the files
                        Transferable t = dtde.getTransferable();
                        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        
                        // 3. Trigger your function
                        processFileList(files);
                        
                        // 4. Tell the OS the drop was successful
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.dropComplete(false);
                }
            }
        });
    }
    
    
    public Component getDefaultCenterComponent() {
    	return new JLabel("Drag files or folders here");
	}
    

    public void processFileList(List<File> files) {
        System.out.println("Successful Drop: " + files);
    }
}