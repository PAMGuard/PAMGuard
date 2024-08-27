package rocca;


import java.io.BufferedInputStream;
import java.io.FileInputStream;

import javax.help.HelpSet;
import javax.help.JHelpContentViewer;
import javax.swing.ProgressMonitorInputStream;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


/**
 * Generates a little ARFF file with different attribute types.
 *
 * @author FracPete
 */
public class AttTest {

    RandomForest cls;
    Instances data;

    public static void main(String args[]) {
        System.out.println("Starting helper");
        try {
            JHelpContentViewer viewer = new JHelpContentViewer(new HelpSet(null,null));
        } catch (Exception ex) {
            
        }

        System.out.println("Program running...");
        AttTest testit = new AttTest();
        //testit.runAttTest();
        System.out.println("Finished!");
    }

    public void runAttTest() {

        // the current data to use with classifier
        // deserialize model
        System.out.println("Program still running...");
        String fname = "C:/Users/Michael/Documents/Work/Java/RF8sp12att.model";
//        Vector<Object> test = new Vector<Object>();

        try {
//              ObjectInputStream  input =
//                new ObjectInputStream
//                ( new BufferedInputStream(
//                  (new ProgressMonitorInputStream(null, "Loading",
//                                                  new FileInputStream(fname)))));
                BufferedInputStream input = new BufferedInputStream(
                  (new ProgressMonitorInputStream(null, "Loading",
                                                  new FileInputStream(fname))));
              Object[] o = weka.core.SerializationHelper.readAll(input);
//              while (true) {
//                  test.add(input.readObject());
//              }
            input.close();
        } catch (Exception ex) {
            System.err.println("Deserialization failed: " + ex.getMessage());
            ex.printStackTrace();

        }
/*
            cls = (RandomForest) o[0];
            data = (Instances) o[1];
            int dataSize = data.numAttributes();
            for (int i=0; i<dataSize; i++) {
                // skip the index position if it's the class attribute
                if (i!=data.classIndex()) {
                    System.out.println("Classifier attribute is " +
                            data.attribute(i).name() + '.');
                } else {
                    System.out.println("Classifier attribute is " +
                            data.attribute(i).name() +
                            " - it is the class index!");
                }
            }

            System.out.println("This one worked too");

        System.out.println("Out of the try/catch");
*/
    }
}

