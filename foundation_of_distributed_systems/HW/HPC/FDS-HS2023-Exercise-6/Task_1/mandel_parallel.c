/**********************************************************************************                                                          *
 * Ali Mohammed  <ali.mohammed@unibas.ch>                                         *                                                  *
 * All rights reserved.                                                           *
 *           ------------------------------------------------------------         *
 * Summary of changes                                                             *
 * ==================                                                             *
 * 1. Change the size of the image (pixels) as an option                          *
 * 2. Save output to csv file to be visualized later                              *
 * 3. Aded calculate_pixel and finalize functions                                  *
 * 4. Calculate Z^4 + C instead of Z^2 + C                                        *
 **********************************************************************************
 *
 * Mandelbrot program 
 *
 * This program computes and displays all or part of the Mandelbrot 
 * set.  By default, it examines all points in the complex plane
 * that have both real and imaginary parts between -2 and 2.  
 * Command-line parameters allow zooming in on a specific part of
 * this range.
 * 
 * Usage:
 *   mandelbrot maxiter [pixels x0 y0 size] 
 * where 
 *   maxiter denotes the maximum number of iterations at each point
 *   x0, y0, and size specify the range to examine (a square 
 *     centered at x0 + iy0 of size 2*size by 2*size -- by default, 
 *     a square of size 4 by 4 centered at the origin)
 * 
 * Input:  none, except the optional command-line arguments
 * Output: A comma separated file [mandel_data.csv] contains the values at each pixel in the format
 * x, y, value
 *
 * The output can be visualized with visualize.py python script
 *
 */


#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>
#include <unistd.h>

#include <time.h>

#include <omp.h>
#include <mpi.h>




/* Default values for things. */
#define N           2           /* size of problem space (x, y from -N to N) */

#define min_color 0 
#define max_color 16777215
 
#define DATAOWNER 0



/* Structure definition for complex numbers */
typedef struct {
    double real, imag;
} complex;

/* ---- Function declarations ---- */
void calculate_pixel(int width, int maxiter, long *data_msg, double real_min, double real_max, double imag_min, double imag_max, double scale_real, double scale_imag, double scale_color, int start_pixel, int end_pixel);

void finalize(FILE *fp, long *data_msg,int width, int nworkers, int maxiter, double time, double x, double y, double size);

void get_time(struct timespec* t) {
		clock_gettime(CLOCK_MONOTONIC, t);
	}
	void get_clockres(struct timespec* t) {
		clock_getres(CLOCK_MONOTONIC, t);
	}


/* ---- Main program ---- */
/* ---- Here you will initialize MPI and figure out how to divide the work ---- */
int main (int argc, char *argv[]) {

    
    int nprocs;
    int rank;
  
    int returnval;
    int maxiter;
    double real_min = -N;
    double real_max = N;
    double imag_min = -N;
    double imag_max = N;
    long *data_msg;
    long *local_data_msg;
    
    FILE *fp;
    double scale_real, scale_imag, scale_color;
    double Tpar, tpar1, tpar2;
    double size  = 2;
    double x0 = 0;
    double y0 = 0;

    int NPIXELS = 1024;
    int width;         /* dimensions of display window */
    int height;

    int start,end;
    struct timespec t1, t2, dt;

   
    /* Process command-line arguments */
    if(argc<2)
	{
	    printf("usage: %s  maxiter pixels x0 y0 size \n", argv[0]);
            return 0;
		
	}
    maxiter = atoi(argv[1]);

    if(argc >2)
    {
      NPIXELS = atoi(argv[2]);
    }
    
    if(argc > 3)
    {
      x0 = atof(argv[3]);
    }
    if(argc >4)
    {
      y0 = atof(argv[4]);
    }
    if(argc > 5)
    {
      size = atof(argv[5]);
    }


   real_min = x0 - size;
   real_max = x0 + size;
   imag_min = y0 - size;
   imag_max = y0 + size;
 
   width = height = NPIXELS;
 
  /* Compute factors to scale computational region to window */
    scale_real = (double) (real_max - real_min) / (double) width;
    scale_imag = (double) (imag_max - imag_min) / (double) width;

   /* Compute factor for color scaling */
   scale_color = (double) (max_color - min_color) / (double) (maxiter - 1);

   get_clockres(&t1);  
   get_time(&t1); // Record the starting time
   
   MPI_Init(&argc, &argv); // Initialize MPI environment
   MPI_Comm_size(MPI_COMM_WORLD, &nprocs); // Get the number of processes
   MPI_Comm_rank(MPI_COMM_WORLD, &rank); // Get the rank of the current process

// Calculate the number of pixels each process will handle
int pix_per_procs = ceil(width * width / nprocs);
start = rank * pix_per_procs; // Calculate the starting pixel index for the current process
end = (rank + 1) * pix_per_procs; // Calculate the ending pixel index for the current process

	// Ensure the last process gets the remaining pixels
	if (rank == nprocs - 1) {
	    end = width * width;
	}

	// Allocate memory for the main data array and open a file for writing
	if (rank == 0) {
	    data_msg = malloc(width * width * sizeof(long));
	    fp = fopen("mandel_data.csv", "w");
	}

	// Allocate memory for the local data array for each process
	local_data_msg = malloc(pix_per_procs * sizeof(long));

	// Calculate pixels for the current process
	calculate_pixel(width, maxiter, local_data_msg, real_min, real_max, imag_min, imag_max, scale_real, scale_imag, scale_color, start, end);

	// Gather local pixel data from all processes to the root (rank 0) process
	MPI_Gather(local_data_msg, pix_per_procs, MPI_LONG, data_msg, pix_per_procs, MPI_LONG, 0, MPI_COMM_WORLD);

	get_time(&t2); // Record the ending time

	// Calculate the elapsed time
	if ((t2.tv_nsec - t1.tv_nsec) < 0) {
	    dt.tv_sec = t2.tv_sec - t1.tv_sec - 1;
	    dt.tv_nsec = 1000000000 - t1.tv_nsec + t2.tv_nsec;
	} else {
	    dt.tv_sec = t2.tv_sec - t1.tv_sec;
	    dt.tv_nsec = t2.tv_nsec - t1.tv_nsec;
	}

	double time = 0;
	time += dt.tv_sec + (double)(dt.tv_nsec) * 0.000000001;

	// Perform final operations on the root process
	if (rank == 0) {
	    finalize(fp, data_msg, width, nprocs, maxiter, time, x0, y0, size);
	    free(data_msg);
	}

	free(local_data_msg); // Free memory allocated for local data

	MPI_Finalize(); // Finalize MPI environment

	return 0;
	}


void finalize(FILE *fp, long *data_msg,int width, int nworkers, int maxiter, double time,  double x, double y, double size)

{

    int col;
    int this_row;
    
 
    //write output
    for(this_row = 0; this_row < width; this_row++) {
        for ( col = 0; col < width; ++col) 
        {
            fprintf(fp,"%d, %d, %ld\n",this_row, col, data_msg[this_row*width+col]);
        }
     }

    fclose(fp);
    /* Produce text output  */
    fprintf(stdout, "\n");
    fprintf(stdout, "Sequential Mandelbrot program\n");
    fprintf(stdout, "Workers, %d\n", nworkers);
    fprintf(stdout, "Maximum iterations, %d\n", maxiter);
    fprintf(stdout, "x0, %lf,y0, %lf, size, %lf\n",x, y, size);
    fprintf(stdout, "Number of pixels, %d * %d =, %d\n", width, width, width*width);
   
    fprintf(stdout,"Program time, %lf\n", time);
  
    fprintf(stdout, "\n");
        
}

/*
The following function do the processing and you can figure out how to parallelized it with OpenMP. 
*/

void calculate_pixel(int width, int maxiter, long *data_msg, double real_min, double real_max, double imag_min, double imag_max, double scale_real, double scale_imag, double scale_color, int start_pixel, int end_pixel)
{
 int pixel;
 complex z, c;
 int the_row,col;

 #pragma omp parallel for private(z, c, the_row, col)
 for(pixel = start_pixel; pixel < end_pixel; pixel++)
 {
    z.real = z.imag = 0;
    the_row = pixel/width;
    col = pixel%width;

            /* Scale display coordinates to actual region */
            c.real = real_min + ((double) col * scale_real);
            c.imag = (imag_min + ((double) (width-1-the_row) * scale_imag));
                                        /* height-1-the_row so y axis displays
                                         * with larger values at top
                                         */

            /* Calculate z0, z1, .... until divergence or maximum iterations */
            int k = 0;
            double lengthsq, temp;
            do  {
                temp = z.real*z.real*z.real*z.real - 6*z.imag*z.imag*z.real*z.real +z.imag*z.imag*z.imag*z.imag+ c.real;
                z.imag = 4*z.real*z.real*z.real*z.imag -4*z.real*z.imag*z.imag*z.imag+ c.imag;
                z.real = temp;
                //z = cpow(z,4) + c;
                lengthsq = z.real*z.real + z.imag*z.imag;
                //lengthsq = cabs(z);
                ++k;
            } while (lengthsq < (N*N) && k < maxiter);

            /* Scale color and store */
            long color = (long) ((k-1) * scale_color) + min_color;
            data_msg[the_row*width+col-start_pixel] = color;
 
 }

}
