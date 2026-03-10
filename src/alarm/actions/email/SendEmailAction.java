/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package alarm.actions.email;

import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamView.GuiFrameManager;
import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;
import alarm.AlarmControl;
import alarm.AlarmDataUnit;
import alarm.AlarmParameters;
import alarm.actions.AlarmAction;

/**
 * @author mo55
 *
 */
public class SendEmailAction extends AlarmAction implements PamSettings {
	
	private int emailReady = AlarmAction.ALARM_CANT_DO;
	
	private SendEmailSettings emailSettings;
	
	private Session session;

	/**
	 * @param alarmControl
	 */
	public SendEmailAction(AlarmControl alarmControl) {
		super(alarmControl);
		emailSettings = new SendEmailSettings();
		PamSettingManager.getInstance().registerSettings(this);
	}

	/* (non-Javadoc)
	 * @see alarm.actions.AlarmAction#getActionName()
	 */
	@Override
	public String getActionName() {
		return "Send Email";
	}

	/* (non-Javadoc)
	 * @see alarm.actions.AlarmAction#hasSettings()
	 */
	@Override
	public boolean hasSettings() {
		return true;
	}

	public SendEmailSettings getEmailSettings() {
		return emailSettings;
	}
	
	
	/* (non-Javadoc)
	 * @see alarm.actions.AlarmAction#setSettings(java.awt.Window)
	 */
	@Override
	public boolean setSettings(Window window) {
		SendEmailSettings newParams = SendEmailSettingsDialog.showDialog(window, this);
		if (newParams != null) {
			emailSettings = newParams.clone();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see alarm.actions.AlarmAction#actOnAlarm(alarm.AlarmDataUnit)
	 */
	@Override
	public boolean actOnAlarm(AlarmDataUnit alarmDataUnit) {
		// check if the email has been configured properly
		if (emailReady!=AlarmAction.ALARM_CAN_DO) {
			System.out.println("Cannot send alarm - email settings incorrect");
			return false;
		}
		
		// check if this is one of the status types we want to send.  If not, return right away
		if ((alarmDataUnit.getCurrentStatus() == 1 && !emailSettings.isSendOnAmber()) ||
				(alarmDataUnit.getCurrentStatus() == 2 && !emailSettings.isSendOnRed())) {
			return true;
		}
		
		// generate the email in a separate thread, or else everything slows down
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>()
		{
		    @Override
		    protected Void doInBackground()
		    {
		    	createAndSendEmail(alarmDataUnit);
		        return null;
		    }
		};
		worker.execute();
		return true;
		
	}
	
	/**
	 * Create and send an email detailing the alarm
	 * 
	 * @param alarmDataUnit
	 */
	public void createAndSendEmail(AlarmDataUnit alarmDataUnit) {
			
		try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailSettings.getFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailSettings.getToAddress()));
            message.setSubject("PAMGuard Alarm " + alarmControl.getUnitName() + " has triggered");
            String newline = System.getProperty("line.separator"); 
            StringBuffer sb = new StringBuffer();
            sb.append("The alarm " + alarmControl.getUnitName() + " has triggered in PAMGuard.  These are the current alarm parameters: ");
            sb.append(newline);
            sb.append("   Time: " + PamCalendar.formatDateTime2(alarmDataUnit.getLastUpdate()));
            sb.append(newline);
            sb.append("   Status Level: " + AlarmParameters.sayLevel(alarmDataUnit.getCurrentStatus())); //String.valueOf(alarmDataUnit.getCurrentStatus()));
            sb.append(newline);
            sb.append("   Score: " + String.valueOf(alarmDataUnit.getCurrentScore()));
//            message.setText(sb.toString());
            Multipart mp = new MimeMultipart();
            MimeBodyPart p1 = new MimeBodyPart();
            p1.setText(sb.toString());
            mp.addBodyPart(p1);

            // attachment
            if (emailSettings.isAttachScreenshot()) {
            	GuiFrameManager frameManager = PamController.getInstance().getGuiFrameManager();
        		int nFrames = frameManager.getNumFrames();
        		for (int i = 0; i < nFrames; i++) {
    	            MimeBodyPart p2 = new MimeBodyPart();
        			JFrame aFrame = frameManager.getFrame(i);
    	            BufferedImage img = getScreenshot(aFrame);
    	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	            byte[] imageBytes=null;
    	            try {
						ImageIO.write(img, "png", baos);
						baos.flush();
						imageBytes= baos.toByteArray();
						baos.close();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
    	            ByteArrayDataSource bds = new ByteArrayDataSource(imageBytes, "image/png"); 
    	            p2.setFileName("Screenshot - Frame " + String.valueOf(i));
    	            p2.setDataHandler(new DataHandler(bds)); 
    	            mp.addBodyPart(p2);
        		}
            }

            message.setContent(mp);
            Transport.send(message);

            System.out.println(alarmControl.getUnitName() + " email sent");
        } catch (MessagingException e) {
            e.printStackTrace();
        }		
	}

	
	private BufferedImage getScreenshot(JFrame aFrame) {
		BufferedImage image = null;
		image = new BufferedImage(aFrame.getWidth(), aFrame.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = image.createGraphics();
		aFrame.paint(graphics2D);
		return image;
	}

	
	/* (non-Javadoc)
	 * @see alarm.actions.AlarmAction#canDo()
	 */
	@Override
	public int canDo() {
		return emailReady;
	}

	@Override
	public boolean prepareAction() {
		Properties prop = new Properties();
		prop.put("mail.smtp.host", emailSettings.getHost());
		prop.put("mail.smtp.port", emailSettings.getPort());
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true"); //TLS

		try {
			String pwd = getPassword(emailSettings.getUsername());
			session = Session.getInstance(prop, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(emailSettings.getUsername(), pwd);
				}
			});	
			Transport transport = session.getTransport("smtp");
		    transport.connect(emailSettings.getHost(), Integer.valueOf(emailSettings.getPort()), emailSettings.getUsername(), pwd);
		    transport.close();
		    System.out.println("Successfully connected to Email SMTP server");
		    emailReady=AlarmAction.ALARM_CAN_DO;
		 } 
		 catch(Exception e) {
			 String title = "Error connecting to Email SMTP server " + emailSettings.getHost();
			 String msg = "There was an error trying to authenticate the Email account " + emailSettings.getUsername() +
					 " to be used for alarm " + alarmControl.getUnitName() + 
					 ".  The alarm will not be able to send emails until " +
					 "this is fixed.  Please check the Alarm Email Settings to ensure they are correct.";
			 String help = null;
			 int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, e);
			 e.printStackTrace();
			 emailReady=AlarmAction.ALARM_CANT_DO;
		 }
		return true;
	}

	/**
	 * @return
	 */
	protected String getPassword(String username) {
		PasswordDialog pwDialog = new PasswordDialog(username);
		String pWord = new String(pwDialog.password.getPassword()); // store password in temp String
		pwDialog.password.setText("");	// clear password field
		return pWord;
	}

	
	private class PasswordDialog extends PamDialog {

		private static final long serialVersionUID = 1L;
		private JPasswordField password;

		/**
		 * @param parentFrame
		 */
		private PasswordDialog(String username) {
			super(null, alarmControl.getUnitName() + " requires password", false);
			JPanel mainPanel = new JPanel();
			mainPanel.setBorder(new EmptyBorder(5,5,5,5));
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			JLabel pwordLbl = new JLabel ("<html><div WIDTH=250>The Alarm module " + alarmControl.getUnitName() +
					" has been configured to send an email using the account<br><br>" +
					emailSettings.getUsername() +
					"<br><br>Please enter the password below to validate the credentials.<br><br></div></html>", JLabel.LEFT);
			mainPanel.add(pwordLbl);
			mainPanel.add(password = new JPasswordField(0));
			setDialogComponent(mainPanel);
			this.setVisible(true);
		}

		/* (non-Javadoc)
		 * @see PamView.dialog.PamDialog#getParams()
		 */
		@Override
		public boolean getParams() {
			return true;
		}

		/* (non-Javadoc)
		 * @see PamView.dialog.PamDialog#cancelButtonPressed()
		 */
		@Override
		public void cancelButtonPressed() {
		}

		/* (non-Javadoc)
		 * @see PamView.dialog.PamDialog#restoreDefaultSettings()
		 */
		@Override
		public void restoreDefaultSettings() {
		}
	}


	/* (non-Javadoc)
	 * @see PamController.SettingsNameProvider#getUnitName()
	 */
	@Override
	public String getUnitName() {
		return alarmControl.getUnitName();
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitType()
	 */
	@Override
	public String getUnitType() {
		return "Send Email Alarm Action";
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsReference()
	 */
	@Override
	public Serializable getSettingsReference() {
		return emailSettings;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsVersion()
	 */
	@Override
	public long getSettingsVersion() {
		return emailSettings.serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#restoreSettings(PamController.PamControlledUnitSettings)
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		emailSettings = ((SendEmailSettings) pamControlledUnitSettings.getSettings()).clone();
		return (emailSettings != null);
	}

}
