#include <stdio.h>
#include <omp.h>

//function to calculate the fibonacci number
int fib(int n) {
  if (n <= 1 ) {
    return n;
  } else {

    int fib1, fib2;
    //two sections that can be run in parallele
    #pragma omp parallel sections
    {
      //calculate n-1 fibonacci number
      #pragma omp section
      fib1 = fib(n-1);

      //calculate n-2 fibonacci number
      #pragma omp section
      fib2 = fib(n-2);
    }

    //add them together
    return fib1 + fib2;
  }
}

void fibonacci( size_t n)
{
  int fib_array[n];

  //calculate here the the fibonacci array with the use uf the fib function
  //also here you can distribute the work
  #pragma omp parallel for
  for(size_t i=0; i <= n; ++i) {
    fib_array[i] = fib(i);
  }

  printf("Calculated sequence: ");
  for(int j = 0; j <= n; j++){
    printf("%d ", fib_array[j]);
  }

  printf("\n");
  return;
}

int main()
{
  int size = 11;
  printf("Should Calculate the following sequence: 1,1,2,3,5,8,13,21,34,55,89 \n");
  fibonacci(size);
  
  return 0;
}
