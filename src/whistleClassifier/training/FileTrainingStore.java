package whistleClassifier.training;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class FileTrainingStore implements TrainingDataStore {

	private boolean storeOk;

	private ObjectOutputStream outputStream;

	public FileTrainingStore() {

	}

	@Override
	synchronized public boolean closeStore() {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	synchronized public boolean openStore(String storeName) {
		try {
			outputStream=  new ObjectOutputStream(new FileOutputStream(
					storeName));

		} catch (Exception Ex) {
			System.out.println(Ex);
			storeOk = false;
		}
		storeOk = true;
		return storeOk;
	}

	@Override
	synchronized public boolean writeData(TrainingDataSet trainingDataSet) {
		synchronized (trainingDataSet) {
			if (outputStream != null) {
				try {
					outputStream.writeObject(trainingDataSet);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	synchronized public TrainingDataSet readData(String storeName) {

		TrainingDataSet trainingDataSet = null;
			ObjectInputStream inputStream;
			try {
				inputStream =  new ObjectInputStream(new FileInputStream(
						storeName));

			} catch (Exception Ex) {
				System.out.println(Ex);
				return null;
			}

			Object o;
			try {
				o =  inputStream.readObject();
				trainingDataSet = (TrainingDataSet) o;
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

			try {
				inputStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		return trainingDataSet;
	}

	@Override
	public boolean isStoreOk() {
		return storeOk;
	}


}
