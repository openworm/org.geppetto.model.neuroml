from neuroml import *
from pyneuroml import pynml
import neuroml.writers as writers

from pyneuroml.lems import generate_lems_file_for_neuroml

import random

population_defs = {}
scale = 1
population_defs['L23PyrRS'] = 2*scale, 'L23'
population_defs['SupBasket'] = 0*scale, 'L23'
population_defs['SupAxAx'] = 2*scale, 'L23'
population_defs['L5TuftedPyrIB'] = 0*scale, 'L5'

t1=-0
t2=-250
t3=-250
t4=-200.0
t5=-300.0
t6=-300.0
ys = {}

ys['L1']=[0,t1]
ys['L23']=[t1,t1+t2+t3]
ys['L4']=[t1+t2+t3,t1+t2+t3+t4]
ys['L5']=[t1+t2+t3+t4,t1+t2+t3+t4+t5]
ys['L6']=[t1+t2+t3+t4+t5,t1+t2+t3+t4+t5+t6]

xs = [0,500]
zs = [0,500] 

net_ref = "Net3D"
net_doc = NeuroMLDocument(id=net_ref)

net = Network(id=net_ref)
net_doc.networks.append(net)

count = 0

for ref in population_defs.keys():
    
    size, layer = population_defs[ref]
    
    if size>0:
        net_doc.includes.append(IncludeType("%s.cell.nml"%ref))

        pop = Population(id="Pop_%s"%ref, component=ref, type="populationList")

        net.populations.append(pop)

        for i in range(size):
            inst = Instance(id=i)
            pop.instances.append(inst)

            X=xs[0]+random.random()*(xs[1]-xs[0])
            Z=zs[0]+random.random()*(zs[1]-zs[0])

            Y =   ys[layer][0]+random.random()*(ys[layer][1]-ys[layer][0])

            inst.location = Location(x=X, y=Y, z=Z)

            count+=1

net_file = '%s.net.nml'%(net_ref)
writers.NeuroMLWriter.write(net_doc, net_file)

print("Written network with %i cells in network to: %s"%(count,net_file))

from neuroml.utils import validate_neuroml2

validate_neuroml2(net_file)

generate_lems_file_for_neuroml("Sim_"+net_ref, 
                               net_file, 
                               net_ref, 
                               50, 
                               0.025, 
                               "LEMS_%s.xml"%net_ref,
                               ".",
                               gen_plots_for_all_v = True,
                               plot_all_segments = True,
                               gen_saves_for_all_v = True,
                               save_all_segments = True,
                               copy_neuroml = False,
                               seed = 1234)

pynml.nml2_to_svg(net_file)

