#include <stdio.h>
#include <omp.h>


static const int a[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};


int sum(const int* arr, size_t n) {
  int s = 0; // Initialize sum variable

  // OpenMP directive: Create a parallel region to execute the following loop in parallel
  // 'reduction(+:s)' indicates the reduction operation, summing up values to 's' in a thread-safe manner
  #pragma omp parallel for reduction(+:s)
  for (size_t i = 0; i < n; ++i) {
    s += arr[i]; // Add each element of the array to the local sum 's' (private to each thread)
  }
  // After parallel execution, the individual 's' values from each thread are combined to a final sum

  return s;

int main() {
  printf("Array Sum: %d\n", sum(a, 10));
  return 0;
}

