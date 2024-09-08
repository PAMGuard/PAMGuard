package JSSHTerminal;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Launch several terminal a place them on the screen
 */
public class MultiTerminal extends JFrame implements ActionListener {

  private Preferences prefs;
  private String prefHostList;
  private String prefUser;
  private String prefPassword;
  private String prefTermDim;
  private String prefLastDir;
  private String prefCommand;

  private JPanel innerPanel;
  private JTextArea hostList;
  private JScrollPane hostListScroll;
  private JLabel userLabel;
  private JTextField userText;
  private JLabel passwordLabel;
  private JTextField passwordText;
  private JLabel termDimLabel;
  private JTextField termDimText;
  private JLabel commandLabel;
  private JTextField commandText;

  private JButton connectBtn;
  private JButton loadButton;

  public MultiTerminal() {

    // Get user settings
    prefs = Preferences.userRoot().node(this.getClass().getName());
    prefHostList = prefs.get("HostList", "");
    prefUser = prefs.get("User","");
    prefPassword = prefs.get("Password","");
    prefTermDim = prefs.get("TermDim","80x40");
    prefLastDir = prefs.get("LastDir",".");
    prefCommand = prefs.get("Command","");

    innerPanel = new JPanel();
    innerPanel.setLayout(null);
    innerPanel.setPreferredSize(new Dimension(350,175));
    setContentPane(innerPanel);

    hostListScroll = new JScrollPane();
    hostList = new JTextArea();
    userText = new JTextField();
    userLabel = new JLabel();
    passwordLabel = new JLabel();
    passwordText = new JTextField();
    connectBtn = new JButton();
    loadButton = new JButton();
    termDimLabel = new JLabel();
    termDimText = new JTextField();
    commandLabel = new JLabel();
    commandText = new JTextField();

    setLayout(null);

    hostList.setEditable(true);
    hostList.setText(prefHostList);
    hostListScroll.setViewportView(hostList);
    innerPanel.add(hostListScroll);
    hostListScroll.setBounds(10, 10, 130, 160);

    userLabel.setText("User");
    innerPanel.add(userLabel);
    userText.setText(prefUser);
    innerPanel.add(userText);
    userLabel.setBounds(150, 18, 100, 20);
    userText.setBounds(250, 15, 95, 25);

    passwordLabel.setText("Password");
    innerPanel.add(passwordLabel);
    passwordText.setText(prefPassword);
    innerPanel.add(passwordText);
    passwordLabel.setBounds(150, 48, 100, 20);
    passwordText.setBounds(250, 45, 95, 25);

    termDimLabel.setText("Terminal (WxH)");
    innerPanel.add(termDimLabel);
    termDimText.setText(prefTermDim);
    innerPanel.add(termDimText);
    termDimLabel.setBounds(150, 78, 100, 20);
    termDimText.setBounds(250, 75, 95, 25);

    commandLabel.setText("Command");
    innerPanel.add(commandLabel);
    commandText.setText(prefCommand);
    innerPanel.add(commandText);
    commandLabel.setBounds(150, 108, 100, 20);
    commandText.setBounds(250, 105, 95, 25);

    connectBtn.setText("Connect");
    connectBtn.addActionListener(this);
    innerPanel.add(connectBtn);
    connectBtn.setBounds(255, 145, 88, 25);

    loadButton.setText("Load list...");
    loadButton.addActionListener(this);
    innerPanel.add(loadButton);
    loadButton.setBounds(145, 145, 105, 25);

    addWindowListener(new WindowAdapter() {
      @Override
	public void windowClosing(WindowEvent e) {
        // Save pref
        prefs.put("HostList",hostList.getText());
        prefs.put("User",userText.getText());
        prefs.put("Password",passwordText.getText());
        prefs.put("TermDim",termDimText.getText());
        prefs.put("LastDir",prefLastDir);
        prefs.put("Command", commandText.getText());

      }
    });

    setTitle("MultiTerminal");
    pack();
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setVisible(true);

  }

  private String[] makeStringArray(String value) {
    // Remove extra \n at the end of the string (not handled by split)
    while (value.endsWith("\n")) value = value.substring(0, value.length() - 1);
    return value.split("\n");
  }

  private Dimension getTermDim() {

    String[] d = termDimText.getText().split("x");

    if(d.length!=2) {
      JOptionPane.showMessageDialog(this,"Error","Invalid terminal dimension (WxH)",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }

    Dimension ret = new Dimension();

    try {
      ret.width = Integer.parseInt(d[0]);
      ret.height = Integer.parseInt(d[1]);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this,"Error","Invalid terminal dimension\n"+e.getMessage(),
          JOptionPane.ERROR_MESSAGE);
      return null;
    }

    return ret;

  }

  public static void main(String[] args) {
    new MultiTerminal();
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if( src==connectBtn ) {

      Dimension termD = getTermDim();
      if(termD==null)
        return;

      String[] hList = makeStringArray(hostList.getText());
      if(hList.length==0) {
        JOptionPane.showMessageDialog(this,"Error","Empty host list",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Dimension scrsize = toolkit.getScreenSize();
      int xPos = 10;
      int yPos = 200;
      String command = commandText.getText();

      for(int termCount=0;termCount<hList.length;termCount++) {

//        MainPanel f = new MainPanel(hList[termCount],userText.getText(),passwordText.getText(),termD.width,termD.height,500);
//        f.setExitOnClose(true);
//        f.setAnswerYes(true);
//        if(command.length()>0)
//          f.setCommand(command);
//        Dimension d = f.getSize();
//        f.setLocation(xPos,yPos);
//        f.setVisible(true);
//
//        xPos += f.getWidth();
//        if(xPos+d.width>scrsize.width) {
//          xPos = 10;
//          yPos += d.height;
//        }

      }

    } else if ( src==loadButton ) {

      JFileChooser chooser = new JFileChooser(prefLastDir);
      int returnVal = chooser.showOpenDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {

        File f = chooser.getSelectedFile();
        try {
          StringBuffer str = new StringBuffer();
          boolean eof = false;
          FileReader fr = new FileReader(f);
          while(!eof) {
            int c = fr.read();
            eof = c<0;
            if(!eof) str.append((char)c);
          }
          fr.close();
          hostList.setText(str.toString());

        } catch (IOException ex) {
          JOptionPane.showMessageDialog(this,"Error","Error while loading " + f.getName() + "\n" + ex.getMessage(),
              JOptionPane.ERROR_MESSAGE);

        }
        if (f != null) prefLastDir = f.getAbsolutePath();

      }

    }

  }

}
