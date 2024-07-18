#include <stdio.h>
#include <mpi.h>
#include <string.h>

#define BUFFER_SIZE 20

int main (int argc, char *argv[]) {
    int numranks, rankid;
    char buffer_A[BUFFER_SIZE];

    MPI_Init(NULL, NULL); // Initialize MPI environment
    MPI_Comm_size(MPI_COMM_WORLD, &numranks); // Get the total number of processes
    MPI_Comm_rank(MPI_COMM_WORLD, &rankid); // Get the rank of the current process

    MPI_Status stat1;
    MPI_Request reqs, reqr;

    if (rankid == 0) {
        // Master thread initializes the string and sends it to rank 1
        strcpy(buffer_A, "Hello World\n");
        printf("Process %d: Sending array to process %d\n", rankid, rankid + 1);
        MPI_Isend(buffer_A, BUFFER_SIZE, MPI_CHAR, 1, 0, MPI_COMM_WORLD, &reqs);
    } else if (rankid < numranks - 1) {
        // Ranks between 1 and numranks - 2 receive the message and send it to the next one
        MPI_Irecv(buffer_A, BUFFER_SIZE, MPI_CHAR, rankid - 1, 0, MPI_COMM_WORLD, &reqr);
        MPI_Wait(&reqr, &stat1); // Wait for receiving the message before sending
        MPI_Isend(buffer_A, BUFFER_SIZE, MPI_CHAR, rankid + 1, 0, MPI_COMM_WORLD, &reqs);
        printf("Process %d: Received array, sending it to process %d\n", rankid, rankid + 1);
    } else if (rankid == numranks - 1) {
        // Last node receives the message
        MPI_Irecv(buffer_A, BUFFER_SIZE, MPI_CHAR, rankid - 1, 0, MPI_COMM_WORLD, &reqr);
        MPI_Wait(&reqr, &stat1); // Wait for receiving the message before printing
        printf("Last process (Nr. %d) received array:\n", rankid);
        printf("%s", buffer_A);
    }

    MPI_Finalize();

    return 0;
}

