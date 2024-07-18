import numpy as np
import plotly.graph_objs as go
from plotly.subplots import make_subplots


sequential = np.mean([62.785580, 62.786268, 62.785547, 62.786792, 62.786779])
OMP_2 = np.mean([31.751337, 31.750869, 31.750362, 31.744399, 31.751472])
OMP_4 = np.mean([19.488861, 19.481945, 19.484321, 19.480824, 19.488803])
OMP_8 = np.mean([10.165672, 10.156603, 10.158897, 10.159906, 10.165682])

OMP = [sequential, OMP_2, OMP_4, OMP_8]

MPI_2 = np.mean([31.901491, 31.909121, 31.913423, 32.015724, 31.912580])
MPI_4 = np.mean([19.528747, 19.534196, 19.536538, 19.532826, 19.532379])
MPI_8 = np.mean([10.268146, 10.182746, 10.368574, 10.251303, 10.369646])

MPI = [sequential, MPI_2, MPI_4, MPI_8]

Hyb_2 = np.mean([19.593021, 19.593699, 19.700898, 19.702227, 19.701239])
Hyb_4 = np.mean([5.554986, 5.638201, 5.437654, 5.639317, 5.650358])
Hyb_8 = np.mean([1.951109, 1.817650, 1.847375, 1.806273, 1.804778])

Hyb = [sequential, Hyb_2, Hyb_4, Hyb_8]

S_OMP = sequential / OMP
S_MPI = sequential / MPI
S_Hyb = sequential / Hyb

PE = [1, 2, 4, 8]
PE2 = [1, 4, 16, 64]

E_OMP = S_OMP / PE
E_MPI = S_MPI / PE
E_Hyb = S_Hyb / np.array(PE)**2


# Define functions for updating subplot properties
def update_subplot_layout(fig, row, col, x_title, y_title, y_data, max_value):
    fig.update_xaxes(title_text=x_title, row=row, col=col)
    fig.update_yaxes(title_text=y_title, row=row, col=col, range=[0, max_value])
    fig.add_trace(go.Scatter(x=PE if col != 3 else PE2, y=y_data, mode='lines', name=f'{y_title}'), row=row, col=col)

# Use the functions to create subplots
fig = make_subplots(rows=1, cols=3, subplot_titles=('Speedup Graph <br> increasing OpenMP Threads',
                                                   'Speedup Graph <br> increasing MPI ranks',
                                                   'Speedup Graph <br> Hybrid'))

update_subplot_layout(fig, 1, 1, 'Number of Threads', 'Speedup', S_OMP, max(S_OMP))
update_subplot_layout(fig, 1, 2, 'Number of Ranks', 'Speedup', S_MPI, max(S_MPI))
update_subplot_layout(fig, 1, 3, 'Number of Ranks and Threads', 'Speedup', S_Hyb, max(S_Hyb))

# Update layout for all subplots
fig.update_layout(height=600, width=1000, title_text='Speedup Graphs', showlegend=True)
fig.show()

# Similarly for Efficiency Graphs
fig = make_subplots(rows=1, cols=3, subplot_titles=('Efficiency Graph <br> increasing OpenMP Threads',
                                                   'Efficiency Graph <br> increasing MPI ranks',
                                                   'Efficiency Graph <br> Hybrid'))

update_subplot_layout(fig, 1, 1, 'Number of Threads', 'Efficiency', E_OMP, max(E_OMP))
update_subplot_layout(fig, 1, 2, 'Number of Ranks', 'Efficiency', E_MPI, max(E_MPI))
update_subplot_layout(fig, 1, 3, 'Number of Ranks and Threads', 'Efficiency', E_Hyb, max(E_Hyb))

fig.update_layout(height=600, width=1000, title_text='Efficiency Graphs', showlegend=True)
fig.show()


