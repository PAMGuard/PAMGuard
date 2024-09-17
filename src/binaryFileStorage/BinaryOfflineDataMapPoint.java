package binaryFileStorage;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import dataGram.Datagram;
import dataGram.DatagramPoint;
import dataMap.OfflineDataMapPoint;


public class BinaryOfflineDataMapPoint extends OfflineDataMapPoint implements Serializable, DatagramPoint, ManagedParameters {

	private static final long serialVersionUID = 1L;
	
	private BinaryHeader binaryHeader;
	private BinaryFooter binaryFooter;
	private ModuleHeader moduleHeader;
	private ModuleFooter moduleFooter;
	
	//removed so can be just relative to data store location this means when whole folder copied or read on different platform it's still compatible
//	private File binaryFile;
	
	private URI relativeURI;
//	transient BinaryStore binaryStore;

	private Datagram datagram;
	
	/*
	 * 
	 * relPathInsideBinStorage will store at pos0 the filename and the parent at pos1 etc etc.
	 * but will stop at the binary storage folder name and not store it.
	 * This is so we can store the relative path in the data map but it
	 * does not depend on either the absolute path which will cause problems
	 * when the folder is moved or a network drive is mapped to a location on
	 * a different computer and also so it does not depend on OS by having just
	 * a string which will contain the OS specific file separator.
	 * 
	 */
	

	
	public BinaryOfflineDataMapPoint(BinaryStore binaryStore, File file, BinaryHeader binaryHeader,
			BinaryFooter binaryFooter, ModuleHeader moduleHeader, ModuleFooter moduleFooter, Datagram datagram) {
		super(binaryHeader.getDataDate(), 
					binaryFooter != null ? binaryFooter.getDataDate() : -1, 
							binaryFooter != null ? binaryFooter.getNObjects() : -1, 0);
		
//		this.binaryStore=binaryStore;
		String binaryStoreFolderLocation = binaryStore.binaryStoreSettings.getStoreLocation();
		URI binaryStoreFolderURI = new File(binaryStoreFolderLocation).toURI();
		URI binaryFileURI = file.toURI();
		
		
		this.relativeURI = binaryStoreFolderURI.relativize(binaryFileURI);
//		this.binaryFile = file; 
		this.binaryHeader = binaryHeader;
		this.binaryFooter = binaryFooter;
		this.moduleHeader = moduleHeader;
		this.moduleFooter = moduleFooter;
		if (binaryFooter != null) {
			setLowestUID(binaryFooter.getLowestUID());
			setHighestUID(binaryFooter.getHighestUID());
			if (binaryFooter.getHighestUID() == null) {
				setMissingUIDs(binaryFooter.getNObjects());
			}
		}
		
		setDatagram(datagram);
	}
	
	public BinaryOfflineDataMapPoint() {
		super(0,0,0,0);
	}
	
	public void update(BinaryStore binaryStore, File file, BinaryHeader binaryHeader,
			BinaryFooter binaryFooter, ModuleHeader moduleHeader, ModuleFooter moduleFooter, Datagram datagram) {
		
//		this.binaryStore=binaryStore;
		String binaryStoreFolderLocation = binaryStore.binaryStoreSettings.getStoreLocation();
		URI binaryStoreFolderURI = new File(binaryStoreFolderLocation).toURI();
		URI binaryFileURI = file.toURI();
		
		
		this.relativeURI = binaryStoreFolderURI.relativize(binaryFileURI);
//		this.binaryFile = file;
		this.binaryHeader = binaryHeader;
		this.binaryFooter = binaryFooter;
		this.moduleHeader = moduleHeader;
		this.moduleFooter = moduleFooter;
		setDatagram(datagram);
	}


	@Override
	public String getName() {
		if (relativeURI == null) {
			return "No file !";
		}
//		This apparently caused problems and can see that File(URI) should 
//		be absolute URI though this wasn't required at original change.
//		return new File(relativeURI).getName();
		
		
		String name = relativeURI.toString();
		if (name.lastIndexOf("/")!=-1){
			name = name.substring(name.lastIndexOf("/")+1);
		}
		
//		Shouldn't happen but should make caller throw null pointer exception
		if (name.length()==0) name =null;
		
		return name;
	}

	/**
	 * 
	 * @return the binary file header
	 */
	public BinaryHeader getBinaryHeader() {
		return binaryHeader;
	}
	
	/**
	 * 
	 * @return the binary file footer
	 */
	public BinaryFooter getBinaryFooter() {
		return binaryFooter;
	}
	
	/**
	 * @return the binaryFile
	 */
	public File getBinaryFile(BinaryStore binaryStore) {
		if (relativeURI == null) {
			return null;
		}
		return new File(new File(binaryStore.binaryStoreSettings.getStoreLocation()).toURI().resolve(relativeURI));
		
	}
	
//	/**
//	 * Set the binary file name. 
//	 * @param binaryStoreFolderLocation
//	 * @param binaryFile
//	 */
//	public void setBinaryFile(String binaryStoreFolderLocation, File binaryFile) {
//		URI binaryStoreFolderURI = new File(binaryStoreFolderLocation).toURI();
//		URI binaryFileURI = binaryFile.toURI();
//		this.relativeURI = binaryStoreFolderURI.relativize(binaryFileURI);
//	}
	
	/**
	 * GEt a binary file using the relative file name + a new root folder name. 
	 * @param rootFolder
	 * @return File using the given path name. 
	 */
	public File getBinaryFile(String rootFolder) {
		if (relativeURI == null) {
			return null;
		}
		return new File(new File(rootFolder).toURI().resolve(relativeURI));
	}
	
//	/**
//	 * @param the The file where the binaryFile has been moved to
//	 */
//	public void setBinaryFile(BinaryStore binaryStore, File file) {
//		this.binaryFile=file;
//	}
	
//	/**
//	 * @return the binaryFile
//	 */
//	public File getBinaryFile() {
//		File dir;
//		
//		FileFinder.findFileName(initDir, binaryFile.getName())
//		return findFile(binaryFile.getName(),dir);
//		
//	}
	
	

	/**
	 * @return the moduleHeader
	 */
	public ModuleHeader getModuleHeader() {
		return moduleHeader;
	}

	/**
	 * @param moduleHeader the moduleHeader to set
	 */
	public void setModuleHeader(ModuleHeader moduleHeader) {
		this.moduleHeader = moduleHeader;
	}

	/**
	 * @return the moduleFooter
	 */
	public ModuleFooter getModuleFooter() {
		return moduleFooter;
	}

	/**
	 * @param moduleFooter the moduleFooter to set
	 */
	public void setModuleFooter(ModuleFooter moduleFooter) {
		this.moduleFooter = moduleFooter;
	}

	/**
	 * @return the datagram
	 */
	@Override
	public Datagram getDatagram() {
		return datagram;
	}

	/**
	 * @param datagram the datagram to set
	 */
	@Override
	public void setDatagram(Datagram datagram) {
		this.datagram = datagram;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (relativeURI != null) {
			return relativeURI.toString();
		}
		return super.toString();
	}

	public void setBinaryFooter(BinaryFooter binaryFooter) {
		this.binaryFooter = binaryFooter;
		if (binaryFooter != null) {
			setEndTime(binaryFooter.getDataDate());
		}
	}

	/* (non-Javadoc)
	 * @see dataMap.OfflineDataMapPoint#getLowestUId()
	 */
	@Override
	public Long getLowestUID() {
		if (binaryFooter == null) return null;
		return binaryFooter.getLowestUID();
	}

	/* (non-Javadoc)
	 * @see dataMap.OfflineDataMapPoint#getHighestUID()
	 */
	@Override
	public Long getHighestUID() {
		if (binaryFooter == null) return null;
		return binaryFooter.getHighestUID();
	}

	@Override
	public void setLowestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHighestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param binaryHeader the binaryHeader to set
	 */
	public void setBinaryHeader(BinaryHeader binaryHeader) {
		this.binaryHeader = binaryHeader;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("relativeURI");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return relativeURI;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
}
