package likelihoodDetectionModule;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The TargetConfigurationNameDialog is used by the LikelihoodDetectionParametersDialog to
 * allow the user to enter in a new target configuration name.
 */
public class TargetConfigurationNameDialog extends JDialog implements ActionListener, KeyListener {
	
	/** The Constant serialVersionUID. */
	static final long serialVersionUID = 121212;
	
	/** The existing identifiers. */
	private ArrayList<String> existingIdentifiers = null;
	
	/** The ok button. */
	private JButton okButton = null;
	
	/** The cancel button. */
	private JButton cancelButton = null;
	
	/** The main panel. */
	private JPanel mainPanel = null;
	
	/** The message. */
	private JLabel message = null;
	
	/** The accepted. */
	private boolean accepted = false;
	
	/** The identifier field. */
	private JTextField identifierField = null;
	
	/**
	 * Instantiates a new target configuration name dialog.
	 * 
	 * @param existingIdentifiers The existing target configuration identifiers. The
	 * dialog will make sure the new name is unique.
	 */
	public TargetConfigurationNameDialog( ArrayList<String> existingIdentifiers ) {
		super( (JFrame)null, true );
		this.setTitle( "New Target Configuration Name" );
		this.setSize( new Dimension( 300, 200 ) );
		this.setResizable( false );
		mainPanel = new JPanel();
		BoxLayout layout = new BoxLayout( mainPanel, BoxLayout.Y_AXIS );
		mainPanel.setLayout( layout );
		getContentPane().add( mainPanel );
		message = new JLabel( "Enter new target configuration identifier." );
		mainPanel.add( message );
		identifierField = new JTextField( 30 );
		identifierField.addKeyListener( this );
		mainPanel.add( identifierField );
		JPanel buttonPanel = new JPanel();
		okButton = new JButton( "OK" );
		cancelButton = new JButton( "Cancel" );
		okButton.addActionListener( this );
		okButton.setEnabled( false );
		cancelButton.addActionListener( this );
		buttonPanel.add( okButton );
		buttonPanel.add( cancelButton );
		mainPanel.add( buttonPanel );
		this.rootPane.setDefaultButton( okButton );
		pack();	
		this.existingIdentifiers = existingIdentifiers;
	}
	
	/**
	 * Show dialog.
	 * 
	 * @return the string
	 */
	public String showDialog() {
		setVisible( true );
		
		if ( accepted ) {
			return identifierField.getText();	
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		if ( e.getSource() == okButton ) {
		
			accepted = true;
			setVisible( false );
		}
		else if ( e.getSource() == cancelButton ) {
			setVisible( false );
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased( KeyEvent e ) {
		
		if ( identifierField.getText().isEmpty() ) {
			okButton.setEnabled( false );
			return;
		}
		
		if ( existingIdentifiers.size() == 0 ) {
			okButton.setEnabled( true );
			identifierField.setForeground( Color.black );
			return;
		}
		
		Iterator<String> i = existingIdentifiers.iterator();
		while ( i.hasNext() ) {
			String id = i.next();
			if ( id.equals( identifierField.getText() ) ) {
				// This is not good.
				message.setText( "This target configuration identifier is in use." );
				identifierField.setForeground( Color.RED );
				okButton.setEnabled( false );
				break;
			}
			else if ( id.isEmpty() ) {
				message.setText( "Enter new target configuration identifier." );
				okButton.setEnabled( false );
				break;
			}
			else {
				message.setText( "Enter new target configuration identifier." );
				identifierField.setForeground( Color.BLACK );
				okButton.setEnabled( true );
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped( KeyEvent e ) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed( KeyEvent e ) {}
}
