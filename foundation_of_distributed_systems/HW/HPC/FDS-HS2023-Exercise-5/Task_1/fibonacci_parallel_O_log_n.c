#include <stdio.h>
#include <omp.h>

#define MAX_ROWS_COLS 2

// Define a matrix structure
typedef struct {
    long long mat[MAX_ROWS_COLS][MAX_ROWS_COLS];
} Matrix;

// Multiply two matrices
Matrix matrixMultiply(const Matrix *A, const Matrix *B) {
    Matrix result;

    // Matrix multiplication for 2x2 matrices
    result.mat[0][0] = A->mat[0][0] * B->mat[0][0] + A->mat[0][1] * B->mat[1][0];
    result.mat[0][1] = A->mat[0][0] * B->mat[0][1] + A->mat[0][1] * B->mat[1][1];
    result.mat[1][0] = A->mat[1][0] * B->mat[0][0] + A->mat[1][1] * B->mat[1][0];
    result.mat[1][1] = A->mat[1][0] * B->mat[0][1] + A->mat[1][1] * B->mat[1][1];

    return result;
}

// Perform matrix exponentiation
Matrix matrixPower(const Matrix *M, int n) {
    if (n == 1)
        return *M;

    Matrix half_power, result;

    if (n % 2 == 0) {
        // If n is even, compute half the power and square it
        half_power = matrixPower(M, n / 2);
        result = matrixMultiply(&half_power, &half_power);
    } else {
        // If n is odd, compute half the power, square it, and multiply by M once more
        half_power = matrixPower(M, (n - 1) / 2);
        result = matrixMultiply(&half_power, &half_power);
        result = matrixMultiply(&result, M);
    }

    return result;
}

// Calculate nth Fibonacci number using matrix exponentiation
long long fibonacci(int n) {
    if (n <= 0) return 0;
    if (n == 1 || n == 2) return 1;

    Matrix base = { {{1, 1}, {1, 0}} }; // Fibonacci transformation matrix
    Matrix result = matrixPower(&base, n - 2); // Compute matrix exponentiation

    return result.mat[0][0] + result.mat[0][1]; // Extract the Fibonacci number
}

int main() {
    int n = 10; // Number of terms in the sequence
    printf("Parallel Fibonacci Sequence using Matrix Exponentiation: ");
    
    // Parallel loop using OpenMP to compute Fibonacci sequence up to nth term
    #pragma omp parallel for
    for (int i = 1; i <= n; ++i) {
        printf("%lld ", fibonacci(i)); // Calculate and print Fibonacci numbers
    }
    printf("\n");

    return 0;
}

