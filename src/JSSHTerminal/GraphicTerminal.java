package JSSHTerminal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Terminal component: Use bitmap font to render
 *
 * @author JL PONS
 */

public class GraphicTerminal extends TerminalEvent {

  private BufferedImage charSetA;
  private BufferedImage charSetB;
  private BufferedImage charSet0;
  private BufferedImage screen;
  private BufferedImage tmpImg;
  private int[] tmpBuffer;
  private int lastChx=-1;
  private int lastChy=-1;
  private int lastBg=-1;
  private int lastFg=-1;
  private int lastCharSet=-1;
  private Graphics tmpImgG;
  private AffineTransform identityTr;

  public GraphicTerminal(MainPanel parent,int width,int height) {

    setBorder(null);
    setOpaque(false);
    setDoubleBuffered(false);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);

    init(parent,width,height,8,16);

    tmpImg = new BufferedImage(charWidth,charHeight,BufferedImage.TYPE_INT_RGB);
    tmpImgG = tmpImg.getGraphics();
    tmpBuffer = new int[charWidth*charHeight];
    identityTr = new AffineTransform();
    loadFont();

  }

  private void loadFont() {

    try {
      charSetA = ImageIO.read(getClass().getClassLoader().getResource("JSSHTerminal/fontPA.png"));
      charSetB = ImageIO.read(getClass().getClassLoader().getResource("JSSHTerminal/fontPB.png"));
      charSet0 = ImageIO.read(getClass().getClassLoader().getResource("JSSHTerminal/fontP0.png"));
    } catch (IOException e) {
      System.out.println("Cannot load font " + e.getMessage());
    }

  }

  @Override
public void dispose() {

    tmpImgG.dispose();
    tmpImg = null;
    screen = null;
    terminal = null;

  }


  @Override
public void sizeComponent(int width,int height) {

    //GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    //GraphicsDevice device = env.getDefaultScreenDevice();
    //GraphicsConfiguration config = device.getDefaultConfiguration();
    //screen = config.createCompatibleImage(termWidth*charWidth,termHeight*charHeight,Transparency.OPAQUE);
    screen = new BufferedImage(termWidth*charWidth,termHeight*charHeight, BufferedImage.TYPE_INT_RGB);

  }


  @Override
public synchronized void drawComponent(Graphics g) {

    if (terminal == null) {
      g.setColor(Color.BLACK);
      g.drawRect(0, 0, termWidth * charWidth, termHeight * charHeight);
      return;
    }

    Graphics scrG = screen.getGraphics();
    // Reset transformation to identity
    ((Graphics2D)g).setTransform(identityTr);
    paintTo(scrG);
    scrG.dispose();
    g.drawImage(screen,0,0,null);

  }

  public void  paintTo(Graphics g) {

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
        boolean isDefault = (fg == 7 && bg == 0) && !reverse;

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
          isDefault = false;
        }

        if (terminal.isCursor(x, y - scrollPos)) {
          // Cursor
          bgColor = cursorBackground;
          fgColor = Color.BLACK;
          isDefault = false;
        }

        if (c < 256) {

          getChar(c, fgColor, bgColor, charSet, bold, isDefault);

        } else {


          switch (c) {

            case 0x2500: // hline
              c = 'q';
              charSet = 2;
              break;

            case 0x2502: // vline
              c = 'x';
              charSet = 2;
              break;

            case 0x250C: // LU corner
              c = 'l';
              charSet = 2;
              break;

            case 0x2510: // RU corner
              c = 'k';
              charSet = 2;
              break;

            case 0x2514: // LD corner
              c = 'm';
              charSet = 2;
              break;

            case 0x2518: // LD corner
              c = 'j';
              charSet = 2;
              break;

            case 0x2524: // T left
              c = 'u';
              charSet = 2;
              break;

            case 0x251C: // T right
              c = 't';
              charSet = 2;
              break;

            case 0x2592: // Dotted fill
              c = 'a';
              charSet = 2;
              break;
            case 0x2010: // Dash
              c = '-';
              break;

            default:
              // Not handled unicode
              System.out.println("Warning, unhandled unicode " + String.format("%04X", c));
              c = 0;
          }

        }

        getChar(c, fgColor, bgColor, charSet, bold, isDefault);
        g.drawImage(tmpImg, x * charWidth, y * charHeight, charWidth * (x + 1), charHeight * (y + 1),
            0, 0, charWidth, charHeight, null);

        if (underline) {
          g.setColor(Color.WHITE);
          g.drawLine(x * charWidth, (y + 1) * charHeight - 1, (x + 1) * charWidth, (y + 1) * charHeight - 1);
        }

        i++;
      }
    }

  }

  BufferedImage getChar(int c,Color fgColor,Color bgColor,int charSet,boolean isBold,boolean isDefault) {

    // Character coordinates
    int chx = 0;
    int chy = 0;
    if(c>=32 && c<127) {
      chx = c % 16;
      chy = c / 16 - 2;
      if( isBold ) chy += 6;
    }

    // Detect context change
    int bg = bgColor.getRGB();
    int fg = fgColor.getRGB();

    if(lastBg==bg && lastFg==fg && lastChx==chx && lastChy==chy && lastCharSet==charSet) {
      // Last image is the same
      return tmpImg;
    }

    lastBg = bg;
    lastFg = fg;
    lastChx = chx;
    lastChy = chy;
    lastCharSet = charSet;

    BufferedImage src;

    switch (charSet) {
      case 1:
        src = charSetA;
        break;
      case 2:
        src = charSet0;
        break;
      default:
        src = charSetB;
    }

    if( isDefault ) {

      // Default white on black
      tmpImgG.drawImage(src, 0, 0, charWidth, charHeight,
          chx * charWidth, chy * charHeight, charWidth * (chx + 1), charHeight * (chy + 1), null);

    } else {

      if (c <= 32) {

        // Space (only paint background)
        tmpImgG.setColor(bgColor);
        tmpImgG.fillRect(0, 0, charWidth, charHeight);

      } else {

        // Colorize character
        double bgR = (double) bgColor.getRed();
        double bgG = (double) bgColor.getGreen();
        double bgB = (double) bgColor.getBlue();
        double fgR = (double) fgColor.getRed();
        double fgG = (double) fgColor.getGreen();
        double fgB = (double) fgColor.getBlue();
        double i255 = 1/255.0;

        src.getRGB(chx * charWidth, chy * charHeight, charWidth, charHeight, tmpBuffer, 0, charWidth);
        for (int i = 0; i < tmpBuffer.length; i++) {
          if ((tmpBuffer[i] & 0xFFFFFF) == 0) {
            tmpBuffer[i] = bg;
          } else if (tmpBuffer[i] == 0xFFFFFF) {
            tmpBuffer[i] = fg;
          } else {
            double factor = (double) (tmpBuffer[i] & 0xFF) * i255;
            double ofactor = 1.0 - factor;
            int nRed =   (int) (factor * fgR + ofactor * bgR) << 16;
            int nGreen = (int) (factor * fgG + ofactor * bgG) << 8;
            int nBlue =  (int) (factor * fgB + ofactor * bgB);
            tmpBuffer[i] = nRed | nGreen | nBlue;
          }
        }
        tmpImg.setRGB(0, 0, charWidth, charHeight, tmpBuffer, 0, charWidth);

      }

    }


    return tmpImg;

  }


}
