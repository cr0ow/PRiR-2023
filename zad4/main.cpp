#include "Force.h"
#include "MyForce.h"
#include "Simulation.h"
#include "DataSupplier.h"
#include "SimpleDataSupplier.h"
#include "SequentialSimulation.h"

#include <iostream>
#include <ctime>
#include <iomanip>

using namespace std;

constexpr int HISTOGRAM_SIZE = 20;
constexpr double HISTOGRAM_LENGTH_PER_BIN = 0.2;
constexpr double DT = 0.02;
constexpr double DISTANCE = 1.0;
constexpr double MASS = 1.0;
constexpr int STEPS = 5000;
constexpr int REPORT_PERIOD = 500;
constexpr int PARTICLES_SQRT = 20;

void showReport(int step, Simulation *s, double *v, time_t time);

int main() {

	auto *v_s = new double[HISTOGRAM_SIZE];
	auto *v_p = new double[HISTOGRAM_SIZE];
	Force *force = new MyForce();
	DataSupplier *supplier = new SimpleDataSupplier(PARTICLES_SQRT, DISTANCE, MASS);

	supplier->initializeData();

	auto *simulation_s = new SequentialSimulation(force, DT, true);
    auto *simulation_p = new Simulation(force, DT, true);
    simulation_s->initialize(supplier);
    simulation_p->initialize(supplier);

    time_t start = time(nullptr);
    printf("--------------------------\n");
    printf("--------SEQUENTIAL--------\n");
    printf("--------------------------\n");
    for (int step = 0; step < STEPS; step++) {
//        if (step % REPORT_PERIOD == 0) {
//            showReport(step, reinterpret_cast<Simulation *>(simulation_s), v_s, time(nullptr) - start);
//        }
        simulation_s->step();
	}
    time_t end = time(nullptr);
	showReport(STEPS, reinterpret_cast<Simulation *>(simulation_s), v_s, end - start);

    start = time(nullptr);
    printf("\n--------------------------\n");
    printf("---------PARALLEL---------\n");
    printf("--------------------------\n");
    for (int step = 0; step < STEPS; step++) {
//        if (step % REPORT_PERIOD == 0) {
//            showReport(step, simulation_p, v_p, time(nullptr) - start);
//        }
        simulation_p->step();
    }
    end = time(nullptr);
    showReport(STEPS, simulation_p, v_p, end - start);

    cout << endl << "--------------------------" << endl;
    cout << "Summary:" << endl << endl;
    bool ok = true;
    int errors = 0;
    for(int i = 0; i < HISTOGRAM_SIZE; i++) {
        if(v_s[i] != v_p[i]) {
            cout << "v[" << i << "]: " << v_s[i] << " != " << v_p[i] << endl;
            errors++;
        }
    }
    if(simulation_p->avgMinDistance() != simulation_s->avgMinDistance()) {
        cout << "avgMinDistance(): " << setprecision(20) << simulation_s->avgMinDistance() << " != "  << simulation_p->avgMinDistance() << endl;
        errors++;
    }
    if(simulation_p->Ekin() != simulation_s->Ekin()) {
        cout << "Ekin(): " << setprecision(20) << simulation_s->Ekin() << " != " << simulation_p->Ekin() << endl;
        errors++;
    }

    cout << endl;
    if(!errors) {
        cout << "> OK <" << endl;
    }
    else {
        cout << "> ERRORS: " << errors << " <" << endl;
    }
    cout << "--------------------------" << endl;
}

void showReport(int step, Simulation *s, double *v, time_t time) {
    s->pairDistribution(v, HISTOGRAM_SIZE, HISTOGRAM_LENGTH_PER_BIN);
    cout << "Step: " << step << "\nEkin = " << s->Ekin()
         << "\n<min(NNdistance)> = " << s->avgMinDistance() << endl;
    for (int j = 0; j < HISTOGRAM_SIZE; j++) {
        cout << "v[" << j << "] = " << v[j] << endl;
    }
    cout << "Elapsed time: " << time << "s" << endl;
}