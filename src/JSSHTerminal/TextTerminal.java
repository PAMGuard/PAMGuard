package JSSHTerminal;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Text terminal: Use Java font to render
 *
 * @author JL PONS
 */

public class TextTerminal extends TerminalEvent {

  private static BufferedImage dummyImg = null;
  private static FontRenderContext frc = null;

  static private Font plainFont;
  static private Font boldFont;

  private int fontAscent;
  private char[] charBuffer;

  public static final char[] C0 = {

      '\u25c6', // black_diamond
      '\u2592', // Medium Shade
      '\u2409', // Horizontal tab (HT)
      '\u240c', // Form Feed (FF)
      '\u240d', // Carriage Return (CR)
      '\u240a', // Line Feed (LF)
      '\u00b0', // Degree sign
      '\u00b1', // Plus/minus sign
      '\u2424', // New Line (NL)
      '\u240b', // Vertical Tab (VT)
      '\u2518', /*'\u251b'*/ // Forms up and left
      '\u2510', /*'\u2513'*/ // Forms down and left
      '\u250c', /*'\u250f'*/ // Forms down and right
      '\u2514', /*'\u2517'*/ // Forms up and right
      '\u253c', /* '\u254b'*/ // Forms vertical and horizontal
      '\u23ba', // Scan 1
      '\u23bb', // Scan 3
      '\u2500', /*'\u2501'*/ // Scan 5 / Horizontal bar
      '\u23bc', // Scan 7
      '\u23bd', // Scan 9
      '\u251c', /*'\u2523'*/ // Forms vertical and right
      '\u2524', /*'\u252b'*/ // Forms vertical and left
      '\u2534', /*'\u253b'*/ // Forms up and horizontal
      '\u252c', /*'\u2533'*/ // Forms down and horizontal
      '\u2502', /*'\u2503'*/ // vertical bar
      '\u2264', // less than or equal sign
      '\u2265', // greater than or equal sign
      '\u03c0', // pi
      '\u2260', // not equal sign
      '\u00a3', // pound sign
      '\u00b7'  // middle dot

  };

  public TextTerminal(MainPanel parent,int width,int height) {

    setBorder(null);
    setOpaque(false);
    setDoubleBuffered(false);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);

    plainFont = new Font("Monospaced",Font.PLAIN,14);
    boldFont = plainFont.deriveFont(Font.BOLD);

    int[] dim = measureString(plainFont,"W");
    init(parent, width, height, dim[0], dim[1]);
    fontAscent = dim[2];

  }

  public void dispose() {
    dummyImg = null;
  }

  public void sizeComponent(int width,int height) {
    charBuffer = new char[width];
  }

  public synchronized void drawComponent(Graphics g) {

    Graphics2D g2 = (Graphics2D)g;

    g.setColor(Color.BLACK);
    g.drawRect(0, 0, termWidth * charWidth, termHeight * charHeight);
    g.setFont(plainFont);
    //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    //g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    int[] scr = terminal.getScreen(scrollPos);

    int i = 0;
    for (int y = 0; y < termHeight; y++) {
      for (int x = 0; x < termWidth; x++) {

        int c = scr[i] & 0xFFFF;
        int sgr = scr[i] >> 16;

        int bg = (sgr & 0x70) >> 4;
        int fg = sgr & 0x7;
        boolean bold = (sgr & 0x8) != 0;
        boolean underline = (sgr & 0x80) != 0;
        boolean reverse = (sgr & 0x400) != 0;
        int charSet = (sgr & 0x300) >> 8;

        Color fgColor;
        Color bgColor;

        if (reverse) {
          bgColor = defaultColors[fg];
          if(bold)
            fgColor = defaultBrightColors[bg];
          else
            fgColor = defaultColors[bg];
        } else {
          bgColor = defaultColors[bg];
          if(bold)
            fgColor = defaultBrightColors[fg];
          else
            fgColor = defaultColors[fg];
        }

        if (i - scrollPos * termWidth >= terminal.getStartSelection() &&
            i - scrollPos * termWidth <= terminal.getEndSelection()) {
          // Selected text
          bgColor = Color.WHITE;
          fgColor = Color.BLACK;
        }

        if (terminal.isCursor(x, y - scrollPos)) {
          // Cursor
          bgColor = cursorBackground;
          fgColor = Color.BLACK;
        }

        if(charSet==2 && (c>=96 && c<128))
          charBuffer[0] = C0[c-96];
        else
          charBuffer[0] = (char)c;

        if(!bgColor.equals(Color.BLACK)) {
          g.setColor(bgColor);
          g.fillRect(x * charWidth, y * charHeight, charWidth, charHeight);
        }
          
        if(c>32) {
          g.setColor(fgColor);
          if(bold)
            g.setFont(boldFont);
          else
            g.setFont(plainFont);
          g.drawChars(charBuffer,0,1,x*charWidth,y*charHeight+fontAscent);
        }

        if (underline) {
          g.setColor(Color.WHITE);
          g.drawLine(x * charWidth, (y + 1) * charHeight - 1, (x + 1) * charWidth, (y + 1) * charHeight - 1);
        }

        i++;
      }
    }


  }

  private static int[] measureString(Font f,String s) {

    int[] ret = new int[3];
    if(dummyImg==null) {
      dummyImg = new BufferedImage(8,8,BufferedImage.TYPE_INT_RGB);
      Graphics2D g = (Graphics2D)dummyImg.getGraphics();
      //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      //g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      frc = g.getFontRenderContext();
      g.dispose();
    }
    LineMetrics lm = f.getLineMetrics(s,frc);
    Rectangle2D bounds = f.getStringBounds(s, frc);
    ret[0] = (int)(bounds.getWidth()+0.5);
    ret[1] = (int)(bounds.getHeight()+0.5);
    ret[2] = (int)(lm.getAscent()+0.5);
    return ret;

  }

}
