#ifndef LIFEPARALLELIMPLEMENTATION_H_
#define LIFEPARALLELIMPLEMENTATION_H_

#include "Life.h"
#include <cstdlib>
#include <mpi.h>
#include <cmath>

class LifeParallelImplementation: public Life {
private:
    int min;
    int max;
    int interval;
    int myInterval;
    int myRank;
    int processes;
protected:
    void realStep();
    void calculate();
    void send();
public:
    LifeParallelImplementation();
    void beforeFirstStep();
    void afterLastStep();
    int numberOfLivingCells();
    double averagePollution();
    int getMyInterval(int);
    void oneStep();
};

#endif