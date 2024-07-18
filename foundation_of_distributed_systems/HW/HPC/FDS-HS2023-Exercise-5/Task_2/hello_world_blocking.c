#include <stdio.h>
#include <mpi.h>
#include <string.h>

#define MAX_LEN 20 // Maximum length of the buffer

int main(int argc, char *argv[]) {
    int numranks, rankid; // Variables to store the number of processes and current process rank
    char buffer_A[MAX_LEN]; // Buffer to hold the message

    MPI_Init(NULL, NULL); // Initialize MPI environment
    MPI_Comm_size(MPI_COMM_WORLD, &numranks); // Get the total number of processes
    MPI_Comm_rank(MPI_COMM_WORLD, &rankid); // Get the rank of the current process

    if (numranks > 1) { // Check if there's more than one process
        if (rankid == 0) { // For the first process (rank 0)
            strcpy(buffer_A, "Hello World\n"); // Copy the message to the buffer
            printf("Process %d sending array to process %d\n", rankid, rankid + 1);
            MPI_Send(buffer_A, MAX_LEN, MPI_CHAR, rankid + 1, 0, MPI_COMM_WORLD); // Send the message to the next process
        } else if (rankid < numranks - 1) { // For processes in between the first and last
            MPI_Recv(buffer_A, MAX_LEN, MPI_CHAR, rankid - 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE); // Receive the message from the previous process
            printf("Process %d received array and sending it to process %d\n", rankid, rankid + 1);
            MPI_Send(buffer_A, MAX_LEN, MPI_CHAR, rankid + 1, 0, MPI_COMM_WORLD); // Send the received message to the next process
        } else { // For the last process
            MPI_Recv(buffer_A, MAX_LEN, MPI_CHAR, rankid - 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE); // Receive the message from the previous process
            printf("Last process (Nr. %d) received Array.\n", rankid);
            printf("%s", buffer_A);
        }
    }

    MPI_Finalize();
    return 0;
}

