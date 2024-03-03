#include "LifeParallelImplementation.h"

LifeParallelImplementation::LifeParallelImplementation() { }

void LifeParallelImplementation::beforeFirstStep() {
    MPI_Comm_size(MPI_COMM_WORLD, &processes);
    MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

    interval = ceil((double)(size_1 - 1) / processes);
    myInterval = getMyInterval(myRank);
    min = myRank * interval + 1;
    max = min + myInterval;

    for(int row = 1; row < size; row++) {
        MPI_Bcast(cells[row], size, MPI_INT, 0, MPI_COMM_WORLD);
        MPI_Bcast(pollution[row], size, MPI_INT, 0, MPI_COMM_WORLD);
    }
}

void LifeParallelImplementation::afterLastStep() {
    if(myRank) {
        for(int row = min; row < max; row++) {
            MPI_Send(cells[row], size, MPI_INT, 0, 0, MPI_COMM_WORLD);
            MPI_Send(pollution[row], size, MPI_INT, 0, 1, MPI_COMM_WORLD);
        }
    }
    else {
        MPI_Status* status = new MPI_Status();
        for(int rank = 1; rank < processes; rank++) {
            int localMin = rank * interval + 1;
            int localMax = localMin + getMyInterval(rank);
            for(int row = localMin; row < localMax; row++) {
                MPI_Recv(cells[row], size, MPI_INT, rank, 0, MPI_COMM_WORLD, status);
                MPI_Recv(pollution[row], size, MPI_INT, rank, 1, MPI_COMM_WORLD, status);
            }
        }
    }
}

void LifeParallelImplementation::calculate() {
    int currentState, currentPollution;
    for(int row = min; row < max; row++) {
        for(int col = 1; col < size_1; col++) {
            currentState = cells[row][col];
            currentPollution = pollution[row][col];
            cellsNext[row][col] = rules->cellNextState(currentState, liveNeighbours(row, col), currentPollution);

            pollutionNext[row][col] =
                    rules->nextPollution(currentState, currentPollution, pollution[row + 1][col] + pollution[row - 1][col] + pollution[row][col - 1] + pollution[row][col + 1],
                                         pollution[row - 1][col - 1] + pollution[row - 1][col + 1] + pollution[row + 1][col - 1] + pollution[row + 1][col + 1]);
        }
    }
}

void LifeParallelImplementation::send() {
    MPI_Status* status = new MPI_Status();
    if(myRank < processes - 1) {
        int row = max - 1;
        MPI_Sendrecv(cellsNext[row], size, MPI_INT, myRank + 1, 0, cellsNext[row + 1], 1 * size, MPI_INT, myRank + 1, 0, MPI_COMM_WORLD, status);
        MPI_Sendrecv(pollutionNext[row], size, MPI_INT, myRank + 1, 1, pollutionNext[row + 1], size, MPI_INT, myRank + 1, 1, MPI_COMM_WORLD, status);
    }
    if(myRank) {
        int row = min;
        MPI_Sendrecv(cellsNext[row], size, MPI_INT, myRank - 1, 0, cellsNext[row - 1], size, MPI_INT, myRank - 1, 0, MPI_COMM_WORLD, status);
        MPI_Sendrecv(pollutionNext[row], size, MPI_INT, myRank - 1, 1, pollutionNext[row - 1], size, MPI_INT, myRank - 1, 1, MPI_COMM_WORLD, status);
    }
}

void LifeParallelImplementation::oneStep() {
    realStep();
    swapTables();
}

void LifeParallelImplementation::realStep() {
    calculate();
    send();
}

int LifeParallelImplementation::getMyInterval(int rank) {
    return rank == processes - 1 && (size_1 - 1) % interval ? size_1 - (interval * (processes - 1)) - 1 : interval;
}

int LifeParallelImplementation::numberOfLivingCells() {
    return sumTable(cells);
}

double LifeParallelImplementation::averagePollution() {
    return (double)sumTable(pollution)/size_1_squared/rules->getMaxPollution();
}