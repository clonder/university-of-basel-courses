#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <math.h>
#include <time.h>
#include <ctype.h>


// In this exercise you need to paralelize the function sine //


void get_time(struct timespec* t) {
	clock_gettime(CLOCK_MONOTONIC, t);
}
void get_clockres(struct timespec* t) {
	clock_getres(CLOCK_MONOTONIC, t);
}

void sine(double *a, int N);


int main(int argc, char * argv[]) {
    struct timespec t1, t2, dt;
	double time = 0.0;
	int N;
	if (argc == 2 && isdigit(argv[1][0])) {
        N = atoi(argv[1]);
        
    }else {
        N=100000000;
    }

	double* a=(double*) malloc (N*sizeof(double));
	
	get_time(&t1);


	sine(a,N);
	
	
	get_time(&t2);

    if ((t2.tv_nsec - t1.tv_nsec) < 0) {
        dt.tv_sec = t2.tv_sec - t1.tv_sec - 1;
        dt.tv_nsec = 1000000000 - t1.tv_nsec + t2.tv_nsec;
    }else {
        dt.tv_sec = t2.tv_sec - t1.tv_sec;
        dt.tv_nsec = t2.tv_nsec - t1.tv_nsec;
    }

    time = dt.tv_sec + (double)(dt.tv_nsec)*0.000000001;

	printf("sines; %d; %d; %f\n",N,omp_get_max_threads(),time);

	free(a);
	return 0;
}

void sine(double *a, int N){

	int i;
	//Divides the work so that every thread has to do for N/number loop passes
	#pragma omp parallel for
	for(i=0;i<N;i++){
		a[i]=sin(2.0*M_PI/1000.0*i);
	}
}
