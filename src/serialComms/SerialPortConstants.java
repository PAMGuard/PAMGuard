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



package serialComms;

/**
 * Constants copied from the RXTX library gnu.io.SerialPortInterface.  Values are
 * reproduced here so that the RXTX library can be removed but the constants still available
 * for use by other classes.
 * 
 * Important: the majority of the constants are commented out because the jSerialComm library uses
 * it's own constants.  They match the RXTX library for Parity, BUT ARE DIFFERENT FOR STOP BITS AND
 * FLOWCONTROL.  The jSerialComm values are given below the RXTX values for reference.  We don't think
 * this is a big problem because Flowcontrol NONE and Stop Bits 1 is the same in both libraries, and that's generally
 * what most people will be using. 
 * 
 * @author mo55
 *
 */
public class SerialPortConstants {
	
	public static final int DATABITS_5 = 5;
	public static final int DATABITS_6 = 6;
	public static final int DATABITS_7 = 7;
	public static final int DATABITS_8 = 8;
//	public static final int PARITY_NONE = 0;
//	public static final int PARITY_ODD = 1;
//	public static final int PARITY_EVEN = 2;
//	public static final int PARITY_MARK = 3;
//	public static final int PARITY_SPACE = 4;
//	public static final int STOPBITS_1 = 1;	
//	public static final int STOPBITS_2 = 2;
//	public static final int STOPBITS_1_5 = 3;
//	public static final int FLOWCONTROL_NONE = 0;
//	public static final int FLOWCONTROL_RTSCTS_IN = 1;
//	public static final int FLOWCONTROL_RTSCTS_OUT = 2;
//	public static final int FLOWCONTROL_XONXOFF_IN = 4;
//	public static final int FLOWCONTROL_XONXOFF_OUT = 8;
	  
/* jSerialComm constants
	 public static final int	NO_PARITY	0
	 public static final int	ODD_PARITY	1
 	 public static final int	EVEN_PARITY	2
	 public static final int	MARK_PARITY	3
	 public static final int	SPACE_PARITY 4
	 
	 public static final int	FLOW_CONTROL_DISABLED	0
	 public static final int	FLOW_CONTROL_RTS_ENABLED	1
	 public static final int	FLOW_CONTROL_CTS_ENABLED	16
	 public static final int	FLOW_CONTROL_DSR_ENABLED	256
	 public static final int	FLOW_CONTROL_DTR_ENABLED	4096
	 public static final int	FLOW_CONTROL_XONXOFF_IN_ENABLED	65536
	 public static final int	FLOW_CONTROL_XONXOFF_OUT_ENABLED	1048576
	 
	 public static final int	ONE_STOP_BIT	1
	 public static final int	ONE_POINT_FIVE_STOP_BITS	2
	 public static final int	TWO_STOP_BITS	3
	 
	 public static final int	LISTENING_EVENT_DATA_AVAILABLE	1
	 public static final int	LISTENING_EVENT_DATA_RECEIVED	16
	 public static final int	LISTENING_EVENT_DATA_WRITTEN	256
	 public static final int	TIMEOUT_NONBLOCKING	0
	 public static final int	TIMEOUT_READ_BLOCKING	16
	 public static final int	TIMEOUT_READ_SEMI_BLOCKING	1
	 public static final int	TIMEOUT_SCANNER	4096
	 public static final int	TIMEOUT_WRITE_BLOCKING	256
 */

}
