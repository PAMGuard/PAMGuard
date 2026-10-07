package difar.offline;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import PamUtils.PamCalendar;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryTypes;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import difar.DifarDataBlock;
import difar.DifarDataUnit;

/**
 * Writes the clips for one time slot to a binary file and its index file,
 * under temporary names beside their final ones.
 * <p>
 * The header time is the slot start and the footer time the slot end. The
 * footer's UID range is the lowest and highest UID actually in the file. The
 * stream's own footer would take them from its start counter and the block's
 * latest UID, which in the viewer have nothing to do with this file. The
 * index file is written here too, since the stream only writes one after
 * its own footer.
 */
public class SlotFileWriter {

	/** Added to a final name while the file is being written. */
	public static final String TEMP_SUFFIX = ".tmp";

	/** What was written for one slot. */
	public static final class Written {
		public final long slotStart;
		public final File dataFile, indexFile;
		public final File tempDataFile, tempIndexFile;
		public final BinaryHeader header;
		public final BinaryFooter footer;
		public final ModuleHeader moduleHeader;
		public final ModuleFooter moduleFooter;
		public final List<DifarDataUnit> clips;

		Written(long slotStart, File dataFile, File indexFile, BinaryHeader header, BinaryFooter footer,
				ModuleHeader moduleHeader, ModuleFooter moduleFooter, List<DifarDataUnit> clips) {
			this.slotStart = slotStart;
			this.dataFile = dataFile;
			this.indexFile = indexFile;
			this.tempDataFile = new File(dataFile.getPath() + TEMP_SUFFIX);
			this.tempIndexFile = new File(indexFile.getPath() + TEMP_SUFFIX);
			this.header = header;
			this.footer = footer;
			this.moduleHeader = moduleHeader;
			this.moduleFooter = moduleFooter;
			this.clips = clips;
		}

		/** Remove the temporary files, after a failure. */
		public void deleteTemps() {
			tempDataFile.delete();
			tempIndexFile.delete();
		}
	}

	private final BinaryStore binaryStore;
	private final DifarDataBlock dataBlock;

	public SlotFileWriter(BinaryStore binaryStore, DifarDataBlock dataBlock) {
		this.binaryStore = binaryStore;
		this.dataBlock = dataBlock;
	}

	/**
	 * The file a slot's clips go in, named from the slot start the same way
	 * normal mode names a file from the time it opens.
	 * @param slotStart start of the slot.
	 * @return the binary data file.
	 */
	public File dataFileFor(long slotStart) {
		BinaryDataSource source = dataBlock.getBinaryDataSource();
		String folder = binaryStore.getFolderName(slotStart, true);
		String name = PamCalendar.createFileName(slotStart, source.createFilenamePrefix(), BinaryStore.fileType);
		return new File(folder + name.replaceAll(" ", "_"));
	}

	/**
	 * Write one slot's clips to temporary files.
	 * @param grid the grid the slot is on.
	 * @param slotStart start of the slot.
	 * @param clips the clips, sorted by time, all starting in the slot.
	 * @return what was written, or null if writing failed. Nothing is left
	 * behind on failure.
	 */
	public Written write(TimeGrid grid, long slotStart, List<DifarDataUnit> clips) {
		BinaryDataSource source = dataBlock.getBinaryDataSource();
		File dataFile = dataFileFor(slotStart);
		File indexFile = binaryStore.swapFileType(dataFile, BinaryStore.indexFileType);
		File tempData = new File(dataFile.getPath() + TEMP_SUFFIX);
		File tempIndex = new File(indexFile.getPath() + TEMP_SUFFIX);
		long slotEnd = slotStart + grid.getWidthMillis();
		long now = System.currentTimeMillis();

		BinaryOutputStream stream = new BinaryOutputStream(binaryStore, dataBlock);
		source.setBinaryStorageStream(stream);
		if (!stream.openPGDFFile(tempData)) {
			return fail("could not open " + tempData, tempData, tempIndex);
		}
		byte[] moduleHeaderData = source.getModuleHeaderData();
		boolean ok = stream.writeHeader(slotStart, now);
		ok &= stream.writeModuleHeader(moduleHeaderData);
		long lowest = Long.MAX_VALUE, highest = Long.MIN_VALUE;
		for (DifarDataUnit clip : clips) {
			ok &= source.saveData(clip);
			lowest = Math.min(lowest, clip.getUID());
			highest = Math.max(highest, clip.getUID());
		}
		byte[] moduleFooterData = source.getModuleFooterData();
		ok &= stream.writeModuleFooter(moduleFooterData);
		/*
		 * The stream counts each object twice, so its count is not used.
		 * Every clip was written if every write reported success.
		 */
		BinaryFooter footer = new BinaryFooter(slotEnd, now, clips.size(), stream.getFileSize());
		footer.setFileEndReason(BinaryFooter.END_RUNSTOPPED);
		footer.setLowestUID(lowest);
		footer.setHighestUID(highest);
		ok &= stream.writeFileFooter(footer);
		ok &= stream.closeFile();
		if (!ok) {
			return fail("could not write all " + clips.size() + " clips to " + tempData, tempData, tempIndex);
		}

		BinaryHeader header = new BinaryHeader(source.getModuleType(), source.getModuleName(),
				source.getStreamName(), BinaryStore.getCurrentFileFormat());
		header.setDataDate(slotStart);
		header.setAnalysisDate(now);
		if (!writeIndex(tempIndex, stream, header, moduleHeaderData, moduleFooterData, footer)) {
			return fail("could not write " + tempIndex, tempData, tempIndex);
		}

		ModuleHeader moduleHeader = null;
		ModuleFooter moduleFooter = null;
		if (moduleHeaderData != null) {
			BinaryObjectData data = new BinaryObjectData(0, BinaryTypes.MODULE_HEADER,
					moduleHeaderData, moduleHeaderData.length);
			data.setVersionNumber(source.getModuleVersion());
			moduleHeader = source.sinkModuleHeader(data, header);
		}
		if (moduleFooterData != null) {
			BinaryObjectData data = new BinaryObjectData(0, BinaryTypes.MODULE_FOOTER,
					moduleFooterData, moduleFooterData.length);
			moduleFooter = source.sinkModuleFooter(data, header, moduleHeader);
		}
		return new Written(slotStart, dataFile, indexFile, header, footer, moduleHeader, moduleFooter, clips);
	}

	/**
	 * An index file holds the file's header, module header, module footer
	 * and footer, in that order, as the stream writes it.
	 */
	private boolean writeIndex(File indexFile, BinaryOutputStream stream, BinaryHeader header,
			byte[] moduleHeaderData, byte[] moduleFooterData, BinaryFooter footer) {
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)))) {
			boolean ok = header.writeHeader(out);
			ok &= stream.writeModuleHeader(out, moduleHeaderData);
			ok &= stream.writeModuleFooter(out, moduleFooterData);
			ok &= footer.writeFooter(out, BinaryStore.getCurrentFileFormat());
			return ok;
		}
		catch (IOException e) {
			System.out.println("DIFAR: " + e);
			return false;
		}
	}

	private Written fail(String why, File tempData, File tempIndex) {
		System.out.println("DIFAR: " + why);
		tempData.delete();
		tempIndex.delete();
		return null;
	}
}
