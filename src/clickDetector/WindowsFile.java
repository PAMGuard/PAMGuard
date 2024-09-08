/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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

package clickDetector;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Single;

/**
 * 
 * @author doug Gillespie
 *         <p>
 *         Some additions to RandomAccessFile to write binary data that is
 *         compatible with the RainbowClick file format.
 */
public class WindowsFile extends RandomAccessFile implements WriteWinFile , ReadWinFile {

	public WindowsFile(String file, String mode) throws IOException {
		super(file, mode);
	}

	public WindowsFile(File file, String mode) throws IOException {
		super(file, mode);
	}
	
	/**
	 * Go to the end of the file. 
	 * @throws IOException 
	 */
	public void seekEnd() throws IOException {
		seek(length());
	}

	/* (non-Javadoc)
	 * @see clickDetector.WriteWinFile#writeWinInt(int)
	 */
	@Override
	public void writeWinInt(int val) throws IOException {
		writeWinShort(val & 0xFFFF);
		writeWinShort(val >> 16);
		
	}
	
	/**
	 * Read a Windows 32 bit integer
	 */
	@Override
	public int readWinInt() throws IOException {
		int val = readWinShort();
		if (val < 0) val+=65536;
		int val2 = readWinShort();
		val += val2 << 16;
		return val;
	}

	/* (non-Javadoc)
	 * @see clickDetector.WriteWinFile#writeWinShort(int)
	 */
	@Override
	public void writeWinShort(int val) throws IOException {
		/*
		 * Write out low byte first
		 */
		writeByte(0xFF & val);
		writeByte(0xFF & (val >> 8));
	}
	
	@Override
	public int readWinShort() throws IOException {
		/*
		 * Read the low byte first
		 */
		int val = readByte();
		if (val < 0) val += 256;
		int b2 = readByte();
		val += b2 << 8;
		
		return val;
		
	}
	
	public int readWinUShort() throws IOException {
		/*
		 * Read the low byte first
		 */
		int val = readByte();
		if (val < 0) val += 256;
		int b2 = readByte();
		if (b2 < 0) b2 += 256;
		val += b2 << 8;
		
		return val;
		
	}
	
	/* (non-Javadoc)
	 * @see clickDetector.WriteWinFile#writeWinFloat(float)
	 */
	@Override
	public void writeWinFloat(float val) throws IOException {
//		java.lang.Float floatValue = new Float(val);
		int intValue = Float.floatToIntBits(val);
		writeWinInt(intValue);
	}
	
	@Override
	public float readWinFloat() throws IOException {
		int val = readWinInt();
		return Float.intBitsToFloat(val);
	}
	
	/* (non-Javadoc)
	 * @see clickDetector.WriteWinFile#writeWinDouble(double)
	 */
	@Override
	public void writeWinDouble(double val) throws IOException {
		long intValue = Double.doubleToLongBits(val);
		writeWinLong(intValue);
	}
	
	@Override
	public double readWinDouble() throws IOException {
		long val = readWinLong();
		return Double.longBitsToDouble(val);
	}
	
	/* (non-Javadoc)
	 * @see clickDetector.WriteWinFile#writeWinLong(long)
	 */
	@Override
	public void writeWinLong(long longValue) throws IOException {
		writeWinInt((int) (0xFFFFFFFF & longValue));
		writeWinInt((int) (0xFFFFFFFF & (longValue >> 32)));
	}
	
	@Override
	public long readWinLong() throws IOException {
		long val = readWinInt();
		val += readWinInt() << 32;
		return val;
	}
	
}
