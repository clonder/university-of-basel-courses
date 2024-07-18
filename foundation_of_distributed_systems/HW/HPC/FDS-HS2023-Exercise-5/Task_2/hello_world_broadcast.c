#include <stdio.h>
#include <mpi.h>
#include <string.h>

#define BUFFER_SIZE 20

int main(int argc, char *argv[]) {
    int num_ranks, rank_id;
    char buffer[BUFFER_SIZE];

    MPI_Init(NULL, NULL); // Initialize MPI environment
    MPI_Comm_size(MPI_COMM_WORLD, &numranks); // Get the total number of processes
    MPI_Comm_rank(MPI_COMM_WORLD, &rankid); // Get the rank of the current process

    if (rank_id == 0) {
        strcpy(buffer, "Hello, World!"); // Populate the message in the root process
        printf("Broadcasting the message to all processes...\n");
    }

    // Broadcast the message from rank 0 to all processes
    MPI_Bcast(buffer, BUFFER_SIZE, MPI_CHAR, 0, MPI_COMM_WORLD);

    printf("Process %d received the message.\n", rank_id);

    // Ensure all processes have received the message before proceeding
    MPI_Barrier(MPI_COMM_WORLD);

    if (rank_id == num_ranks - 1) {
        // Print the message from the last process after all have received it
        printf("All processes have received the message.\n");
        printf("%s printed from process %d\n", buffer, rank_id);
    }

    MPI_Finalize();

    return 0;
}

