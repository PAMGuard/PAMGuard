package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsResult;

public class BoundingBoxMerger {

    /**
     * Combines overlapping bounding boxes from a list until no more merges can be performed.
     *
     * @param boxes The initial list of DeepAcousticsResult objects.
     * @param minOverlapThreshold The minimum Intersection over Union (IoU) required to merge two boxes.
     * A value between 0.0 (any overlap) and 1.0. For 5%, use 0.05.
     * @return A new list of merged bounding boxes.
     */
    public static List<DeepAcousticsResult> combineBoxes(List<DeepAcousticsResult> boxes, double minOverlapThreshold) {
        if (boxes == null || boxes.size() < 2) {
            return boxes;
        }

        List<DeepAcousticsResult> mergedBoxes = new ArrayList<>(boxes);
        boolean wasMergePerformedInPass;

        do {
            wasMergePerformedInPass = false;
            List<DeepAcousticsResult> currentPassResult = new ArrayList<>();
            boolean[] consumed = new boolean[mergedBoxes.size()];

            for (int i = 0; i < mergedBoxes.size(); i++) {
                if (consumed[i]) {
                    continue;
                }

                DeepAcousticsResult baseBox = mergedBoxes.get(i);

                for (int j = i + 1; j < mergedBoxes.size(); j++) {
                    if (consumed[j]) {
                        continue;
                    }

                    DeepAcousticsResult otherBox = mergedBoxes.get(j);

                    if (calculateIoU(baseBox, otherBox) >= minOverlapThreshold) {
                        baseBox = merge(baseBox, otherBox);
                        consumed[j] = true; // Mark the other box as consumed
                        wasMergePerformedInPass = true;
                    }
                }
                currentPassResult.add(baseBox);
            }

            if (wasMergePerformedInPass) {
                mergedBoxes = currentPassResult;
            }

        } while (wasMergePerformedInPass); // Repeat until a pass completes with no merges

        return mergedBoxes;
    }

    /**
     * Calculates the Intersection over Union (IoU) of two bounding boxes.
     */
    private static double calculateIoU(DeepAcousticsResult box1, DeepAcousticsResult box2) {
        // Calculate intersection coordinates
        double interX = Math.max(box1.getX(), box2.getX());
        double interY = Math.max(box1.getY(), box2.getY());
        double interMaxX = Math.min(box1.getX() + box1.getWidth(), box2.getX() + box2.getWidth());
        double interMaxY = Math.min(box1.getY() + box1.getHeight(), box2.getY() + box2.getHeight());

        // Calculate intersection area
        double interWidth = Math.max(0, interMaxX - interX);
        double interHeight = Math.max(0, interMaxY - interY);
        double intersectionArea = interWidth * interHeight;

        if (intersectionArea == 0) {
            return 0.0;
        }

        // Calculate union area
        double unionArea = box1.getHeight()*box1.getWidth() + box2.getHeight()*box2.getWidth() - intersectionArea;

        return intersectionArea / unionArea;
    }

    /**
     * Merges two bounding boxes into a single one that encompasses both.
     */
    private static DeepAcousticsResult merge(DeepAcousticsResult box1, DeepAcousticsResult box2) {
        double minX = Math.min(box1.getX(), box2.getX());
        double minY = Math.min(box1.getY(), box2.getY());
        double maxX = Math.max(box1.getX() + box1.getWidth(), box2.getX() + box2.getWidth());
        double maxY = Math.max(box1.getY() + box1.getHeight(), box2.getY() + box2.getHeight());
        
        double[] boundingBox = new double[] {minX, minY, maxX - minX, maxY - minY};

        //now we select the maximum confidence score from the boxes. 
        float maxConfidence = Math.max(box1.getConfidence(), box2.getConfidence());
       	float[] classScores = box1.getPredicitions();
       	
       	//create the new result. 
       	return new DeepAcousticsResult(boundingBox, maxConfidence, classScores);
    }
}