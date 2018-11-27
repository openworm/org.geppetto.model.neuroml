'''
NETPYNE simulator compliant export for:

Components:
    net1 (Type: network)
    sim1 (Type: Simulation:  length=1.0 (SI time) step=5.0E-5 (SI time))
    hhcell (Type: cell)
    passive (Type: ionChannelPassive:  conductance=1.0E-11 (SI conductance))
    na (Type: ionChannelHH:  conductance=1.0E-11 (SI conductance))
    k (Type: ionChannelHH:  conductance=1.0E-11 (SI conductance))
    pulseGen1 (Type: pulseGenerator:  delay=0.0 (SI time) duration=1.0E8 (SI time) amplitude=8.000000000000001E-11 (SI current))


    This NETPYNE file has been generated by org.neuroml.export (see https://github.com/NeuroML/org.neuroml.export)
         org.neuroml.export  v1.5.4
         org.neuroml.model   v1.5.4
         jLEMS               v0.9.9.1

'''
# Main NetPyNE script for: net1

# See https://github.com/Neurosim-lab/netpyne

from netpyne import specs  # import netpyne specs module
from netpyne import sim    # import netpyne sim module

from neuron import h

import sys


###############################################################################
# NETWORK PARAMETERS
###############################################################################

nml2_file_name = 'NET_net1.net.nml'

###############################################################################
# SIMULATION PARAMETERS
###############################################################################

simConfig = specs.SimConfig()   # object of class SimConfig to store the simulation configuration

# Simulation parameters
simConfig.duration = simConfig.tstop = 1000.0 # Duration of the simulation, in ms
simConfig.dt = 0.05 # Internal integration timestep to use

# Seeds for randomizers (connectivity, input stimulation and cell locations)
# Note: locations and connections should be fully specified by the structure of the NeuroML,
# so seeds for conn & loc shouldn't affect networks structure/behaviour
simConfig.seeds = {'conn': 0, 'stim': 123456789, 'loc': 0} 

simConfig.createNEURONObj = 1  # create HOC objects when instantiating network
simConfig.createPyStruct = 1  # create Python structure (simulator-independent) when instantiating network
simConfig.verbose = False  # show detailed messages 

# Recording 
simConfig.recordCells = ['all']  
simConfig.recordTraces = {}
simConfig.saveCellSecs=False
simConfig.saveCellConns=False
simConfig.gatherOnlySimData=True 



simConfig.plotCells = ['all']


simConfig.recordStim = True  # record spikes of cell stims
simConfig.recordStep = simConfig.dt # Step size in ms to save data (eg. V traces, LFP, etc)



# Analysis and plotting, see http://neurosimlab.org/netpyne/reference.html#analysis-related-functions
simConfig.analysis['plotRaster'] = False  # Plot raster
simConfig.analysis['plot2Dnet'] = False  # Plot 2D net cells and connections
simConfig.analysis['plotSpikeHist'] = False # plot spike histogram
simConfig.analysis['plotConn'] = False # plot network connectivity
simConfig.analysis['plotSpikePSD'] = False # plot 3d architecture

# Saving
simConfig.filename = 'net1.txt'  # Set file output name
simConfig.saveFileStep = simConfig.dt # step size in ms to save data to disk
# simConfig.saveDat = True # save to dat file


###############################################################################
# IMPORT & RUN
###############################################################################

print("Running a NetPyNE based simulation for %sms (dt: %sms) at %s degC"%(simConfig.duration, simConfig.dt, simConfig.hParams['celsius']))

gids = sim.importNeuroML2SimulateAnalyze(nml2_file_name,simConfig)

print("Finished simulation")


###############################################################################
#   Saving data (this ensures the data gets saved in the format/files 
#   as specified in the LEMS <Simulation> element)
###############################################################################


if sim.rank==0: 



    print("Saved all data.")

if '-nogui' in sys.argv:
    quit()
