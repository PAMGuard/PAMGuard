## Depracated
### OrcaSpot
[ORCA-SPOT](https://github.com/ChristianBergler/ORCA-SPOT) is a deep learning based algorithm which was initially designed for killer whale sound detection in noise heavy underwater recordings. 

Settings up OrcaSpot to work is not trivial and requires some command line coding. 

You will need to. 

 * Install [Python and Anaconda or similar](https://docs.anaconda.com/anaconda/install/windows/).
 * Install [CUDA installed from Nvidea](https://developer.nvidia.com/cuda-downloads) to run the classifier on a graphics card. 
 * Install [Cuda support for Pytorch](https://pytorch.org).
 * Download the OrcaSpot Python code and classifier (pending publication and licensing). 
 
 To set up the python environment
 * Copy the Orcaspot Segmeneter folder to a location on your computer you ar enot going to change. 
 * Open command prompt or Anaconda prompt if is using Anaconda.
 * Type ```python -m venv C:\Your\Enviroment\Path\Here``` for example ```python -m venv C:\Users\Hauec\Desktop\Segmenter\pytorch\my-venv```. This creates a Folder called my-venv in the PyTorch Folder inside of the Segmenter.
 * Next activate your Virtual environment. Inside of my-venv\Scripts should see a windows batch called activate.bat. cd to it and run it in CMD by typing ```activate.bat```. You'll know that it is active via the (my-venv) precommand in the command windows.
 * Once that is done, run setup_pytorch.bat from the PyTorch folder. It should automatically install Pytorch, PyVision, and all of the required ependencies.


