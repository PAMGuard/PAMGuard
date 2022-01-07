#!/bin/bash

function bad {
  echo "******************************"
  echo "A regression failed, see above"
  exit 1
}


java -ea -cp ../ likelihoodDetectionModule/normalizer/UnitTest
if [ $? != 0 ]; then bad; fi

java -ea -cp ../ likelihoodDetectionModule/UnitTest
if [ $? != 0 ]; then bad; fi

java -ea -cp ../ likelihoodDetectionModule/linearAverageSpectra/UnitTest
if [ $? != 0 ]; then bad; fi

java -ea -cp ../ likelihoodDetectionModule/spectralEti/UnitTest
if [ $? != 0 ]; then bad; fi

java -ea -cp ../ likelihoodDetectionModule/thresholdDetector/UnitTest
if [ $? != 0 ]; then bad; fi

echo "**************************"
echo "All regressions passed"


