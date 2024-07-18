#include <stdio.h>
//#include <omp.h>



void fibonacci( size_t n)
{
  int fib_array[n];
  //Initialize start values
  fib_array[0] = 1;
  fib_array[1] = 1;

  for(size_t i=2; i < n; ++i) {
    fib_array[i] = fib_array[i-1] + fib_array[i-2];
  }

  printf("Calculated sequence: ");
  for(int j = 0; j < n; j++){
    printf("%d ", fib_array[j]);
  }

  printf("\n");
  return;
}

int
main()
{
  int size = 11;
  printf("Should Calculate the following sequence: 1,1,2,3,5,8,13,21,34,55,89 \n");
  fibonacci(size);
  
  return 0;
}
