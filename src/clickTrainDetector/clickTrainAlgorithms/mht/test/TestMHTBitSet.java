package clickTrainDetector.clickTrainAlgorithms.mht.test;

import java.util.BitSet;

import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;

/**
 * Test how BitSets work
 * @author Jamie Macaulay 
 *
 */
public class TestMHTBitSet extends MHTTestChart {

    public static void main(String[] args) {
      
    	//test length
    	BitSet bitSet= new BitSet(3);
       
       System.out.println("BitSet size: " + bitSet.size() +  " length: " + bitSet.length());
       
       bitSet.set(5,false);
       
       System.out.println("BitSet size: " + bitSet.size() +  " length: " + bitSet.length());
       
       bitSet.set(7,true);
       
       System.out.println("BitSet size: " + bitSet.size() +  " length: " + bitSet.length());
       
       
       //testing comaprsison
       BitSet bitSet1= new BitSet(8);
       BitSet bitSet2= new BitSet(8);
              
       System.out.println("Are bit sets equal (they should be): " + bitSet1.equals(bitSet2));
       
       bitSet2.set(3,true);

       System.out.println("Are bit sets equal (they should not be): " + bitSet1.equals(bitSet2));
       
       bitSet1.set(3,true);
       System.out.println("Are bit sets equal (they should be): " + bitSet1.equals(bitSet2));

       //test get 
       BitSet myBit=bitSet1.get(0,5);
       System.out.println(MHTKernel.bitSetString(myBit, 5));
    }

}