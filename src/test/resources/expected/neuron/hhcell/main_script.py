'''
Neuron simulator export for:

Components:
    net1 (Type: network)
    sim1 (Type: Simulation:  length=1.0 (SI time) step=5.0E-5 (SI time))
    hhcell (Type: cell)
    passive (Type: ionChannelPassive:  conductance=1.0E-11 (SI conductance))
    na (Type: ionChannelHH:  conductance=1.0E-11 (SI conductance))
    k (Type: ionChannelHH:  conductance=1.0E-11 (SI conductance))
    pulseGen1 (Type: pulseGenerator:  delay=0.0 (SI time) duration=1.0E8 (SI time) amplitude=8.000000000000001E-11 (SI current))


    This NEURON file has been generated by org.neuroml.export (see https://github.com/NeuroML/org.neuroml.export)
         org.neuroml.export  v1.5.3
         org.neuroml.model   v1.5.3
         jLEMS               v0.9.9.0

'''

import neuron

import time

import hashlib
h = neuron.h
h.load_file("stdlib.hoc")

h.load_file("stdgui.hoc")

h("objref p")
h("p = new PythonObject()")

class NeuronSimulation():

    def __init__(self, tstop, dt, seed=123456789):

        print("\n    Starting simulation in NEURON generated from NeuroML2 model...\n")

        self.seed = seed
        self.randoms = []
        self.next_global_id = 0  # Used in Random123 classes for elements using random(), etc. 

        self.next_spiking_input_id = 0  # Used in Random123 classes for elements using random(), etc. 

        '''
        Adding simulation Component(id=sim1 type=Simulation) of network/component: net1 (Type: network)
        
        '''
        # ######################   Population: hhpop
        print("Population hhpop contains 1 instance(s) of component: hhcell of type: cell")

        h.load_file("hhcell.hoc")
        a_hhpop = []
        h("{ n_hhpop = 1 }")
        h("objectvar a_hhpop[n_hhpop]")
        for i in range(int(h.n_hhpop)):
            h("a_hhpop[%i] = new hhcell()"%i)
            h("access a_hhpop[%i].soma"%i)

            self.next_global_id+=1


        h("proc initialiseV_hhpop() { for i = 0, n_hhpop-1 { a_hhpop[i].set_initial_v() } }")
        h("objref fih_hhpop")
        h('{fih_hhpop = new FInitializeHandler(0, "initialiseV_hhpop()")}')

        h("proc initialiseIons_hhpop() { for i = 0, n_hhpop-1 { a_hhpop[i].set_initial_ion_properties() } }")
        h("objref fih_ion_hhpop")
        h('{fih_ion_hhpop = new FInitializeHandler(1, "initialiseIons_hhpop()")}')

        # Adding single input: Component(id=null type=explicitInput)
        h("objref explicitInput_pulseGen1a_hhpop0_soma")
        h("a_hhpop[0].soma { explicitInput_pulseGen1a_hhpop0_soma = new pulseGen1(0.5) } ")

        trec = h.Vector()
        trec.record(h._ref_t)

        h.tstop = tstop

        h.dt = dt

        h.steps_per_ms = 1/h.dt



        # ######################   File to save: time.dat (time)
        # Column: time
        h(' objectvar v_time ')
        h(' { v_time = new Vector() } ')
        h(' { v_time.record(&t) } ')
        h.v_time.resize((h.tstop * h.steps_per_ms) + 1)

        self.initialized = False

        self.sim_end = -1 # will be overwritten

    def run(self):

        self.initialized = True
        sim_start = time.time()
        print("Running a simulation of %sms (dt = %sms; seed=%s)" % (h.tstop, h.dt, self.seed))

        h.run()

        self.sim_end = time.time()
        sim_time = self.sim_end - sim_start
        print("Finished NEURON simulation in %f seconds (%f mins)..."%(sim_time, sim_time/60.0))

        self.save_results()


    def advance(self):

        if not self.initialized:
            h.finitialize()
            self.initialized = True

        h.fadvance()


    ###############################################################################
    # Hash function to use in generation of random value
    # This is copied from NetPyNE: https://github.com/Neurosim-lab/netpyne/blob/master/netpyne/simFuncs.py
    ###############################################################################
    def _id32 (self,obj): 
        return int(hashlib.md5(obj).hexdigest()[0:8],16)  # convert 8 first chars of md5 hash in base 16 to int


    ###############################################################################
    # Initialize the stim randomizer
    # This is copied from NetPyNE: https://github.com/Neurosim-lab/netpyne/blob/master/netpyne/simFuncs.py
    ###############################################################################
    def _init_stim_randomizer(self,rand, stimType, gid, seed): 
        rand.Random123(self._id32(stimType), gid, seed)


    def save_results(self):

        print("Saving results at t=%s..."%h.t)

        if self.sim_end < 0: self.sim_end = time.time()


        # ######################   File to save: time.dat (time)
        py_v_time = [ t/1000 for t in h.v_time.to_python() ]  # Convert to Python list for speed...

        f_time_f2 = open('time.dat', 'w')
        num_points = len(py_v_time)  # Simulation may have been stopped before tstop...

        for i in range(num_points):
            f_time_f2.write('%f'% py_v_time[i])  # Save in SI units...
        f_time_f2.close()
        print("Saved data to: time.dat")

        save_end = time.time()
        save_time = save_end - self.sim_end
        print("Finished saving results in %f seconds"%(save_time))

        print("Done")

        quit()


if __name__ == '__main__':

    ns = NeuronSimulation(tstop=1000.0, dt=0.049999997, seed=123456789)

    ns.run()

