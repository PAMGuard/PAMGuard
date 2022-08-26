# Matched click classifier

## Overview

The matched click classifier is an alternative to the in built click classifier in the click detection module which uses two click templates (target and reject) to classiify individual clicks detections from the click detection module. The idea behind this classifier is to more accurately classify rarer clicks when there 

## How it works

The classifier is based on a matched filter i.e. a canditate click detection is compared to a template and it's maximum correlation value with the template is then used for classification. Each click is compared to both a _match_ and a _reject_ template. If the ratio of the correlation match to the reject template exceeds a certain threshold then the click is classified. There can be multiple combinations of match to reject templates allowing the matched classifier to deal with with different types of clicks or even clicks from multiple species.


## Configuring the matched click classifier
