# Java_KCP
Java based implementation of the Kinetic Change Point algorithm as an ImageJ plugin.

Copy the jar file Java_KCPv1.0.jar into the plugins folder of imagej to install. The plugin can be accessed under the Menu Plugins->Kinetic Change Point or Plugins->Batch Kinetic Change Point.

The plugin expects a table with x and y data. The region for changpoint analysis must be specified as X start and end. The Dialog provides the following options:
```
    Table - Name of the table that contains the raw data. Typically this is the Results table.
    x column - X column for change point analysis. Typically this is time or frame number.
    y column - Y column for change point analysis. Typically this is position.  
    Trajectory - Number of the trajectory to analyze in the Results table. Multiple trajectories can be in the same table with different numbers in the Trajectory column. This parameter defines the trajectory to analyze within the table. 
    Sigma - Standard deviation of the background noise. Can be determined using the region prior to enzymatic activity.
    Confidence value - Confidence interval (1 - alpha). A value of 0.99 represents a 1% false positive rate.  
    start - Start of the region for change point analysis in X values.              
    stop - End of the region for change point analysis in X values.                          
    TableTitle - Title of the output table. 
    steps - If checked, the slope will be set to zero for all segments. This assumes you have data representing a series of steps and fits them using the algorithm.
```
A sample trajectory is included in the example folder with the expected output when following the instructions below:

1. Open the exampletrajectory.csv file using File->Open. A results table with time, nucleotides, trajectory and position columns.

2. Open the dialog to define the algorithm settings using Plugins->Kinetic Change Point. You will then be presented with numerous options. Use the following settings:
```
    Table            :   Results
    x column         :   time 
    y column         :   nucleotides  
    Trajectory       :   0  
    Sigma            :   200
    Confidence value :   0.99
    start            :   50     
    stop             :   440
    TableTitle       :   Segments
    steps            :   UNCHECKED
```
(an image of the dialog with these settings is included in the example directory)

Click OK and an output table with the specified TableTitle will appear with a list of segments resulting from changepoint analysis. The columns in the output table have the following meaning for the equation for a line (y = A x + B):
```
      (x1, y1)  is the start position of each line segment.
      (x2, y2)  is the end position of each line segment
          A     is the slope of each line segment.
       sigma_A  is the Standard deviation of the slope of each line segment.
          B     is the intercept of each line segment.
       sigma_B  is the Standard deviation of the intercept of each line segment.
```
Version 1.0 has been tested on Fuji (here you have to go inside the package to install) on MacOS High Sierra. It has also been tested with ImageJ on MacOS Sierra and prior mac operating systems. 

The Batch Kinetic Change Point plugin performs the same algorithm but on multiple trajectories at once. Expected input is a table with a trajectory column that provides unique numbers for each trajectory. The batch tool will perform kinetic change point analysis on each trajectory and generate an output segments table in which a trajectory column is also provided with numbering that matches the input table.

Please provide us with feedback so we can continue to improve the plugin. Comments, questions, and suggestions are welcome!
