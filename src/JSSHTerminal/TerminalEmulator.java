package JSSHTerminal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Screen buffer and escape sequence handling
 *
 * @author JL PONS (derived from original Kohsuke Kawaguchi source)
 */

class Screen {

  private static final int EMPTY_CH = 0x070000; // back=0,fore=7,char=0

  // Character considered as a part of a word when double clicking
  private final static char[] WORDC = {
    '-','.','/','?','%','&','#',':','_','=','+','@','~'
  };


  int[] scr;       // Screen buffer
  int[] scrollScr; // Screen buffer (for scrolling)
  int   scrollSize;// Scroll size
  int   scrollFill;// Scroll buffer occupation
  int   width;     // Width
  int   height;    // Height
  int   st;        // Scroll region start
  int   sb;        // Scroll region end
  int   cx;        // Cursor position X
  int   cy;        // Cursor position Y
  int   cx_bak;    // Backup cursor position X
  int   cy_bak;    // Backup cursor position Y
  boolean mouseEna;// Mouse tracking enabled

  int sgr;            // Current SGR (Select Graphics Rendition)
  boolean nextLine;   // Jump to next line flag
  boolean showCursor; // Show cursor flag

  int startSel;       // Text selection start
  int endSel;         // Text selection end

  Screen(int width,int height,int scroll) {

    this.width = width;
    this.height = height;
    this.scrollSize = scroll;
    scrollScr = null;
    scrollFill = 0;
    scr = null;
    scr = new int[width * height];
    if(scrollSize>0) scrollScr = new int[width*scrollSize];
    reset();

  }

  // Reset screen
  void reset() {

    Arrays.fill(scr, EMPTY_CH);
    if(scrollSize!=0) {
      Arrays.fill(scrollScr, EMPTY_CH);
      scrollFill = 0;
    }
    st = 0;
    sb = height - 1;
    cx_bak = cx = 0;
    cy_bak = cy = 0;
    nextLine = false;
    sgr = 0x70000;
    showCursor = true;
    clearSelection();
    mouseEna = false;

  }

  // Set selected text
  public void setSelection(int start,int end) {

    if(end<start) {
      int swp = end;
      end = start;
      start = swp;
    }

    if(start<-(scrollSize*width-1))
      start = -(scrollSize*width-1);
    if(end>scr.length-1)
      end = scr.length-1;

    // Look for start position
    if(getCharAt(start)==0)
      while(start>-(scrollSize*width-1) && getCharAt(start-1)==0) start--;

    // Look for end position
    if(getCharAt(end)==0)
      while(end<scr.length-2 && getCharAt(end+1)==0) end++;

    startSel = start;
    endSel = end;

  }

  // Clear selection
  public void clearSelection() {

    startSel = Integer.MIN_VALUE;
    endSel = Integer.MIN_VALUE;

  }

  private boolean isWordChar(int c) {

    if(c>='A' && c<='Z')
      return true;
    if(c>='a' && c<='z')
      return true;
    if(c>='0' && c<='9')
      return true;

    // Check extra word char
    int i=0;
    boolean found = false;
    while(!found && i<WORDC.length) {
      found = WORDC[i]==c;
      if(!found) i++;
    }
    return found;

  }

  // Auto select word under offset
  public void autoSelect(int offset) {

    if( getCharAt(offset)<=32 )
      clearSelection();
    else {
      startSel = offset;
      endSel = offset;
      while(startSel>-(scrollSize*width-1) && isWordChar(getCharAt(startSel-1))) startSel--;
      while(endSel<scr.length-1 && isWordChar(getCharAt(endSel+1))) endSel++;
    }

  }

  public boolean isValidSelection() {
    return startSel != Integer.MIN_VALUE && endSel != Integer.MIN_VALUE;
  }

  // Return selected text
  public String getSelectedText() {

    if(!isValidSelection())
      return "";

    StringBuffer str = new StringBuffer();
    int sX = startSel % width;
    int sY = startSel / width;

    for(int i=startSel;i<=endSel;i++) {

      int c = getCharAt(i);
      if(c!=0) str.append((char)c);

      sX++;
      if(sX>=width) {
        if(getCharAt(i)==0) str.append("\n");
        sX=0;
        sY++;
      }

    }

    return str.toString();

  }


  // Resize screen
  public void resize(int nWidth, int nHeight, int nScroll) {

    if(nWidth==width && nHeight==height && nScroll==scrollSize)
      return;

    int[] nScr = new int[nWidth * nHeight];
    Arrays.fill(nScr, EMPTY_CH);
    int i=0;
    for(int y=0;y<nHeight;y++) {
      for(int x=0;x<nWidth;x++) {
        nScr[i] = getAt(x,y);
        i++;
      }
    }
    int[] nScrollScr = null;
    if(nScroll>0) {
      nScrollScr = new int[nWidth * nScroll];
      Arrays.fill(nScrollScr, EMPTY_CH);
      i=0;
      for(int y=0;y<nScroll;y++) {
        for(int x=0;x<nWidth;x++) {
          nScrollScr[i] = getAtScroll(x, y);
          i++;
        }
      }
    }

    width = nWidth;
    height = nHeight;
    scr = nScr;
    scrollScr = nScrollScr;

    if(cx>=width) cx = width-1;
    if(cy>=height) cy = height-1;

    st = 0;
    sb = height - 1;
    cx_bak = cx;
    cy_bak = cy;
    nextLine = false;
    sgr = 0x70000;
    showCursor = true;

  }

  // Get index for given coords
  private int $(int y, int x) {
    return y * width + x;
  }

  // Get char at absolute offset in the full buffer including scroll
  // offset can be negative
  public int getCharAt(int offset) {

    if(offset>=0)
      return (scr[offset] & 0xFFFF);
    else {
      offset = scrollSize*width + offset;
      if(offset>=0 && scrollScr!=null)
        return (scrollScr[offset] & 0xFFFF);
    }

    return 0;

  }

  // Get char at x,y
  public int getAt(int x,int y) {
    if(x<width && y<height) {
      return scr[$(y, x)];
    } else {
      return EMPTY_CH;
    }
  }

  // Get char at x,y in scroll buffer
  public int getAtScroll(int x,int y) {

    if(scrollScr!=null && x<width && y<scrollSize) {
      return scrollScr[y*width+x];
    } else {
      return EMPTY_CH;
    }

  }

  // Get screen buffer from (0,y1) to (0,y2)
  public int[] peek(int y1, int y2) {
    return peek(y1, 0, y2, width);
  }

  // Get screen buffer for (x1,y1) to (x2,y2)
  public int[] peek(int y1, int x1, int y2, int x2) {

    int start = $(y1, x1);
    int stop = $(y2, x2);
    int length = stop-start;
    int[] ret = new int[length];
    for(int i=0;i<length;i++) ret[i] = scr[start+i];
    return ret;

  }

  // Write s to buffer screen at (x,y) with specified source offset
  public void poke(int y, int x, int[] s, int off) {
    int destPos = $(y, x);
    System.arraycopy(s, off, scr, destPos, min(s.length, scr.length - destPos) - off);
  }

  // Write s to buffer screen at (x,y)
  public void poke(int y, int x, int[] s) {
    int destPos = $(y, x);
    System.arraycopy(s, 0, scr, destPos, min(s.length, scr.length - destPos));
  }

  // Write s to buffer screen at (0,y)
  public void poke(int y, int[] s) {
    poke(y, 0, s);
  }

  // Clear screen buffer from (x1,y1) to (x2,y2)
  public void zero(int y1, int x1, int y2, int x2) {
    int e = $(y2, x2);
    for (int i = $(y1, x1); i < e; i++)
      scr[i] = sgr;
  }

  // Clear screen buffer from (0,y1) to (0,y2)
  public void zero(int y1, int y2) {
    zero(y1, 0, y2, width);
  }

  // Scroll the (y1+1,y2) region up one line to (y1,y2-1)
  public void scrollUp(int y1, int y2) {

    if(y1==0 && scrollScr!=null) {
      // Go to scroll buffer (if any)
      int[] line = peek(0,0);
      System.arraycopy(scrollScr,width,scrollScr,0,(scrollSize-1)*width);
      System.arraycopy(line,0,scrollScr,(scrollSize-1)*width,line.length);
      startSel -= width;
      endSel -= width;
      if(scrollFill<scrollSize)
        scrollFill++;
    } else {
      clearSelection();
    }


    poke(y1, peek(y1 + 1, y2));
    zero(y2, y2);
  }

  // Scroll the (y1,y2-1) region down one line to (y1+1,y2)
  public void scrollDown(int y1, int y2) {

    poke(y1 + 1, peek(y1, y2 - 1));
    zero(y1, y1);
    clearSelection();

  }

  // Shift Right
  public void scrollRight(int y, int x) {
    poke(y, x + 1, peek(y, x, y, width));
    zero(y, x, y, x);
  }

  // Down the cursor by one line
  public void cursorDown() {

    if (cy >= st && cy <= sb) {
      nextLine = false;
      int q = (cy + 1) / (sb + 1);
      if (q != 0) {
        scrollUp(st, sb);
        cy = sb;
      } else {
        cy = (cy + 1) % (sb + 1);
      }
    }

  }

  // Shift cursor to rigth
  public void cursorRight() {
    if ((cx + 1) >= width)
      nextLine = true;
    else
      cx = (cx + 1) % width;
  }

  // Echo given char
  public void echo(char c) {
    if (nextLine) {
      cursorDown();
      cx = 0;
    }
    scr[$(cy, cx)] = sgr | ((int)c & 0xFFFF);
    cursorRight();
  }

  // Set SGR
  void setSgr(int pos,int bits,int value) {

    int mask = ((1 << bits) - 1) << pos;
    sgr &= ~mask;
    value = value << pos;
    sgr |= value;

  }

  void backspace() {
    cx = max(0, cx - 1);
  }

  void tab() {
    cx = (((cx / 8) + 1) * 8) % width;
  }

  void carriage_return() {
    nextLine = false;
    cx = 0;
  }

  void selectCharSet(int charSet) {
    sgr = 0x070000; // Reset text attributes
    setSgr(24, 2, charSet);
  }

  public void saveCursor() {
    cx_bak = cx;
    cy_bak = cy;
  }

  public void restoreCursor() {
    cx = cx_bak;
    cy = cy_bak;
  }

  private int defaultsTo(int[] args, int defaultValue) {
    return (args.length == 0) ? defaultValue : args[0];
  }

  public void escRi() {
    cy = max(st, cy - 1);
    if (cy == st)
      scrollDown(st, sb);
  }

  public void csi_A(int[] i) {
    cy = max(st, cy - defaultsTo(i, 1));
  }

  public void csi_B(int[] i) {
    cy = min(sb, cy + defaultsTo(i, 1));
  }

  public void csi_C(int[] i) {
    cx = min(width - 1, cx + defaultsTo(i, 1));
    nextLine = false;
  }

  public void csi_D(int[] i) {
    cx = max(0, cx - defaultsTo(i, 1));
    nextLine = false;
  }

  public void csi_E(int[] i) {
    csi_B(i);
    cx = 0;
    nextLine = false;
  }

  public void csi_F(int[] i) {
    csi_A(i);
    cx = 0;
    nextLine = false;
  }

  public void csi_G(int[] i) {
    cx = min(width, i[0]) - 1;
  }

  public void csi_H(int[] i) {
    if (i.length < 2) i = new int[]{1, 1};
    cx = min(width, i[1]) - 1;
    cy = min(height, i[0]) - 1;
    nextLine = false;
  }

  public void csi_J(int[] i) {
    switch (defaultsTo(i, 0)) {
      case 0:
        zero(cy, cx, height, 0);
        return;
      case 1:
        zero(0, 0, cx, cy);
        return;
      case 2:
        zero(0, 0, height, 0);
        return;
    }
  }

  public void csi_K(int... i) {
    switch (defaultsTo(i, 0)) {
      case 0:
        zero(cy, cx, cy, width);
        return;
      case 1:
        zero(cy, 0, cy, cx);
        return;
      case 2:
        zero(cy, 0, cy, width);
        return;
    }
  }

  // Insert lines.
  public void csi_L(int[] args) {
    for (int i = 0; i < defaultsTo(args, 1); i++)
      if (cy < sb)
        scrollDown(cy, sb);
  }

  // Delete lines.
  public void csi_M(int[] args) {
    if (cy >= st && cy <= sb)
      for (int i = 0; i < defaultsTo(args, 1); i++)
        scrollUp(cy, sb);
  }

  // Scroll up scroll region
  public void csi_S(int[] args) {
    for (int i = 0; i < defaultsTo(args, 1); i++)
       scrollUp(st, sb);
  }

  // Scroll down scroll region
  public void csi_T(int[] args) {
    for (int i = 0; i < defaultsTo(args, 1); i++)
      scrollDown(st, sb);
  }

  // Delete n chars
  public void csi_P(int[] args) {
    int _cy = cy, _cx = cx;
    int[] end = peek(cy, cx, cy, width);
    csi_K(0);
    poke(_cy, _cx, end, defaultsTo(args, 1));
  }

  public void csi_X(int[] args) {
    zero(cy, cx, cy, cx + args[0]);
  }

  public void csi_a(int[] args) {
    csi_C(args);
  }

  public void csi_c(int[] args) {
  }

  public void csi_d(int[] args) {
    cy = min(height, args[0]) - 1;
  }

  public void csi_e(int[] args) {
    csi_B(args);
  }

  public void csi_f(int[] args) {
    csi_H(args);
  }

  public void csi_m(int[] args) {

    if (args.length == 0) {
      // Reset text attributes but not the charset (Same as CSI0m)
      sgr = sgr & 0x3000000;
      sgr |= 0x070000;
      return;
    }

    for (int n : args) {

      if (n == 0 ) {
        // Reset text attributes but not the charset
        sgr = sgr & 0x3000000;
        sgr |= 0x070000;
      }

      else if (n == 1)
        setSgr(19,1,1); // bold

      else if (n == 2)
        setSgr(19,1,0); // bold off

      else if (n == 4)
        setSgr(23,1,1); // underline

      else if (n == 24)
        setSgr(23,1,0); // underline off

      else if( n == 27)
        setSgr(26,1,0); // reverse off

      else if (n == 7)
        setSgr(26,1,1); // reverse on

      else if (n>=30 && n<=37)
        setSgr(16,3,n-30); // fg

      else if (n == 39)
        setSgr(16,3,7); // Default fg

      else if (n>=40 && n<=47)
        setSgr(20,3,n-40); // bg

      else if (n == 49)
        setSgr(20,3,0); // Default bg

      else {
        System.out.println("Warning: unhandled escape sequence ESC["+n+"...m");
      }

    }

  }

  public void csi_r(int[] args) {

    if (args.length < 2)
      args = new int[]{0, height};

    st = min(height, args[0]) - 1;
    sb = min(height, args[1]) - 1;
    sb = max(sb, st);

  }

  public void csi_s(int[] args) {
    sb = max(sb, st);
    saveCursor();
  }

  public void csi_u(int[] args) {
    restoreCursor();
  }

  public int getScrollSize() {
    return height+scrollFill;
  }

};


// ------------------------------------------------------------------------------------
// TerminalEmulator emulator class
// ------------------------------------------------------------------------------------

public class TerminalEmulator {

  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  private String buf="";     // Input flow
  private String outBuf="";  // TerminalEmulator output response

  private Screen scr;          // Current screen
  private Screen backScr=null; // Alternate screen
  int[]   retScr;              // Temp buffer
  private String title;        // Window title
  private String titleBackup;  // Window title backup


  public TerminalEmulator(int width, int height, int scroll) {
    scr = new Screen(width,height,scroll);
    retScr = new int[width*height];
    title = "";
  }

  public boolean isCursor(int x,int y) {
    return (scr.cx == x) && (scr.cy == y);
  }

  public String getTitle() {
    return title;
  }

  public void resize(int nWidth, int nHeight) {
    scr.resize(nWidth,nHeight,scr.scrollSize);
    if(backScr!=null) backScr.resize(nWidth,nHeight,scr.scrollSize);
    retScr = new int[nWidth*nHeight];
    buf = "";
  }

  public void setSelection(int start,int end) {
    scr.setSelection(start,end);
  }

  public void clearSelection() {
    scr.clearSelection();
  }

  public int getStartSelection() {
    return scr.startSel;
  }

  public int getEndSelection() {
    return scr.endSel;
  }

  public void autoSelect(int offset) {
    scr.autoSelect(offset);
  }

  public String getSelectedText() {
    return scr.getSelectedText();
  }

  public boolean isMouseEnabled() {
    return scr.mouseEna;
  }

  public int[] getScreen(int scrollPos) {

    if(scrollPos==0 || scr.scrollScr==null) {
      return scr.scr;
    } else {
      int screenSize = scr.width * scr.height;
      int scrollLength = scrollPos*scr.width;
      int scrollIdx = scr.scrollSize*scr.width - scrollLength;
      int leftSize = screenSize - scrollLength;
      if(scrollLength>screenSize) scrollLength=screenSize;
      System.arraycopy(scr.scrollScr,scrollIdx,retScr,0,scrollLength);
      if(leftSize>0) System.arraycopy(scr.scr,0,retScr,scrollLength,leftSize);
      return retScr;
    }

  }

  public int getScrollSize() {
    return scr.getScrollSize();
  }

  public int getCharAt(int offset) {
    return scr.getCharAt(offset);
  }


    @Esc("\u001Bc")
  public void reset() {
    scr.reset();
    buf = "";
  }


  @Esc({"\n", "\u000B", "\u000C"})
  public void cursorDown() {
    scr.cursorDown();
  }

  public void cursorRight() {
    scr.cursorRight();
  }

  public void echo(char c) {
    scr.echo(c);
  }

  // Handle escape sequence
  public void escape() {

    /*
    if (buf.length() > 32) {
      System.out.println("Warning, unexpected escape sequence:");
      printSeq(buf);
      buf = "";
      return;
    }
    */

    EscapeSequence es = ESCAPE_SEQUENCES.get(buf);
    if (es != null) {
      es.handle(this, buf, null);
      buf = "";
      return;
    }

    for (Entry<Pattern, EscapeSequence> ent : REGEXP_ESCAPE_SEQUENCES.entrySet()) {
      Matcher m = ent.getKey().matcher(buf);
      if (m.matches()) {
        ent.getValue().handle(this, buf, m);
        buf = "";
        return;
      }
    }

  }

  /**
   * Return terminal response (if any)
   */
  public String read() {
    //String ret = new String(outBuf);
    //outBuf="";
    //return ret;
    return "";
  }

  private void printSeq(String buf) {

    for(int i=0;i<buf.length();i++) {
      char c = buf.charAt(i);
      if(c=='\u001B')
        System.out.print("<ESC>");
      else if (c<32)
        System.out.print("<"+Integer.toString((int) c)+">");
      else
        System.out.print(buf.charAt(i));
    }
    System.out.println();

  }

  /**
   * Receives the output from the shell into the terminal.
   * @param s
   */
   public void write(char[] s,int length) {

    for (int i = 0; i < length; i++) {
      char ch = s[i];
      if (buf.length() > 0 || ESCAPE_SEQUENCES.containsKey("" + ch)) {
        if(ch=='\u001B') {
          System.out.print("Warning, unexpected escape sequence :");printSeq(buf);
          buf = "";
        }
        buf += ch;
        escape();
      } else if (ch == '\u001B') {
        buf += ch;
      } else {
        echo(ch);
      }
    }

  }

  public String dump() {

    StringBuffer buf = new StringBuffer();
    int i=0;
    for (int y = 0; y < scr.height; y++) {
      for (int x = 0; x < scr.width; x++) {
        buf.append(String.format("%04X ",(int)scr.scr[i])).append(" ").append((char)(scr.scr[i]&0xFFFF)).append(" ");
        i++;
      }
      buf.append("\n");
    }
    return buf.toString();

  }


  // ------------------------------------------------------------------------------
  // Escape SEQUENCE
  // ------------------------------------------------------------------------------

  @Esc({"\u0000", "\u0007", "\u000E", "\u000F", "\u001B#8",
      "\u001B=", "\u001B>",
      "\u001B]R", "\u001BD", "\u001BE", "\u001BH",
      "\u001BN", "\u001BO", "\u001Ba", "\u001Bn", "\u001Bo"})
  public void noOp() {
     // Unhandled
  }

  @Esc("\u0008")
  public void esc_0x08() {
    scr.backspace();
  }

  @Esc("\u0009")
  public void esc_0x09() {
    scr.tab();
  }

  @Esc("\r")
  public void esc_0x0d() {
    scr.carriage_return();
  }

  @Esc("\u001B)A")
  public void selectCharSetPA() {
    //System.out.println("selectCharSetPA");
    scr.selectCharSet(1);
  }

  @Esc("\u001B(A")
  public void selectCharSetAA() {
    //System.out.println("selectCharSetAA");
    scr.selectCharSet(1);
  }

  @Esc("\u001B)B")
  public void selectCharSetPB() {
    //System.out.println("selectCharSetPB");
    scr.selectCharSet(0);
  }

  @Esc("\u001B(B")
  public void selectCharSetAB() {
    //System.out.println("selectCharSetPB");
    scr.selectCharSet(0);
  }

  @Esc("\u001B(0")
  public void selectCharSetP0() {
    //System.out.println("selectCharSetP0");
    scr.selectCharSet(2);
  }

  @Esc("\u001B)0")
  public void selectCharSetA0() {
    //System.out.println("selectCharSetA0");
    scr.selectCharSet(0);
  }

  @Esc("\u001B(1")
  public void selectCharSetP1() {
    //System.out.println("selectCharSetP1");
    scr.selectCharSet(0);
  }

  @Esc("\u001B)1")
  public void selectCharSetA1() {
    //System.out.println("selectCharSetA1");
    scr.selectCharSet(0);
  }

  @Esc("\u001B(2")
  public void selectCharSetP2() {
    //System.out.println("selectCharSetP2");
    scr.selectCharSet(0);
  }

  @Esc("\u001B)2")
  public void selectCharSetA2() {
    //System.out.println("selectCharSetA2");
    scr.selectCharSet(0);
  }

  @Esc("\u001B7")
  public void saveCursor() {
    scr.saveCursor();
  }

  @Esc("\u001B8")
  public void restoreCursor() {
    scr.restoreCursor();
  }

  @Esc("\u001BM")
  public void escRi() {
    scr.escRi();
  }

  public void csi_A(int[] i) {
    scr.csi_A(i);
  }

  public void csi_B(int[] i) {
    scr.csi_B(i);
  }

  public void csi_C(int[] i) {
    scr.csi_C(i);
  }

  public void csi_D(int[] i) {
    scr.csi_D(i);
  }


  public void csi_E(int[] i) {
    scr.csi_E(i);
  }

  public void csi_F(int[] i) {
    scr.csi_F(i);
  }

  public void csi_G(int[] i) {
    scr.csi_G(i);
  }

  public void csi_H(int[] i) {
    scr.csi_H(i);
  }

  public void csi_J(int[] i) {
    scr.csi_J(i);
  }

  public void csi_K(int... i) {
    scr.csi_K(i);
  }

  public void csi_L(int[] args) {
    scr.csi_L(args);
  }

  public void csi_M(int[] args) {
    scr.csi_M(args);
  }

  public void csi_P(int[] args) {
    scr.csi_P(args);
  }

  public void csi_S(int[] args) {
    scr.csi_S(args);
  }

  public void csi_T(int[] args) {
    scr.csi_T(args);
  }

  public void csi_X(int[] args) {
    scr.csi_X(args);
  }

  public void csi_a(int[] args) {
    scr.csi_a(args);
  }

  public void csi_c(int[] args) {
    scr.csi_c(args);
  }

  public void csi_d(int[] args) {
    scr.csi_d(args);
  }

  public void csi_e(int[] args) {
    scr.csi_e(args);
  }

  public void csi_f(int[] args) {
    scr.csi_f(args);
  }

  public void csi_h(int[] args) {
    switch (args[0]) {
      case 25:
        scr.showCursor = true;
        break;
      case 1049:
        // Use alternate screen buffer (no scroll for alternate)
        backScr = scr;
        scr = new Screen(backScr.width,backScr.height,0);
        break;
      case 1000:
        // Enable mouse tracking VT200
        scr.mouseEna = true;
        break;
    }
  }

  public void csi_l(int[] args) {

    switch (args[0]) {
      case 25:
        scr.showCursor = false;
        break;
      case 1000:
        // Disable mouse tracking
        scr.mouseEna = false;
        break;
      case 1049:
        // Restore original screen
        scr = backScr;
        break;
    }

  }


  public void csi_m(int[] args) {
    scr.csi_m(args);
  }

  public void csi_r(int[] args) {
    scr.csi_r(args);
  }

  public void csi_s(int[] args) {
    scr.csi_s(args);
  }

  public void csi_u(int[] args) {
    scr.csi_u(args);
  }

  public void osc(int function,String param) {
    switch (function) {
      case 0:
      case 2:
        // Set terminal title
        title = param;
        break;
      default:
        System.out.println("Warning, unhandled OSC function #"  + function);

    }
  }

  private static interface EscapeSequence {
    void handle(TerminalEmulator t, String s, Matcher m);
  }

  private static final EscapeSequence NONE = new EscapeSequence() {
    public void handle(TerminalEmulator t, String s, Matcher m) {
    }
  };

  private static final Map<String, EscapeSequence> ESCAPE_SEQUENCES = new HashMap<String, EscapeSequence>();

  private static final Map<Pattern, EscapeSequence> REGEXP_ESCAPE_SEQUENCES = new HashMap<Pattern, EscapeSequence>();

  private static abstract class CsiSequence {

    protected CsiSequence() {
    }

    abstract void handle(TerminalEmulator t, int[] args);
  }

  private static abstract class OscSequence {

    protected OscSequence() {
    }

    abstract void handle(TerminalEmulator t, int func, String s);
  }

  private static final Map<Character, CsiSequence> CSI_SEQUENCE = new HashMap<Character, CsiSequence>();

  private static OscSequence oscSequence;

  static {

    for (final Method m : TerminalEmulator.class.getMethods()) {
      Esc esc = m.getAnnotation(Esc.class);
      if (esc != null) {
        for (String s : esc.value()) {
          ESCAPE_SEQUENCES.put(s, new EscapeSequence() {
            public void handle(TerminalEmulator t, String s, Matcher m2) {
              try {
                m.invoke(t);
              } catch (IllegalAccessException e) {
                throw new IllegalAccessError();
              } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
              }
            }
          });
        }
      }
      if (m.getName().startsWith("csi_") && m.getName().length() == 5) {
        CSI_SEQUENCE.put(m.getName().charAt(4), new CsiSequence() {
          void handle(TerminalEmulator t, int[] args) {
            try {
              m.invoke(t, new Object[]{args});
            } catch (IllegalAccessException e) {
              throw new IllegalAccessError();
            } catch (InvocationTargetException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }

      if (m.getName().equals("osc")) {
        oscSequence = new OscSequence() {
          @Override
          void handle(TerminalEmulator t, int func, String s) {
            try {
              m.invoke(t, func, s);
            } catch (IllegalAccessException e) {
              throw new IllegalAccessError();
            } catch (InvocationTargetException e) {
              throw new RuntimeException(e);
            }

          }
        };
      }

    }

    REGEXP_ESCAPE_SEQUENCES.put(
        Pattern.compile("\u001B\\[\\??([0-9;]*)([@ABCDEFGHJKLMPSTXacdefghlmnqrstu`])"),
        new EscapeSequence() {
          public void handle(TerminalEmulator t, String s2, Matcher m) {
            String s = m.group(1);
            CsiSequence seq = CSI_SEQUENCE.get(m.group(2).charAt(0));
            if (seq != null) {
              String[] tokens = s.split(";");
              if (s.length() == 0)
                tokens = EMPTY_STRING_ARRAY;
              int[] n = new int[tokens.length];
              for (int i = 0; i < n.length; i++)
                try {
                  n[i] = Integer.parseInt(tokens[i]);
                } catch (NumberFormatException e) {
                  n[i] = 0;
                }
              seq.handle(t, n);
            } else {
              System.out.println("Warning, unhandled escape sequence " + m.group(2).charAt(0));
            }
          }
        });

    REGEXP_ESCAPE_SEQUENCES.put(
        // OSC
        Pattern.compile("\u001B\\]([0-9];[\\p{Alpha}\\p{Digit}\\p{Punct} ]*)\u0007"),
        new EscapeSequence() {
          public void handle(TerminalEmulator t, String s2, Matcher m) {
            try {
              String s = m.group(1);
              String[] tokens = s.split(";");
              if(tokens.length==2) {
                int func = Integer.parseInt(tokens[0]);
                oscSequence.handle(t,func,tokens[1]);
              }
            } catch (Exception e) {
            }
          }
        });

    REGEXP_ESCAPE_SEQUENCES.put(
        Pattern.compile("\u001C([^\u0007]+)\u0007"),
        NONE);

    CSI_SEQUENCE.put('@', new CsiSequence() {
      @Override
      void handle(TerminalEmulator t, int[] args) {
        for (int i = 0; i < args[0]; i++)
          t.scr.scrollRight(t.scr.cy, t.scr.cx);
      }
    });

  }

  private static byte[] ENTER = {(byte) 0x0d};
  private static byte[] UP = {(byte) 0x1b, (byte) 'O', (byte) 'A'};
  private static byte[] DOWN = {(byte) 0x1b, (byte) 'O', (byte) 'B'};
  private static byte[] RIGHT = {(byte) 0x1b, (byte) 'O', (byte) 'C'};
  private static byte[] LEFT = {(byte) 0x1b, (byte) 'O', (byte) 'D'};
  private static byte[] DELETE = {(byte) 0x1b, (byte) '[', (byte) '3', (byte) '~'};
  private static byte[] SCROLL_UP = {(byte) 0x1b, (byte) '[', (byte) '5', (byte) '~'};
  private static byte[] SCROLL_DOWN = {(byte) 0x1b, (byte) '[', (byte) '6', (byte) '~'};
  private static byte[] HOME = {(byte) 0x1b, (byte) '[', (byte) '1', (byte) '~'};
  private static byte[] END = {(byte) 0x1b, (byte) '[', (byte) '4', (byte) '~'};
  private static byte[] F1 = {(byte) 0x1b, (byte) 'O', (byte) 'P'};
  private static byte[] F2 = {(byte) 0x1b, (byte) 'O', (byte) 'Q'};
  private static byte[] F3 = {(byte) 0x1b, (byte) 'O', (byte) 'R'};
  private static byte[] F4 = {(byte) 0x1b, (byte) 'O', (byte) 'S'};
  private static byte[] F5 = {(byte) 0x1b, (byte) '[', (byte) '1', (byte) '5', (byte) '~'};
  private static byte[] F6 = {(byte) 0x1b, (byte) '[', (byte) '1', (byte) '7', (byte) '~'};
  private static byte[] F7 = {(byte) 0x1b, (byte) '[', (byte) '1', (byte) '8', (byte) '~'};
  private static byte[] F8 = {(byte) 0x1b, (byte) '[', (byte) '1', (byte) '9', (byte) '~'};
  private static byte[] F9 = {(byte) 0x1b, (byte) '[', (byte) '2', (byte) '0', (byte) '~'};
  private static byte[] F10 = {(byte) 0x1b, (byte) '[', (byte) '2', (byte) '1', (byte) '~'};
  private static byte[] F11 = {(byte) 0x1b, (byte) '[', (byte) '2', (byte) '3', (byte) '~'};
  private static byte[] F12 = {(byte) 0x1b, (byte) '[', (byte) '2', (byte) '4', (byte) '~'};

  static public byte[] getCodeENTER() {
    return ENTER;
  }

  static public byte[] getCodeUP() {
    return UP;
  }

  static public byte[] getCodeDOWN() {
    return DOWN;
  }

  static public byte[] getCodeRIGHT() {
    return RIGHT;
  }

  static public byte[] getCodeDELETE() {
    return DELETE;
  }

  static public byte[] getCodeScrollUp() {
    return SCROLL_UP;
  }

  static public byte[] getCodeScrollDown() {
    return SCROLL_DOWN;
  }

  static public byte[] getCodeHome() {
    return HOME;
  }

  static public byte[] getCodeEnd() {
    return END;
  }

  static public byte[] getCodeLEFT() {
    return LEFT;
  }

  static public byte[] getCodeF1() {
    return F1;
  }

  static public byte[] getCodeF2() {
    return F2;
  }

  static public byte[] getCodeF3() {
    return F3;
  }

  static public byte[] getCodeF4() {
    return F4;
  }

  static public byte[] getCodeF5() {
    return F5;
  }

  static public byte[] getCodeF6() {
    return F6;
  }

  static public byte[] getCodeF7() {
    return F7;
  }

  static public byte[] getCodeF8() {
    return F8;
  }

  static public byte[] getCodeF9() {
    return F9;
  }

  static public byte[] getCodeF10() {
    return F10;
  }

  static public byte[] getCodeF11() {
    return F11;
  }

  static public byte[] getCodeF12() {
    return F12;
  }

}
