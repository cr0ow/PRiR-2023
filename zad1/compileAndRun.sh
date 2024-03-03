#!/bin/bash

mpic++ -O2 *.cpp -o main.out && mpirun -n $1 main.out
