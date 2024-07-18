#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

int main() {
    int rankID, p;
    int local_sum = 0;
    int global_sum = 0;
    int max = 2000;


    MPI_Init(NULL, NULL); // Initialize MPI environment
    MPI_Comm_size(MPI_COMM_WORLD, &numranks); // Get the total number of processes
    MPI_Comm_rank(MPI_COMM_WORLD, &rankid); // Get the rank of the current process

    // Distribute the work among processes
    for (int i = rankID; i <= max; i += p) {
        local_sum += i; // Each process computes its local sum
    }

    // Reduce all local sums into a global sum, combines data from all processes in a communicator and stores it in a specified process
    MPI_Reduce(&local_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);

    // Output the result from the master process
    if (rankID == 0) {
        printf("Cumulative sum from 1 to %d: %d\n", max, global_sum);
    }

    MPI_Finalize();
    return 0;
}

