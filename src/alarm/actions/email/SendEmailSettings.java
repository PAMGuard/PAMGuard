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

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import alarm.actions.serial.AlarmSerialSettings;

/**
 * @author mo55
 *
 */
public class SendEmailSettings implements Serializable, Cloneable, ManagedParameters {


	public static final long serialVersionUID = 1L;
	
	private String host = "smtp.gmail.com";
	
	private String port = "587";
	
	private String username = "***@gmail.com";
	
	private String toAddress = "***@***.com";
	
	private String fromAddress = "***@***.com";
	
	private boolean sendOnAmber = false;
	
	private boolean sendOnRed = false;
	
	private boolean attachScreenshot = false;
	

	/**
	 * 
	 */
	public SendEmailSettings() {
		// TODO Auto-generated constructor stub
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public String getPort() {
		return port;
	}


	public void setPort(String port) {
		this.port = port;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getToAddress() {
		return toAddress;
	}


	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}


	public String getFromAddress() {
		return fromAddress;
	}


	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected SendEmailSettings clone() {
		try {
			return (SendEmailSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


	public boolean isSendOnAmber() {
		return sendOnAmber;
	}


	public void setSendOnAmber(boolean sendOnAmber) {
		this.sendOnAmber = sendOnAmber;
	}


	public boolean isSendOnRed() {
		return sendOnRed;
	}


	public void setSendOnRed(boolean sendOnRed) {
		this.sendOnRed = sendOnRed;
	}


	public boolean isAttachScreenshot() {
		return attachScreenshot;
	}


	public void setAttachScreenshot(boolean attachScreenshot) {
		this.attachScreenshot = attachScreenshot;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}


}
