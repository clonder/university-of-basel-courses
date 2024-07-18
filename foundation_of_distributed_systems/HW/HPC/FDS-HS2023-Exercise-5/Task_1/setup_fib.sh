#!/bin/bash
#SBATCH -J TEST # Job name.
#SBATCH --time=00:05:00 # Maximum running time for this job.
#SBATCH --exclusive # You will run yours experiments alone in the available nodes.
#SBATCH --nodes=1 #Number of nodes you will allocate.
#SBATCH --ntasks-per-node=1 #Number of MPI ranks per node. That means if you use nodes=2 and ntasks-per-node=2 you will be running 4 ranks.
#SBATCH --cpus-per-task=10 #Number of OpenMP threads.
#SBATCH --partition=xeon #– Which partition you will use.
#SBATCH --output=fibpar_OUTPUT.txt #– The files where the outputs of your program will appear.
#SBATCH --hint=nomultithread #– Block the usage of hyper-threads.

ml intel #– Load the intel compiler.
icc -O3 -fopenmp fibonacci_sequential.c -o fib
srun --cpus-per-task=10 fib

#srun fibonacci_par
