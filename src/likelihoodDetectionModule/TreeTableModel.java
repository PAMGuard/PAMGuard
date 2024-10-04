
package likelihoodDetectionModule;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import likelihoodDetectionModule.normalizer.NormalizerProcess.NormalizerAlgorithm;

/**
 * The TreeTableModel represents a LikelihoodDetectionParameters object
 * (which contains module parameters and an ArrayList of TargetConfiguration objects)
 * as a node-based tree for use with a JTree. This model is intended to be
 * placed within a JTable to represent a tree within the first column of a
 * table (specifically, the first column of the TreeTable). It implements
 * the standard Java TreeModel interface.
 * 
 * The JTree that this model is used for is the TreeTableCellRenderer that
 * is embedded within the TreeTable.
 * 
 * @see LikelihoodDetectionParameters
 * @see TreeTable.TreeTableCellRenderer
 * @see javax.swing.tree.TreeModel
 * 
 */

public class TreeTableModel implements TreeModel {
	
    /** A list of listeners for various events that the model may fire. Typically these
     * are model change events. 
     */
    private EventListenerList listenerList = new EventListenerList();
    
    /** The root (top-level node) of the internal representation of an ArrayList of TargetConfiguration objects.
     *  The TreeTable is configured to hide this node when the tree is displayed.
     */
    private ConfigurationNode rootNode = new ConfigurationNode();
    
    /** The acquisition settings (audio source parameters) that were used to
     * create the LikelihoodDetectionParameters.
     */
    private AcquisitionSettings acquisitionSettings;
    
    /** The channel map selected. */
    private int channelMap;
    
    /** The target configurations. */
    private ArrayList<TargetConfiguration> targetConfigurations;
    
    /** The signal band names. These are gathered to help the configuration dialog. */
    Vector<String> signalBandNames = new Vector<String>();
    
    /** The guard band names. These are gathered to help the configuraiton dialog. */
    Vector<String> guardBandNames = new Vector<String>();
    
    /** The configuration dialog settings.
     * @see ConfigurationDialogSettings
     */
    private ConfigurationDialogSettings dialogSettings = new ConfigurationDialogSettings();
    
    /**
     * Instantiates a new tree table model. This constructor only takes in the 
     * acquisition settings and the channel map because those are static with respect
     * to configuration while the dialog is open. The actual likelihood detection parameters
     * that the dialog is to represent will be set via the setTargetConfigurations() method.
     * 
     * @param acquisitionSettings the acquisition settings
     * @param channelMap the channel map
     */
    public TreeTableModel( AcquisitionSettings acquisitionSettings, int channelMap ) {
    	this.acquisitionSettings = acquisitionSettings;
    	this.channelMap = channelMap;
    }
    
    /**
     * Gets the acquisition settings that are associated with the parameters.
     * 
     * @return the acquisition settings
     */
    public AcquisitionSettings getAcquisitionSettings() {
    	return this.acquisitionSettings;	
    }
    
    public void setAcquisitionSettings( AcquisitionSettings acquisitionSettings ) {
    	this.acquisitionSettings = acquisitionSettings;
    }
    /**
     * Gets the channel map that the user has selected.
     * 
     * @return the channel map
     */
    public int getChannelMap() {
    	return this.channelMap;	
    }
    
    /**
     * Returns an ArrayList of the target configuration names.
     * 
     * @return the array list< string>
     */
    public ArrayList<String> targetConfigurationNames() {
  		ArrayList<String> names = new ArrayList<String>();
  		
  		for ( int i = 0; i < rootNode.children.size(); i++ ) {
  			names.add( ((TargetNode)rootNode.children.get(i)).getName() );
  		}
    	
  		return names;
    }
    
  	/**
	   * Creates a new target configuration using the identifier specified. This
	   * identifier should be unique to the module. Note that this object does
	   * not enforce the uniqueness.
	   * 
	   * @param identifier the identifier string
	   */
	  public void createNewTargetConfiguration( String identifier ) {
  		TargetConfiguration config = new TargetConfiguration( acquisitionSettings, channelMap );
  		config.setIdentifier(identifier);
  		ArrayList<TargetConfiguration> configurations = this.getTargetConfigurations();
  		configurations.add( config );
  		setTargetConfigurations( configurations );
  	}
    
  	/**
	   * Delete the target configuration specified by the identifier. If the configuration
	   * is not found, this method is a no-op.
	   * 
	   * @param identifier the identifier
	   */
	  public void deleteTargetConfiguration( String identifier ) {
  		ArrayList<TargetConfiguration> configurations = this.getTargetConfigurations();
  		
  		int index = -1;
  		for ( int i = 0; i < configurations.size(); i++ ) {
  			if ( identifier == configurations.get(i).getIdentifier() ) {
  				index = i;	
  			}
  		}
  		
  		if ( index != -1 ) {
  			configurations.remove(index);
  		}
  		
  		setTargetConfigurations( configurations );
  	}
  	
  	/**
	   * Gets the target configuration identified by the string. This will return
	   * null if the configuration is not found.
	   * 
	   * @param identifier the identifier
	   * 
	   * @return the target configuration
	   */
	  public TargetConfiguration getTargetConfiguration( String identifier ) {
  		for ( int i = 0; i < targetConfigurations.size(); ++i ) {
  			if ( targetConfigurations.get(i).getIdentifier().equals( identifier )) {
  				return targetConfigurations.get(i);	
  			}
  		}
  		
  		return null;
  	}
  	
  	/**
	   * Adds the target configuration.
	   * 
	   * @param config the config
	   */
	  public void addTargetConfiguration( TargetConfiguration config ) {
  		ArrayList<TargetConfiguration> configurations = this.getTargetConfigurations();
  		configurations.add( config );
  		setTargetConfigurations( configurations );
  	}
  	
  	/**
	   * Gets the band names used.
	   * 
	   * @return the band names
	   */
	  public ArrayList<String> getBandNames() {
  		ArrayList<String> names = new ArrayList<String>();
  		
  		for ( int i = 0; i < targetConfigurations.size(); ++i ) {
  			ArrayList<SignalBand> signalBands = targetConfigurations.get(i).getSignalBands();
  			for ( int j = 0; j < signalBands.size(); j++ ) {
  				names.add( signalBands.get(j).identifier );	
  			}
  			
  			ArrayList<GuardBand> guardBands = targetConfigurations.get(i).getGuardBands();
  			for ( int k = 0; k < guardBands.size(); k++ ) {
  				names.add( guardBands.get(k).identifier );	
  			}
  		}
  		
  		return names;
  	}
  	
  	/**
	   * Creates a new signal band for the target configuration specified. The signal band
	   * name should be unique for this target configuration. The object does not enforce
	   * the uniqueness.
	   * 
	   * @param bandId The band's unique identifier.
	   * @param configId The target configuration's unique identifier.
	   */
	  public void createNewSignalBand( String bandId, String configId ) {
  		ArrayList<TargetConfiguration> configurations = this.getTargetConfigurations();
  		
  		for ( int i =0; i < configurations.size(); ++i ) {
  			if ( configurations.get(i).getIdentifier() == configId ) {
  				SignalBand band = new SignalBand( this.acquisitionSettings, configurations.get(i).getFFTParameters() );
  				band.identifier = bandId;
  				ArrayList<SignalBand> bands = configurations.get(i).getSignalBands();
  				bands.add( band );
  				configurations.get(i).setSignalBands(bands);
  				break;
  			}
  		}
  		
  		setTargetConfigurations( configurations );
  	}
  	
 	/**
	  * Creates a new guard band for the target configuration specified. The guard band name
	  * should be unique for this target configuration. The object does not enforce the
	  * uniqueness.
	  * 
	  * @param bandId The guard band's unique identifier.
	  * @param configId The target configuration's unique identifier.
	  */
	 public void createNewGuardBand( String bandId, String configId ) {
 		ArrayList<TargetConfiguration> configurations = this.getTargetConfigurations();
 		
  		for ( int i =0; i < configurations.size(); ++i ) {
  			if ( configurations.get(i).getIdentifier() == configId ) {
  				GuardBand band = new GuardBand( this.acquisitionSettings, configurations.get(i).getFFTParameters() );
  				band.identifier = bandId;
  				ArrayList<GuardBand> bands = configurations.get(i).getGuardBands();
  				bands.add( band );
  				ArrayList<SignalBand> signalBands = configurations.get(i).getSignalBands();
  				if ( signalBands.size() != 0 ) {
  					band.associatedSignalBandIdentifier = signalBands.get(0).identifier;
  				}
  				else {
  					band.associatedSignalBandIdentifier = "None";	
  				}
  				configurations.get(i).setGuardBands(bands);
  				break;
  			}
  		}
  		
  		setTargetConfigurations( configurations );
  	}
 	
 	/**
	  * Delete the specified guard band from the target configuration indicated. If the
	  * target configuration or the guard band is not found, this function does nothing.
	  * 
	  * @param bandName The guard band's unique identifier.
	  * @param configName The target configuration's unique identifier.
	  */
	 public void deleteGuardBandFrom( String bandName, String configName ) {
 		ArrayList<TargetConfiguration> configurations = this.getTargetConfigurations();
 		
 		for ( int i = 0; i < configurations.size(); ++i ) {
 			if ( configurations.get(i).getIdentifier().equals( configName ) ) {
 				ArrayList<GuardBand> guards = configurations.get(i).getGuardBands();
 				for ( int j = 0; j < guards.size(); ++j ) {
 					if ( guards.get(j).identifier.equals( bandName ) ) {
 						guards.remove(j);
 						break;
 					}
 				}
 			}
 		}
 		
 		this.setTargetConfigurations( configurations );
 	}
 	
 	/**
	  * Delete the specified signal band from the indicated target configuration. If the
	  * target configuration or the signal band are not found, this function does nothing.
	  * 
	  * @param bandName The signal band's unique identifier.
	  * @param configName The target configuration's unique identifier.
	  */
	 public void deleteSignalBandFrom( String bandName, String configName ) {
 		ArrayList<TargetConfiguration> configurations = this.getTargetConfigurations();
 		
 		for ( int i = 0; i < configurations.size(); ++i ) {
 			if ( configurations.get(i).getIdentifier().equals( configName ) ) {
 				ArrayList<SignalBand> signals = configurations.get(i).getSignalBands();
 				for ( int j = 0; j < signals.size(); ++j ) {
 					if ( signals.get(j).identifier.equals( bandName ) ) {
 						String name = signals.get(j).identifier;
 						signals.remove(j);
 						
 						ArrayList<GuardBand> guards = configurations.get(i).getGuardBands();
 						for ( GuardBand g : guards ) {
 							if ( g.associatedSignalBandIdentifier.equals( name ) ) {
 								g.associatedSignalBandIdentifier = "None";	
 							}
 						}
 						
 						break;
 					}
 				}
 			}
 		}
 		
 		setTargetConfigurations( configurations );
 	}
 	
    /**
     * Sets the TargetConfigurations that are being managed by the model.
     * 
     * @param configs An ArrayList of TargetConfigurations.
     */
    public void setTargetConfigurations( ArrayList<TargetConfiguration> configs ) {   	
    	
    	// Store them.
    	targetConfigurations = configs;
        
    	// Clear the internal representation.
    	rootNode.children.clear();
    	
    	// Loop over the array and parse the target configurations out into
        // their internal representations.
        Iterator<TargetConfiguration> i = configs.iterator();
        while ( i.hasNext() ) {
          TargetConfiguration config = i.next();
          parseTargetConfiguration( config, rootNode );
        }
        
        Object[] path = new Object[1];
        path[0] = rootNode;
        
        // Inform listeners (e.g., the TreeTable view) that the model has changed.
        fireTreeStructureChanged( this, path, null, null );
    }
    
    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     * 
     * @param aNode the TreeNode to get the path for
     * 
     * @return the path to root
     */
    public TreeNode[] getPathToRoot(TreeNode aNode) {
        return getPathToRoot(aNode, 0);
    }

   
    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     * 
     * @param aNode  the TreeNode to get the path for
     * @param depth  an int giving the number of steps already taken towards
     * the root (on recursive calls), used to size the returned array
     * 
     * @return an array of TreeNodes giving the path from the root to the
     * specified node
     */
    protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[]              retNodes;
        // This method recurses, traversing towards the root in order
        // size the array. On the way back, it fills in the nodes,
        // starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if(aNode == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new TreeNode[depth];
        }
        else {
            depth++;
            if(aNode == rootNode)
                retNodes = new TreeNode[depth];
            else
                retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    /**
     * Sets the configuration dialog settings that are to be preserved with the
     * LikelihoodDetectionParameters.
     * 
     * @param settings The new configuration dialog settings.
     */
    public void setConfigurationDialogSettings( ConfigurationDialogSettings settings ) {
    	this.dialogSettings = settings;	
    }
    
    /**
     * Gets the dialog settings.
     * 
     * @return the dialog settings
     */
    public ConfigurationDialogSettings getDialogSettings() {
    	return this.dialogSettings;	
    }
    
    /**
     * Sets the configuration dialog expanded state.
     * 
     * @param state the new configuration dialog expanded state
     */
    public void setConfigurationDialogExpandedState( String state ) {
    	this.dialogSettings.expandedState = state;	
    }
    
    /**
     * Returns the current state of the internal representation of the
     * configuration data as an ArrayList of TargetConfiguration objects.
     * 
     * @return the target configurations
     */
    
    public ArrayList<TargetConfiguration> getTargetConfigurations() {
      ArrayList<TargetConfiguration> configs = new ArrayList<TargetConfiguration>();  
      
      // Loop over the internal representation of the data model and compile a bunch
      // of target configurations from it. This basically recurses down the node tree and 
      // builds TargetConfiguration objects as it goes along, using run-time type detection
      // to figure out what object to build and when.
      
      for ( int i = 0; i < this.rootNode.children.size(); ++i ) {
    	TargetNode targetNode = (TargetNode)this.rootNode.children.get(i);
    	TargetConfiguration config = new TargetConfiguration( this.acquisitionSettings, this.channelMap );
    	
    	config.setIdentifier( targetNode.getName() );
    	config.setState( targetNode.getState() );
    	
    	for ( int j = 0; j < targetNode.children.size(); ++j ) {
    		if ( targetNode.children.get(j).getClass() == TimeResolutionNode.class ) {
    			config.setTimeResolution( ((TimeResolutionNode)targetNode.children.get(j)).getResolution() );
    		}
    		else if ( targetNode.children.get(j).getClass() == FrequencyResolutionNode.class ) {
    			config.setFrequencyResolution( ((FrequencyResolutionNode)targetNode.children.get(j)).getResolution() );
    		}
    		else if ( targetNode.children.get(j).getClass() == AlgorithmNode.class ) {
    			String algorithmName = ((AlgorithmNode)targetNode.children.get(j)).algorithmAsString();
    			if ( algorithmName == "Decaying Average" ) {
    				config.setAlgorithm( NormalizerAlgorithm.DecayingAverage );
    			}
    			else if ( algorithmName == "Block Average" ) {
    				config.setAlgorithm( NormalizerAlgorithm.SplitWindow );		
    			}
    		}
    		else if ( targetNode.children.get(j).getClass() == SecondsBetweenDetectionsNode.class ) {
    		  	config.setSecondsBetweenDetections( ((SecondsBetweenDetectionsNode)targetNode.children.get(j)).getSeconds() );	
    		}
    		else if ( targetNode.children.get(j).getClass() == DetectionBandsNode.class ) {
    			DetectionBandsNode bandsNode = (DetectionBandsNode)targetNode.children.get(j);
    			ArrayList<SignalBand> signalBands = new ArrayList<SignalBand>();
    			ArrayList<GuardBand> guardBands = new ArrayList<GuardBand>();
    			
    			for ( int k = 0; k < bandsNode.children.size(); ++k ) {
    				if ( bandsNode.children.get(k).getClass() == SignalBandNode.class ) {
    					SignalBand signalBand = new SignalBand( this.acquisitionSettings, config.getFFTParameters() );
    					SignalBandNode signalNode = (SignalBandNode)bandsNode.children.get(k);
    					signalBand.identifier = signalNode.getName();
    					
    					for ( int l = 0; l < signalNode.children.size(); ++l ) {
    						if ( signalNode.children.get(l).getClass() == SnrThresholdNode.class ) {
    							signalBand.inBandThresholdDb = ((SnrThresholdNode)signalNode.children.get(l)).getThreshold();
    						}
    						else if ( signalNode.children.get(l).getClass() == GuardBandThresholdNode.class ) {
    							signalBand.guardBandThresholdDb = ((GuardBandThresholdNode)signalNode.children.get(l)).getSeconds();
    						}
    						else if ( signalNode.children.get(l).getClass() == StartFrequencyNode.class ) {
    							signalBand.startFrequencyHz = ((StartFrequencyNode)signalNode.children.get(l)).getFrequency();
    							signalBand.frequencyLimits = ((StartFrequencyNode)signalNode.children.get(l)).getLimits();
    						}
    						else if ( signalNode.children.get(l).getClass() == EndFrequencyNode.class ) {
    							signalBand.endFrequencyHz = ((EndFrequencyNode)signalNode.children.get(l)).getFrequency();
    						}
    						else if ( signalNode.children.get(l).getClass() == BackgroundNode.class ) {
    							signalBand.backgroundSeconds = ((BackgroundNode)signalNode.children.get(l)).getBackgroundNoise();
    							signalBand.secondsLimits = ((BackgroundNode)signalNode.children.get(l)).getLimits();
    						}
    						else if ( signalNode.children.get(l).getClass() == SignalNode.class ) {
    							signalBand.signalSeconds = ((SignalNode)signalNode.children.get(l)).getSignalSecods();
    						}
    					}
    					
    					signalBands.add( signalBand );
    					
    				}
    				else if ( bandsNode.children.get(k).getClass() == GuardBandNode.class ) {
      					GuardBand guardBand = new GuardBand( this.acquisitionSettings, config.getFFTParameters() );
    					GuardBandNode guardNode = (GuardBandNode)bandsNode.children.get(k);
    					guardBand.identifier = guardNode.getName();
    					
    					for ( int l = 0; l < guardNode.children.size(); ++l ) {
    						if ( guardNode.children.get(l).getClass() == AssociatedBandNode.class ) {
    							guardBand.associatedSignalBandIdentifier = ((AssociatedBandNode)guardNode.children.get(l)).getBandName();	
    						}
    						else if ( guardNode.children.get(l).getClass() == StartFrequencyNode.class ) {
    							guardBand.startFrequencyHz = ((StartFrequencyNode)guardNode.children.get(l)).getFrequency();
    							guardBand.frequencyLimits = ((StartFrequencyNode)guardNode.children.get(l)).getLimits();
    						}
    						else if ( guardNode.children.get(l).getClass() == EndFrequencyNode.class ) {
    							guardBand.endFrequencyHz = ((EndFrequencyNode)guardNode.children.get(l)).getFrequency();
    						}
    						else if ( guardNode.children.get(l).getClass() == BackgroundNode.class ) {
    							guardBand.backgroundSeconds = ((BackgroundNode)guardNode.children.get(l)).getBackgroundNoise();
    							guardBand.secondsLimits = ((BackgroundNode)guardNode.children.get(l)).getLimits();
    						}
    						else if ( guardNode.children.get(l).getClass() == SignalNode.class ) {
    							guardBand.signalSeconds = ((SignalNode)guardNode.children.get(l)).getSignalSecods();
    						}
    					}
    					
    					guardBands.add( guardBand );
    				}
    			}
    			
    			config.setSignalBands( signalBands );
    			config.setGuardBands( guardBands );
    		}
    	}
    	
    	configs.add( config );
      }
      
      return configs;
    }
    
    /**
     * Parses a single TargetConfiguration object into an internal representation, and adds it
     * as a child of the root configuaration node.
     * 
     * @param config The target configuration to be parsed.
     * @param rootNode The root node into which to store the new tree of nodes.
     */
    
    void parseTargetConfiguration( TargetConfiguration config, ConfigurationNode rootNode ) {
    	// If the config doesn't exist, do nothing.
      	if ( config == null ) return;
      
      	// Create a new target node for the tree.
      	TargetNode targetNode = new TargetNode( config.getIdentifier() );
      	targetNode.setState( config.getState() );
      
      	// Add the new target node to the root node's list of children.
      	rootNode.children.add( targetNode );
 
      	// Create a frequency resolution node using the config's parameters.
      	FrequencyResolutionNode freqResolutionNode = new FrequencyResolutionNode( config.getFrequencyResolution(), config.getFrequencyResolutionLimits() );
      	
      	// Create a time resolution node using the config's parameters.
      	TimeResolutionNode timeResolutionNode = new TimeResolutionNode( targetNode, config.getTimeResolution(), config.getTimeResolutionLimits(), config.getAcquisitionSettings(), config.getChannelMap(), freqResolutionNode );
      	
      	// Create an normalization algorithm node using the config's parameters.
      	AlgorithmNode algorithmNode = new AlgorithmNode( TargetConfiguration.algorithmToString( config.getAlgorithm() ) );
      	
      	// Create a node for the seconds between detections using the config's parameters.
      	SecondsBetweenDetectionsNode betweenNode = new SecondsBetweenDetectionsNode( config.getSecondsBetweenDetections(), config.getSecondsBetweenDetectionsLimits() );
      	
      	// Add those node to the new target node.
      	targetNode.children.add( timeResolutionNode );
      	targetNode.children.add( freqResolutionNode );
      	targetNode.children.add( algorithmNode );
      	targetNode.children.add( betweenNode );
      	
      	// Create a new node to hold the detection bands.
      	DetectionBandsNode bandsNode = new DetectionBandsNode( config.getIdentifier() );
      	
      	// Add the detection bands node to the target node.
      	targetNode.children.add( bandsNode );
      
      	// Calculate the window parameters' warn limit. If the normalizer being used is
      	// split window, then the windowing parameters can't exceed the specified value.
      	double windowWarnLimit = 1000000.0; // some huge value beyond the limits of the widget
      	if ( config.getAlgorithm() == NormalizerAlgorithm.SplitWindow ) {
        	windowWarnLimit = config.getTimeResolution() * 500;
      	}
      
      	// Add the signal bands from the target configuration to the detections bands node for the target.
      	parseSignalBands( config.getSignalBands(), bandsNode, windowWarnLimit );
      	
      	// Add the guard bands from the target configuration to the detection bands node for the target.
      	parseGuardBands( config.getGuardBands(), bandsNode, windowWarnLimit );
    }
    
    /**
     * Parses out an array list of signal band objects into their internal representation
     * and adds it to the detection bands node that is also passed in.
     * 
     * @param bands the bands
     * @param parent the parent
     * @param windowWarnLimit the window warn limit
     */
    
    void parseSignalBands( ArrayList<SignalBand> bands, DetectionBandsNode parent, double windowWarnLimit ) {
      Iterator<SignalBand> signalBands = bands.iterator();
      
      // While there are bands to add, create the appropriate nodes from the configuarion paramters and
      // add them to the signal band node.
      
      while ( signalBands.hasNext() ) {
        SignalBand signalBand = signalBands.next();
        SnrThresholdNode snrNode = new SnrThresholdNode( signalBand.inBandThresholdDb, signalBand.inBandThresholdLimits );
        GuardBandThresholdNode noiseNode = new GuardBandThresholdNode( signalBand.guardBandThresholdDb, signalBand.guardBandThresholdLimits );
        StartFrequencyNode startFreqNode = new StartFrequencyNode( signalBand.startFrequencyHz, signalBand.frequencyLimits[0] );
        EndFrequencyNode endFreqNode = new EndFrequencyNode( signalBand.endFrequencyHz, signalBand.frequencyLimits[1], startFreqNode );
        startFreqNode.setEndFrequencyNode( endFreqNode );
        SignalNode temp = new SignalNode();
        BackgroundNode backNode = new BackgroundNode( signalBand.backgroundSeconds, signalBand.secondsLimits[1], temp, windowWarnLimit );
        SignalNode signode = new SignalNode( signalBand.signalSeconds, signalBand.secondsLimits[0], backNode, windowWarnLimit );
		backNode.setSignalNode( signode );
      
		// Create a new signal band node.
        SignalBandNode signalBandNode = new SignalBandNode( signalBand.identifier, parent.getConfigName() );
   
        // Add the signal band node to the parent, which is a detections band node.
        parent.children.add( signalBandNode );
        
        // Add the parameter nodes as children to the signal band node.
        signalBandNode.children.add( snrNode );
        signalBandNode.children.add( noiseNode );
        signalBandNode.children.add( startFreqNode );
        signalBandNode.children.add( endFreqNode );
        signalBandNode.children.add( backNode );
        signalBandNode.children.add( signode );
        
        // Add this signal band node name to the list of identifiers in use.
        signalBandNames.add( signalBand.identifier );
      }    
    }
    
    /**
     * Parses out an array list of guard band objects into their internal representation
     * and adds it to the detection bands node that is also passed in.
     * 
     * @param bands the bands
     * @param parent the parent
     * @param windowWarnLimit the window warn limit
     */
    
    void parseGuardBands( ArrayList<GuardBand> bands, DetectionBandsNode parent, double windowWarnLimit ) {
      Iterator<GuardBand> guardBands = bands.iterator();
      
      // While there are bands to add, create the appropriate nodes from the configuarion paramters and
      // add them to the signal band node.
      
      while ( guardBands.hasNext() ) {
        GuardBand guardBand = guardBands.next(); 
        AssociatedBandNode assNode = new AssociatedBandNode( guardBand.associatedSignalBandIdentifier );
        StartFrequencyNode startFreqNode = new StartFrequencyNode( guardBand.startFrequencyHz, guardBand.frequencyLimits[0] );
        EndFrequencyNode endFreqNode = new EndFrequencyNode( guardBand.endFrequencyHz, guardBand.frequencyLimits[1], startFreqNode );
        startFreqNode.setEndFrequencyNode( endFreqNode );
        SignalNode temp = new SignalNode();
        BackgroundNode backNode = new BackgroundNode( guardBand.backgroundSeconds, guardBand.secondsLimits[1], temp, windowWarnLimit );
        SignalNode signode = new SignalNode( guardBand.signalSeconds, guardBand.secondsLimits[0], backNode, windowWarnLimit );
        backNode.setSignalNode( signode );
        
        // Create a new guard band node and add it to the parent detection band node.
        GuardBandNode guardBandNode = new GuardBandNode( guardBand.identifier, parent.getConfigName() );
        parent.children.add( guardBandNode );
        
        // Add the parameter nodes to the guard band node.
        guardBandNode.children.add( assNode );
        guardBandNode.children.add( startFreqNode );
        guardBandNode.children.add( endFreqNode );
        guardBandNode.children.add( backNode );
        guardBandNode.children.add( signode );
        
        // Add the guard band identifier to the list.
        guardBandNames.add( guardBand.identifier );
      }
    }
    
    /**
     * Gets the signal band names in use for the specified target configuration identifier. If the
     * identifier is not found, the resulting vector will be empty.
     * 
     * @param configIdentifier The target configuration's unique identifier.
     * 
     * @return the signal band names
     */
    public Vector<String> getSignalBandNames( String configIdentifier ) {
      
    	Vector<String> names = new Vector<String>();
    	
    	DetectionBandsNode bands = null;
    	
    	// Traverse down the internal representation tree and find the target configuration
    	// specified, and then find the detection bands node for that config.
    	for ( Node n : rootNode.children ) {
    		if ( n.getClass() == TargetNode.class ) {
    			TargetNode tn = (TargetNode)n;
    			if ( tn.getName().equals( configIdentifier ) ) {
    				for ( Node tns : tn.children ) {
    					if ( tns.getClass() == DetectionBandsNode.class ) {
    						// Oh, here are the coffee mugs.
    						bands = (DetectionBandsNode)tns;
    						break;
    					}
    				}
    			}
    		}
    	}
    	
    	// Didn't find anything.
    	if ( bands == null ) return names;
    	
    	// For all of the signal band nodes for the signal bands there, get the name.
    	for ( Node n : bands.children ) {
    		if ( n.getClass() == SignalBandNode.class ) {
    			names.add( ((SignalBandNode)n).getName() );	
    		}
    	}
    	
    	return names;
    }
    
    /**
     * Gets the target configuration name for associated band node. This is used when
     * trying to figure out what signal band names are available to be associated with.
     * If a match is not found, null is returned.
     * 
     * @param n The associated band node in the internal representation.
     * 
     * @return the target configuration name that contains this associated band node
     */
    public String getTargetConfigurationNameForAssociatedBandNode( AssociatedBandNode n ) {
    	
    	for ( Node targetNode : rootNode.children ) {
    		for ( Node parameterNode : targetNode.children ) {
    			if ( parameterNode.getClass() == DetectionBandsNode.class ) {
    				DetectionBandsNode bandsNode = (DetectionBandsNode)parameterNode;
    				for ( Node bandNode : bandsNode.children ) {
    					for ( Node bandParamNode : bandNode.children ) {
    					  if ( bandParamNode == n ) {
    						  TargetNode tn = (TargetNode)targetNode;
    						  return tn.getName();
    					  }
    					}
    				}
    			}
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Implementation of getChildCount from the TreeModel interface.
     * 
     * @param node the node
     * 
     * @return the child count
     * 
     * @see TreeModel
     */
    
    @Override
	public int getChildCount( Object node ) {
      int retval = ((Node)node).children.size();
      return retval;
    }
    
    /**
     * Implementation of getChild from the TreeModel interface.
     * 
     * @param node the node
     * @param child the child
     * 
     * @return the child
     * 
     * @see TreeModel
     */
    
    @Override
	public Object getChild( Object node, int child ) {
      return ((Node)node).children.get( child );
    }
    
    /**
     * Implementation of getRoot from the TreeModel interface.
     * 
     * @return the root
     * 
     * @see TreeModel
     */
    
    @Override
	public Object getRoot() {
        return rootNode;
    }

    /**
     * Implementation of isLeaf from the TreeModel interface.
     * 
     * @param node the node
     * 
     * @return true, if checks if is leaf
     * 
     * @see TreeModel
     */
    
    @Override
	public boolean isLeaf(Object node) {
        //return getChildCount(node) == 0;
      boolean val = getChildCount(node) == 0;
      return val;
    }

    /**
     * Empty implementation of valueForPathChanged from the TreeModel
     * interface.
     * 
     * @param path the path
     * @param newValue the new value
     * 
     * @see TreeModel
     */
    
    @Override
	public void valueForPathChanged(TreePath path, Object newValue) {}
 
    /**
     * Implementation of TreeModel.getIndexOfChild()
     * 
     * @param parent the parent
     * @param child the child
     * 
     * @return the index of child
     * 
     * @see TreeModel
     */
    
    @Override
	public int getIndexOfChild(Object parent, Object child) {
        for (int i = 0; i < getChildCount(parent); i++) {
	         if (getChild(parent, i).equals(child)) { 
	           return i; 
	         }
        }
	       
        return -1; 
    }
    
    /**
     * Adds a listener for TreeModel events.
     * 
     * @param l the l
     */
    
    @Override
	public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener for TreeModel events.
     * 
     * @param l the l
     */
    
    @Override
	public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Implements fireTreeNodesChanged from the TreeModel interface.
     * This notifies all registered listeners that there is a
     * TreeModelEvent when a node changes.
     * 
     * @param source the source
     * @param path the path
     * @param childIndices the child indices
     * @param children the children
     * 
     * @see TreeModel
     */
    
    protected void fireTreeNodesChanged(Object source, Object[] path, 
                                        int[] childIndices, 
                                        Object[] children) {
       
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
      
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, 
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }          
        }
    }

    /**
     * Implements the fireTreeNodesInserted method from the TreeModel
     * interface. This notifies all registered listeners that a new
     * node has been inserted into the tree.
     * 
     * @param source the source
     * @param path the path
     * @param childIndices the child indices
     * @param children the children
     * 
     * @see TreeModel
     */
    
    protected void fireTreeNodesInserted(Object source, Object[] path, 
                                        int[] childIndices, 
                                        Object[] children) {
        
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
      
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
              
                if (e == null)
                    e = new TreeModelEvent(source, path, 
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
            }          
        }
    }

    /**
     * Implements the fireTreeNodesRemoved method from the TreeModel
     * interface. This notifies all registered listeners that a
     * node has been removed from the tree.
     * 
     * @param source the source
     * @param path the path
     * @param childIndices the child indices
     * @param children the children
     * 
     * @see TreeModel
     */
    
    protected void fireTreeNodesRemoved(Object source, Object[] path, 
                                        int[] childIndices, 
                                        Object[] children) {

        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
 
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
  
                if (e == null)
                    e = new TreeModelEvent(source, path, 
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
            }          
        }
    }

    /**
     * Implements the fireTreeStructureChanged method from the TreeModel
     * interface. This notifies all registered listeners that a
     * node has moved position in the tree.
     * 
     * @param source the source
     * @param path the path
     * @param childIndices the child indices
     * @param children the children
     * 
     * @see TreeModel
     */
    
    protected void fireTreeStructureChanged(Object source, Object[] path, 
                                        int[] childIndices, 
                                        Object[] children) {
        
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
       
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {

                if (e == null)
                    e = new TreeModelEvent(source, path, 
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }          
        }
    }

    /**
     * Implementation of TreeModel.getColumnClass.
     * 
     * @param column the column
     * 
     * @return the column class
     * 
     * @see TreeModel
     */
    
    @SuppressWarnings("unchecked")
    public Class getColumnClass(int column) { 
      if ( column == 0 ) return TreeTableModel.class;
      return Object.class; 
    }

    /**
     * Implementation of TreeModel.isCellEditable.
     * 
     * @param node the node
     * @param column the column
     * 
     * @return true, if checks if is cell editable
     * 
     * @see TreeModel
     */
    
    public boolean isCellEditable(Object node, int column) { 
         // For all intents and purposes, every cell in the model is editable.
         return true;
    }
    
    /**
     * Implementation of TreeModel.setValueAt(). This changes the value
     * of a node.
     * 
     * @param aValue the a value
     * @param node the node
     * @param column the column
     * 
     * @see TreeModel
     */
    
    public void setValueAt(Object aValue, Object node, int column) {
      
      if ( node.getClass() == AlgorithmNode.class ) {
        ((AlgorithmNode)node).setAlgorithm( (String)aValue ); 
      }
      else if ( node.getClass() == FrequencyResolutionNode.class ) {
        ((FrequencyResolutionNode)node).setResolution( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == TimeResolutionNode.class ) {
        ((TimeResolutionNode)node).setResolution( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == GuardBandThresholdNode.class ) {
        ((GuardBandThresholdNode)node).setSeconds( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == SecondsBetweenDetectionsNode.class ) {
    	((SecondsBetweenDetectionsNode)node).setValue( Double.valueOf( (String)aValue ).doubleValue() );
      }
      else if ( node.getClass() == SnrThresholdNode.class ) {
        ((SnrThresholdNode)node).setThreshold( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == StartFrequencyNode.class ) {
        ((StartFrequencyNode)node).setFrequency( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == EndFrequencyNode.class ) {
        ((EndFrequencyNode)node).setFrequency( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == BackgroundNode.class ) {
        ((BackgroundNode)node).setBackgroundNoise( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == SignalNode.class ) {
        ((SignalNode)node).setSignalSeconds( Double.valueOf( (String)aValue ).doubleValue() ); 
      }
      else if ( node.getClass() == AssociatedBandNode.class ) {
        ((AssociatedBandNode)node).setBandName( (String)aValue ); 
      }
      
      //ArrayList<TargetConfiguration> configs = this.getTargetConfigurations();
      
    }

    /**
     * Implementation of TreeModel.getColumnCount(). For the purposes of
     * the Likelihood detection configuration widget, the TreeModel is always
     * two columns.
     * 
     * @return the column count
     */
    
     public int getColumnCount() {
        return 2;
     }
     
     /**
      * Implementation of TreeModel.getColumnName.
      * 
      * @param column the column
      * 
      * @return the column name
      * 
      * @see TreeModel
      */
     
     public String getColumnName( int column) {
       if ( column == 0 ) return "";
       return "Value";
     }
     
     /**
      * Implementation of TreeModel.getValueAt(). The value that gets
      * returned depends on the type of node in the representation being
      * queried about.
      * 
      * @param node the node
      * @param column the column
      * 
      * @return the value at
      * 
      * @see TreeModel
      */
     
     public Object getValueAt(Object node, int column) {
       if ( node.getClass() == TargetNode.class && column == 1 ) return null;
       else if ( node.getClass() == FrequencyResolutionNode.class && column == 1 ) {
        return ((FrequencyResolutionNode)node).resolutionAsString(); 
       }
       else if ( node.getClass() == ConfigurationNode.class && column == 1 ) {
          return null; 
       }
       else if ( node.getClass() == TimeResolutionNode.class && column == 1 ) {
        return ((TimeResolutionNode)node).resolutionAsString(); 
       }
       else if ( node.getClass() == AlgorithmNode.class && column == 1  ) {
        return ((AlgorithmNode)node).algorithmAsString();  
       }
       else if ( node.getClass() == GuardBandThresholdNode.class && column == 1 ) {
        return ((GuardBandThresholdNode)node).valueToString(); 
       }
       else if ( node.getClass() == SnrThresholdNode.class && column == 1 ) {
          return ((SnrThresholdNode)node).thresholdToString();
       }
       else if ( node.getClass() == StartFrequencyNode.class && column == 1 ) {
          return ((StartFrequencyNode)node).valueToString(); 
       }
       else if ( node.getClass() == EndFrequencyNode.class && column == 1 ) {
          return ((EndFrequencyNode)node).valueToString(); 
       }
       else if ( node.getClass() == BackgroundNode.class && column == 1 ) {
          return ((BackgroundNode)node).valueToString(); 
       }
       else if ( node.getClass() == SignalNode.class && column == 1 ) {
         return ((SignalNode)node).valueToString();
       }
       else if ( node.getClass() == DetectionBandsNode.class && column == 1 ) {
          return null; 
       }
       else if ( node.getClass() == GuardBandNode.class && column == 1 ) {
          return null; 
       }
       else if ( node.getClass() == SignalBandNode.class && column == 1 ) {
          return null; 
       }
       else if ( node.getClass() == SecondsBetweenDetectionsNode.class && column == 1 ) {
    	  return ((SecondsBetweenDetectionsNode)node).valueAsString();   
       }
       else if ( node.getClass() == AssociatedBandNode.class && column == 1 ) {
          return ((AssociatedBandNode)node).valueToString(); 
       }
       
       
       return ((Node)node).toString();
     }
}

/**
 * The Node is the base class for the internal representations of a 
 * TargetConfiguration object. The TargetConfiguration used within the 
 * TreeTableModel needs to be represented as a tree to work with the
 * JTree object, so this serves as a common base class for that purpose.
 * <p>
 * The Node holds an ArrayList of child nodes.
 */

class Node {
  public ArrayList<Node> children = new ArrayList<Node>();

  public boolean isError() {
		return false;  
	}
}

/**
 * The ConfigurationNode represents the top-level of the the TreeTableModel's
 * internal representation of an ArrayList of TargetConfiguration objects. It
 * only contains TargetNodes.
 */

class ConfigurationNode extends Node {
  public void addTargetNode( TargetNode t ) {
    children.add( t );
  }
  
  @Override
public String toString() {
    return "Target Configurations"; 
  }
  
}

/** 
 * The TargetNode is the representation of a TargetConfiguration object.
 * The accessors and mutators correspond directly to the available fields
 * in a TargetConfiguration.
 */

class TargetNode extends Node implements ItemListener {
 
  public TargetNode( String name ) {
    this.name = name;
    this.state = TargetConfiguration.State.Active;
  }
  
  public String getName() {
    return name;  
  }
    
  @Override
public String toString() {
    return getName(); 
  }
      
  public void setState( TargetConfiguration.State state ) {
	this.state = state;  
  }
  
  @Override
public boolean isError() {
	return ( this.getState() == TargetConfiguration.State.Error );  
  }
  
  public TargetConfiguration.State getState() {
	if ( this.state == TargetConfiguration.State.Inactive ) return this.state;
	
	this.state = TargetConfiguration.State.Active;
	
	// Loop down over the children and get their states.
	for ( Node n : children ) {
		if ( n.isError() ) this.state = TargetConfiguration.State.Error;
	}
	
	int numberSignalNodes = this.numberSignalBands();
	if ( numberSignalNodes == 0 ) this.state = TargetConfiguration.State.Error;
	
	return this.state;
  }
  
  public int numberSignalBands() {
		// If there are no signal node children, then there is an error.
		int numberSignalNodes = 0;
		for ( Node n : children ) {
			if ( n.getClass() == DetectionBandsNode.class ) {
				for ( Node bn : n.children ) {
					if ( bn.getClass() == SignalBandNode.class ) ++numberSignalNodes;
				}
			}
		}
		
		return numberSignalNodes;
  }
  
  @Override
public void itemStateChanged( ItemEvent e ) {
    if ( e.getStateChange() == ItemEvent.SELECTED ) {
    	if ( this.state != TargetConfiguration.State.Error ) state = TargetConfiguration.State.Active;
    }
    else if ( e.getStateChange() == ItemEvent.DESELECTED ) {
     	this.state = TargetConfiguration.State.Inactive;
    }
  }
  
  private TargetConfiguration.State state;
  private String name;
}

/** 
 * The TimeResolutionNode represents the time resolution value
 * for a TargetConfiguration.
 */

class TimeResolutionNode extends Node {

  public TimeResolutionNode( TargetNode targetNode,
		                        double resolution, 
		  						double[] limits,
		  						AcquisitionSettings acquisitionSettings,
		  						int channelMap,
		  						FrequencyResolutionNode fn ) {
    this.resolution = resolution; 
    this.limits = limits;
    this.freqResNode = fn;
    this.acquisitionSettings = acquisitionSettings;
    this.channelMap = channelMap;
    this.targetNode = targetNode;
  }
  
  @Override
public String toString() {
    return "Time Resolution (s)"; 
  }
  
  public String resolutionAsString() {
	  DecimalFormat df = new DecimalFormat( "######0.0###" );
    return new Double( df.format( resolution ) ).toString(); 
  }
  
  public void setResolution( double resolution ) {
    this.resolution = resolution;  
    
    // Once the resolution is set, it is necessary to recompute
    // what the _real_ time resolution is going to be for the 
    // FFT process. This is because the lower-bound limit
    // on the sconds parameter is that value.
    LikelihoodFFTParameters params = new LikelihoodFFTParameters( this.acquisitionSettings, 
    		this.channelMap,
    		freqResNode.getResolution(),
    		this.resolution );
    	
   	// Go over all of the existing bands and change the lower limit
    // on the signal parameter.
    for ( Node n : this.targetNode.children ) {
    	if ( n.getClass() == DetectionBandsNode.class ) {
    		for ( Node b : n.children ) {
    			if ( b.getClass() == SignalBandNode.class || b.getClass() == GuardBandNode.class ) {
    				for ( Node s : b.children ) {
    					if ( s.getClass() == SignalNode.class ) {
    						SignalNode sn = (SignalNode)s;
    						sn.setLowerLimit( params.getActualTimeResolution() );
    					}
    				}
    			}
    		}
    	}
    }
    
  }
  
  public double getResolution() {
	return this.resolution;  
  }
  
  public double[] getLimits() {
	  return this.limits;
  }
  
  @Override
public boolean isError() {
	if ( this.resolution < this.limits[0] ) return true;
	else if ( this.resolution > this.limits[1] ) return true;
	else return false;
  }
  
  private TargetNode targetNode;
  private FrequencyResolutionNode freqResNode;
  private double resolution;
  private double[] limits = new double[2];
  private int channelMap;
  private AcquisitionSettings acquisitionSettings;
}

/**
 * The FrequencyResolutionNode represents the frequency resolution
 * value for a TargetConfiguration.
 */

class FrequencyResolutionNode extends Node {

  public FrequencyResolutionNode( double resolution, double[] limits ) {
    this.resolution = resolution; 
    this.limits = limits;
  }
  
  @Override
public String toString() {
    return "Frequency Resolution (Hz)"; 
  }
  
  public String resolutionAsString() {
	  DecimalFormat df = new DecimalFormat( "######0.0###" );
    return new Double( df.format( resolution ) ).toString(); 
  }
  
  public void setResolution( double resolution ) {
    this.resolution = resolution;  
  }
  
  public double getResolution() {
	return this.resolution;  
  }
  
  public double[] getLimits() {
	  return this.limits;
  }
  
  @Override
public boolean isError() {
	if ( this.resolution < this.limits[0] ) return true;
	else if ( this.resolution > this.limits[1] ) return true;
	else return false;
  }

  private double resolution;
  private double[] limits = new double[2];
}

/**
 * The AlgorithmNode represents the algorithm used for a 
 * TargetConfiguration.
 */

class AlgorithmNode extends Node {

  public AlgorithmNode( String algorithm ) {
    this.algorithm = algorithm;
  }
  
  @Override
public String toString() {
    return "Algorithm"; 
  }
  
  public String algorithmAsString() {
    return algorithm; 
  }
  
  public void setAlgorithm( String algorithm ) {
    this.algorithm = algorithm;  
  }
  
  private String algorithm;
}

class SecondsBetweenDetectionsNode extends Node {

	private double seconds;
	private double[] limits = new double[2];
	
	public SecondsBetweenDetectionsNode( double value, double[] limits ) {
		this.seconds = value;
		this.limits = limits;
	}
	
	@Override
	public String toString() {
		return "Minimum detection interval (s)";	
	}
	
	public void setValue( double value ) {
		this.seconds = value;	
	}
	
	public double getSeconds() {
		return this.seconds;
	}
	
	public double[] getLimits() {
		return this.limits;	
	}
	
	@Override
	public boolean isError() {
		if ( this.seconds < this.limits[0] ) return true;
		else if ( this.seconds > this.limits[1] ) return true;
		else return false;
	}
	
	public String valueAsString() {
		return Double.toString( this.seconds );
	}
}

/** 
 * Represents the all of the detection bands, both signal and guard,
 * that are set up for a TargetConfiguration. This is represented
 * in the tree as a node with no value, serving only to roll up the
 * underlying bands.
 */

class DetectionBandsNode extends Node {
  
	DetectionBandsNode( String configName ) {
		this.configName = configName;	
	}
	
  @Override
public String toString() {
    return "Detection Bands"; 
  }
 
  @Override
public boolean isError() {
	for ( Node n : children ) {
		if ( n.isError() ) return true;	
	}
	
    return false;
  }
  
  public String getConfigName() {
	  return this.configName;
  }
  
  private String configName;
}

/** 
 * Represents a signal band within a TargetConfiguration. The only
 * aspect that is displayed is the name of the band. The band's
 * characteristics will be children of this node.
 */

class SignalBandNode extends Node {
 
  public SignalBandNode( String name, String configName ) {
    this.name = name; 
    this.configName = configName;
  }
  
  @Override
public String toString() {
    return name + " (Signal Band)"; 
  }
  
  public String getName() {
	return name;  
  }
  
  public String getConfigName() {
	return this.configName;  
  }
  
  @Override
public boolean isError() {
	
	  for ( Node n : children ) {
		if ( n.isError() ) return true;  
	  }
	  return false;
  }
  
  private String name;
  private String configName;
}

/** 
 * Represents a guard band within a TargetConfiguration. The only
 * aspect that is displayed is the name of the band. The band's
 * characteristics will be children of this node.
 */

class GuardBandNode extends Node {
 
  public GuardBandNode( String name, String configName ) {
    this.name = name; 
    this.configName = configName;
  }
  
  @Override
public String toString() {
    return name + " (Guard Band)"; 
  }
  
  public String getName() {
	return this.name;  
  }
  
  public String getConfigName() {
	return this.configName;  
  }
  
  @Override
public boolean isError() {
	for ( Node n : children ) {
		if ( n.isError() ) return true;	
	}
	  return false;
  }
  
  private String name;
  private String configName;
}

/**
 * Represents the band SNR threshold for a signal band.
 */

class SnrThresholdNode extends Node {
  private double threshold;
  private double[] limits = new double[2];
  
  public SnrThresholdNode( double threshold, double[] limits ) {
    this.threshold = threshold;
    this.limits = limits;
  }
  
  public void setThreshold( double threshold ) {
    this.threshold = threshold; 
  }
  
  public double getThreshold() {
    return this.threshold;
  }
  
  @Override
public String toString() {
    return "In-Band Threshold (dB)"; 
  }
  
  public String thresholdToString() {
	  DecimalFormat df = new DecimalFormat( "######0.0###" );
    return new Double( df.format( threshold ) ).toString(); 
  }
  
  public double[] getLimits() {
	  return this.limits;
  }

  @Override
public boolean isError() {
	if ( this.threshold < this.limits[0] ) return true;
	else if ( this.threshold > this.limits[1] ) return true;
	else return false;
  }
}

/**
 * Represents the name of the associated signal band for a 
 * guard band.
 */

class AssociatedBandNode extends Node {
  private String bandName;
  
  public AssociatedBandNode( String bandName ) {
    this.bandName = bandName;
  }
  
  public void setBandName( String bandName ) {
    this.bandName = bandName; 
  }
  
  public String getBandName() {
    return this.bandName;
  }
  
  @Override
public String toString() {
    return "Associated Signal Band"; 
  }
  
  public String valueToString() {
    return this.bandName; 
  }
}

/** 
 * Represents the start frequency for a band.
 */

class StartFrequencyNode extends Node {
  private double frequency;
  private double lowerLimit;
  private EndFrequencyNode endNode;
  
  public StartFrequencyNode( double frequency, double lowerLimit ) {
    this.frequency = frequency;
    this.lowerLimit = lowerLimit;
  }
  
  public void setFrequency( double frequency ) {
    this.frequency = frequency;
  }
  
  public double getFrequency() {
    return this.frequency; 
  }
  
  @Override
public String toString() {
    return "Start/Low Frequency (Hz)"; 
  }
  
  public String valueToString() {
	  DecimalFormat df = new DecimalFormat( "######0.0###" );
    return new Double( df.format( frequency ) ).toString(); 
  }
  
  public double[] getLimits() {
	  double [] limits = new double[2];
	  limits[0] = this.lowerLimit;
	  limits[1] = this.endNode.getFrequency();
	  return limits;
  }

  @Override
public boolean isError() {
    if ( this.frequency < this.lowerLimit ) return true;
    else if ( this.frequency >= this.endNode.getFrequency() ) return true;
    return false;
  }
  
  public void setEndFrequencyNode( EndFrequencyNode endNode ) {
	  this.endNode = endNode;
  }
}

/**
 * Represents the end frequency for a band.
 */

class EndFrequencyNode extends Node {
  private double frequency;
  private double upperLimit;
  private StartFrequencyNode startNode;
  
  public EndFrequencyNode( double frequency, double upperLimit, StartFrequencyNode startNode ) {
    this.frequency = frequency;
    this.upperLimit = upperLimit;
  	this.startNode = startNode;
  }
  
  public void setFrequency( double frequency ) {
    this.frequency = frequency; 
  }
  
  public double getFrequency() {
    return this.frequency; 
  }
  
  @Override
public String toString() {
    return "End/High Frequency (Hz)"; 
  }
  
  public String valueToString() {
	  DecimalFormat df = new DecimalFormat( "######0.0###" );
    return new Double( df.format( frequency ) ).toString(); 
  }
  
  public double[] getLimits() {
	  double[] limits = new double[2];
	  limits[0] = this.startNode.getFrequency();
	  limits[1] = this.upperLimit;
	  return limits;
  }

  @Override
public boolean isError() {
    if ( this.frequency < this.startNode.getFrequency() ) return true;
    else if ( this.frequency > this.upperLimit ) return true;
    else return false;
  }
}

/**
 * Represents the background seconds setting for a band.
 */

class BackgroundNode extends Node {
  private double noise;
  private double upper_limit;
  private SignalNode signalNode;
  private double warnLimit;
  
  public BackgroundNode( double noise, double upper_limit, SignalNode signalNode, double warnLimit  ) {
    this.noise = noise;
    this.upper_limit = upper_limit;
    this.signalNode = signalNode;
    this.warnLimit = warnLimit;
  }
  
  public void setBackgroundNoise( double noise ) {
    this.noise = noise;
  }
  
  public double getBackgroundNoise() {
    return this.noise; 
  }
  
  @Override
public String toString() {
    return "Noise Window (s)"; 
  }
  
  public boolean overWarnLimit() {
	return this.noise > this.warnLimit;  
  }
  
  public String valueToString() {
	DecimalFormat df = new DecimalFormat( "######0.0###" );
	return new Double( df.format( this.noise ) ).toString();
  }
  
  public double[] getLimits() {
	  double[] limits = new double[2];
	  limits[0] = this.signalNode.getSignalSecods();
	  limits[1] = this.upper_limit;
	  return limits;
  }
  
  @Override
public boolean isError() {
    if ( this.noise < this.signalNode.getSignalSecods() ) return true;
    else if ( this.noise > this.upper_limit ) return true;
    else return false;
  }
  
  public void setSignalNode( SignalNode signalNode ) {
	this.signalNode = signalNode;  
  }
}

/**
 * Represents the signal seconds for a band.
 */

class SignalNode extends Node {
  private double seconds;
  private double lowerLimit;
  private BackgroundNode backNode;
  private double warnLimit;
  
  // This constructor is just used temporarily because of a construction
  // dependency between the SignalNode and the BackgroundNode.
  public SignalNode() {}
  
  public SignalNode( double seconds, double lowerLimit, BackgroundNode backNode, double warnLimit ) {
    this.seconds = seconds;
    this.lowerLimit = lowerLimit;
    this.backNode = backNode;
    this.warnLimit = warnLimit;
  }
  
  public void setSignalSeconds( double seconds ) {
    this.seconds = seconds;
  }
  
  public double getSignalSecods() {
    return this.seconds; 
  }
  
  @Override
public String toString() {
    return "Signal Window (s)"; 
  }
  
  public String valueToString() {
	DecimalFormat df = new DecimalFormat( "######0.0###" );
    return new Double( df.format(seconds) ).toString(); 
  }
  
  public boolean overWarnLimit() {
	return this.seconds > this.warnLimit;  
  }
  
  public double[] getLimits() {
	  double[] limits = new double[2];
	  limits[0] = this.lowerLimit;
	  limits[1] = this.backNode.getBackgroundNoise();
	  return limits;
  }
  
  public void setLowerLimit( double limit ) {
	  this.lowerLimit = limit;
  }
  
  @Override
public boolean isError() {
	if ( this.seconds < this.lowerLimit ) return true;
	else if ( this.seconds > this.backNode.getBackgroundNoise() ) return true;
	else return false;
  }
}

/**
 * Represents the DB threshold for guard bands.
 */

class GuardBandThresholdNode extends Node {
 public GuardBandThresholdNode( double seconds, double[] limits ) {
  this.seconds = seconds; 
  this.limits = limits;
 }
 
 public void setSeconds( double seconds ) {
   this.seconds = seconds;
 }
 
 public double getSeconds() {
   return this.seconds;
 }
 
 @Override
public String toString() {
  return "Guard Band Threshold (dB)"; 
 }
 
 public String valueToString() {
	 DecimalFormat df = new DecimalFormat( "######0.0###" );
    return new Double( df.format( this.seconds ) ).toString(); 
 }
 
 public double[] getLimits() {
	 return this.limits;
 }
 
 @Override
public boolean isError() {
	if ( this.seconds < this.limits[0] ) return true;
	else if ( this.seconds > this.limits[1] ) return true;
	else return false;
 }
 
 private double[] limits = new double[2];
 private double seconds;
}
