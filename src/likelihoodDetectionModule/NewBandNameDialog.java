package likelihoodDetectionModule;

import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.util.ArrayList;
import javax.swing.JTextField;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.BoxLayout;
import java.awt.*;

/**
 * The NewBandNameDialog is used by the LikelihoodDetectionParametersDialog to allow the
 * operator to specify a band name.
 */
public class NewBandNameDialog extends JDialog implements ActionListener, KeyListener {
	
	/** The constant serialVersionUID required by Serialize. */
	static final long serialVersionUID = 121213;
	
	/**
	 * The Enum BandType specifies whether the band name will be for a signal band or a
	 * guard band.
	 */
	public enum BandType {
		
		/** A Signal band. */
		Signal,
		
		/** A Guard band. */
		Guard
	}
	
	/** The existing identifiers. The dialog will enforce a unique identifier be specified. */
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
	
	/** The band type. */
	private BandType bandType = null;
	
	/**
	 * Instantiates a new new band name dialog.
	 * 
	 * @param type The type of band for which the name is.
	 * @param existingIdentifiers A list of the existing band identifiers for the type of band.
	 */
	public NewBandNameDialog( BandType type, ArrayList<String> existingIdentifiers ) {
		super( (JFrame)null, true );
		
		this.bandType = type;
		
		if ( this.bandType == BandType.Signal ) {
			this.setTitle( "New Signal Band Name" );
		}
		else {
			this.setTitle( "New Guard Band Name" );	
		}
		
		this.setSize( new Dimension( 300, 200 ) );
		this.setResizable( false );
		mainPanel = new JPanel();
		BoxLayout layout = new BoxLayout( mainPanel, BoxLayout.Y_AXIS );
		mainPanel.setLayout( layout );
		getContentPane().add( mainPanel );
		
		if ( this.bandType == BandType.Signal ) {
			message = new JLabel( "Enter new signal band identifier." );
		}
		else {
			message = new JLabel( "Enter new guard band identifier." );	
		}
		
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
	public void keyReleased( KeyEvent e ) {
		
		if ( identifierField.getText().isEmpty() ) {
			okButton.setEnabled( false );
			return;
		}
		
		if ( identifierField.getText().equals( "None" ) ) {
			message.setText( "The identifier None is not allowed." );
			identifierField.setForeground( Color.red );
			okButton.setEnabled( false );
			return;
		}
		
		Iterator<String> i = existingIdentifiers.iterator();
		
		if ( existingIdentifiers.size() == 0 ) {
			okButton.setEnabled( true );
			this.rootPane.setDefaultButton( okButton );
			identifierField.setForeground( Color.black );
			return;
		}
		
		while ( i.hasNext() ) {
			String id = i.next();
			
			if ( id.equals( identifierField.getText() ) ) {
				// This is not good.
				message.setText( "This identifier is already in use." );
				identifierField.setForeground( Color.RED );
				okButton.setEnabled( false );
				break;
			}
			else if ( id.isEmpty() ) {
				if ( this.bandType == BandType.Signal ) {
					message.setText( "Enter new signal band identifier." );
				}
				else {
					message.setText( "Enter new guard band identifier." );
				}
				okButton.setEnabled( false );
				break;
			}
			else {
				if ( this.bandType == BandType.Signal ) {
					message.setText( "Enter new signal band identifier." );
				}
				else {
					message.setText( "Enter new guard band identifier. " );	
				}
				
				identifierField.setForeground( Color.BLACK );
				okButton.setEnabled( true );
				this.rootPane.setDefaultButton( okButton );
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped( KeyEvent e ) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed( KeyEvent e ) {}
}

