package JSSHTerminal;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;

/**
 * Terminal component super class
 * Handle events
 *
 * @author: JL PONS
 */

public abstract class TerminalEvent extends JComponent implements MouseListener, MouseMotionListener {

  //final Color[] defaultColors = {
  //    Color.BLACK,
  //    Color.RED.darker(),
  //    Color.GREEN.darker(),
  //    Color.ORANGE.darker(),
  //    Color.BLUE,
  //    Color.MAGENTA,
  //    Color.CYAN.darker(),
  //    Color.WHITE
  //};

  final static Color[] defaultColors = {
      Color.BLACK,                // Black
      new Color(170,0,0),         // Red
      new Color(0,170,0),         // Green
      new Color(197,94,0),        // Orange
      new Color(0,0,170),         // Blue
      new Color(170,0,170),       // Magenta
      new Color(0,170,170),       // Cyan
      new Color(220,220,220),     // Light grey
  };

  final static Color[] defaultBrightColors = {
      new Color(90,90,90),        // GRey
      new Color(255,90,90),       // Light Red
      new Color(90,255,90),       // Light Green
      new Color(255,128,0),       // Light Orange
      new Color(90,90,255),       // Light Blue
      new Color(255,0,255),       // Light Magenta
      new Color(0,255,255),       // Light Cyan
      Color.WHITE
  };

  final Color cursorBackground = new Color(0,170,0);

  SSHSession session = null;
  TerminalEmulator terminal = null;

  int charWidth;
  int charHeight;
  int termWidth=0;
  int termHeight=0;
  int scrollPos=0;
  Dimension size;
  MainPanel _parent;
  boolean isDragging;
  MouseEvent startSel=null;
  MouseEvent endSel=null;

  public void init(MainPanel parent,int width,int height,int charWidth,int charHeight) {

    _parent = parent;
    setBorder(null);
    setOpaque(false);
    setDoubleBuffered(false);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    this.charWidth = charWidth;
    this.charHeight = charHeight;

    resizeComponent(width, height);

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        resize();
      }
    });

    addMouseListener(this);
    addMouseMotionListener(this);

  }

  private void resize() {

    Dimension d = getSize();
    if((d.width%charWidth==0) && (d.height%charHeight==0)) {
      resizeComponent(d.width/charWidth,d.height/charHeight);
    } else {
      setSize(charWidth*(d.width/charWidth),charHeight*(d.height/charHeight));
    }

  }


  abstract public void dispose();

  abstract public void sizeComponent(int width,int height);

  synchronized void resizeComponent(int width,int height) {

    if(termHeight!=height || termWidth!=width ) {
      // Resize
      termWidth = width;
      termHeight = height;
      sizeComponent(width,height);
      size = new Dimension(termWidth*charWidth,termHeight*charHeight);
      notifySizeChange();
    }

  }

  public void notifySizeChange() {
    if(terminal!=null) terminal.resize(termWidth,termHeight);
    if(session!=null) session.resize(termWidth,termHeight);
  }

  public Dimension getPreferredSize() {
    return size;
  }


  public void setSession(SSHSession session) {

    this.session = session;
    terminal = session.getTerminal();

  }

  public void setScrollPos(int pos) {

    scrollPos = terminal.getScrollSize()-pos-termHeight;
    repaint();

  }

  public void moveScroll(int move) {

    scrollPos+=move;
    if(scrollPos<0) scrollPos=0;
    if(scrollPos>terminal.getScrollSize()-termHeight) scrollPos=terminal.getScrollSize()-termHeight;
    repaint();

  } 
  public abstract void drawComponent(Graphics g);

  public void paint(Graphics g) {

    //long t0 = System.currentTimeMillis();
    drawComponent(g);
    //long t1 = System.currentTimeMillis();
    //System.out.println("Rendering time= " + (t1-t0) + "ms");

    if(terminal.getTitle().length()>0)
      _parent.setExtraTilte(terminal.getTitle());

  }

  private boolean isCtrlKey(int keyCode) {
    return (keyCode==KeyEvent.VK_CONTROL) ||
           (keyCode==KeyEvent.VK_SHIFT) ||
           (keyCode==KeyEvent.VK_ALT) ||
           (keyCode==KeyEvent.VK_CAPS_LOCK);
  }


  public void processKeyEvent(KeyEvent e) {

    int id=e.getID();

    if(e.isConsumed())
      return;

    int keyCode = e.getKeyCode();
    if(isCtrlKey(keyCode)) {
      e.consume();
      return;
    }

    // Handle function key
    if (id == KeyEvent.KEY_PRESSED) {

      byte[] fCode = functionKey(keyCode);
      if (fCode != null) {
        try {
          session.write(fCode);
        } catch (Exception ee) {
          ee.printStackTrace();
        }
        scrollPos = 0;
        e.consume();
        return;
      }

    }

    if (id == KeyEvent.KEY_TYPED) {

      int keyChar = e.getKeyChar();
      // keyChar 127 = DEL (escape sequence)
      if (keyChar<127) {
        try {
          session.write(new byte[]{(byte) e.getKeyChar()});
          scrollPos = 0;
        } catch (Exception ee) {
          ee.printStackTrace();
        }
      }

    }

    e.consume();

  }

  public byte[] functionKey(int keycode) {

    if(session==null)
      return null;

    // Function keys
    switch(keycode){
      case KeyEvent.VK_UP:
        return TerminalEmulator.getCodeUP();
      case KeyEvent.VK_DOWN:
        return TerminalEmulator.getCodeDOWN();
      case KeyEvent.VK_RIGHT:
        return TerminalEmulator.getCodeRIGHT();
      case KeyEvent.VK_LEFT:
        return TerminalEmulator.getCodeLEFT();
      case KeyEvent.VK_F1:
        return TerminalEmulator.getCodeF1();
      case KeyEvent.VK_F2:
        return TerminalEmulator.getCodeF2();
      case KeyEvent.VK_F3:
        return TerminalEmulator.getCodeF3();
      case KeyEvent.VK_F4:
        return TerminalEmulator.getCodeF4();
      case KeyEvent.VK_F5:
        return TerminalEmulator.getCodeF5();
      case KeyEvent.VK_F6:
        return TerminalEmulator.getCodeF6();
      case KeyEvent.VK_F7:
        return TerminalEmulator.getCodeF7();
      case KeyEvent.VK_F8:
        return TerminalEmulator.getCodeF8();
      case KeyEvent.VK_F9:
        return TerminalEmulator.getCodeF9();
      case KeyEvent.VK_F10:
        return TerminalEmulator.getCodeF10();
      case KeyEvent.VK_F11:
        return TerminalEmulator.getCodeF11();
      case KeyEvent.VK_F12:
        return TerminalEmulator.getCodeF12();
      case KeyEvent.VK_DELETE:
        return TerminalEmulator.getCodeDELETE();
      case KeyEvent.VK_PAGE_UP:
        return TerminalEmulator.getCodeScrollUp();
      case KeyEvent.VK_PAGE_DOWN:
        return TerminalEmulator.getCodeScrollDown();
      case KeyEvent.VK_HOME:
        return TerminalEmulator.getCodeHome();
      case KeyEvent.VK_END:
        return TerminalEmulator.getCodeEnd();
    }

    return null;

  }

  private void sendMouseEvent(int x,int y,int b,boolean press) {

    if(b<1)
      return;

    char Cx = (char)(x+33);
    char Cy = (char)(y+33);
    char Cb;

    if(press)
      Cb = (char)(b+31);
    else
      Cb = (char)(35);

    String seq = "\u001B[M" + Cb + Cx + Cy;
    session.write(seq);

  }

  private void copyToClipboard(String s) {

    if(s.length()>0) {
      StringSelection stringSelection = new StringSelection( s );
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents( stringSelection, null );
    }

  }

  private String getFromClipboard() {

    String ret = "";
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    try {
      ret = (String)(clipboard.getData(DataFlavor.stringFlavor));
    }
    catch (UnsupportedFlavorException e1) {}
    catch (IOException e2) {}
    return ret;

  }

  @Override
  public void mouseClicked(MouseEvent e) {

    if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
      int sX = e.getX() / charWidth;
      int sY = e.getY() / charHeight - scrollPos;
      terminal.autoSelect(sX + sY*termWidth);
      copyToClipboard(terminal.getSelectedText());
      repaint();
    }

    if(SwingUtilities.isMiddleMouseButton(e) && e.getClickCount()==1) {
      String s = getFromClipboard();
      session.write(s);
      scrollPos=0;
    }

  }

  @Override
  public void mousePressed(MouseEvent e) {


    if(SwingUtilities.isLeftMouseButton(e)) {
      startSel = e;
      endSel = null;
      setSelection(startSel,endSel);
      isDragging = false;
      repaint();
    }

    if(terminal.isMouseEnabled()) {
      int sX = e.getX() / charWidth;
      int sY = e.getY() / charHeight - scrollPos;
      sendMouseEvent(sX,sY,e.getButton(),true);
    }

  }

  @Override
  public void mouseReleased(MouseEvent e) {


    if(SwingUtilities.isLeftMouseButton(e) && isDragging) {
      endSel = e;
      setSelection(startSel, endSel);
      copyToClipboard(terminal.getSelectedText());
      isDragging = false;
      repaint();
    }

    if(terminal.isMouseEnabled()) {
      int sX = e.getX() / charWidth;
      int sY = e.getY() / charHeight - scrollPos;
      sendMouseEvent(sX,sY,e.getButton(),false);
    }

  }

  @Override
  public void mouseEntered(MouseEvent e) {

  }

  @Override
  public void mouseExited(MouseEvent e) {

  }

  @Override
  public void mouseDragged(MouseEvent e) {

    if(SwingUtilities.isLeftMouseButton(e)) {
      isDragging = true;
      endSel = e;
      setSelection(startSel, endSel);
      repaint();
    }

  }

  private void setSelection(MouseEvent s,MouseEvent e) {

    if(s==null || e==null) {
      terminal.clearSelection();
      return;
    }

    int sOff = getCursorCoordinates(s);
    int eOff = getCursorCoordinates(e);
    if(sOff==eOff) {
      terminal.clearSelection();
      return;
    }

    if(sOff>eOff) {
      int swb = sOff;
      sOff = eOff;
      eOff = swb;
    }

    eOff -= 1;
    terminal.setSelection(sOff,eOff);

  }

  private int getCursorCoordinates(MouseEvent e) {

    // On Windows the anchor of the TEXT cursor is not located at the center of the cursor !!!
    // 2 is ~ the half width of the text cursor
    int drift = MainPanel.isWindows?2:0;
    int sX = (e.getX() + drift + charWidth/2 ) / charWidth;
    int sY = e.getY() / charHeight - scrollPos;
    int offset = sX + sY * termWidth;
    return offset;

  }

  @Override
  public void mouseMoved(MouseEvent e) {

  }



}
