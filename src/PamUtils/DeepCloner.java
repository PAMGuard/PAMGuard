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

package PamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DeepCloner {
	
	/**
	 * Make a clone of the passed serializable object.  Note that instead of using the clone method,
	 * this will serial/deserialize the object to make a deep copy.  See this page:
	 * http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
	 * for complete details.
	 * 
	 * @param objectToClone the object to clone.  Must implement Serializable
	 * @return the cloned object
	 * @throws CloneNotSupportedException
	 */ 
	public static Object deepClone(Serializable objectToClone) throws CloneNotSupportedException {
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(objectToClone);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            Object newOne = in.readObject();
            return newOne;		
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new CloneNotSupportedException(e.getMessage());
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            throw new CloneNotSupportedException(cnfe.getMessage());
        }
	}
	

}
