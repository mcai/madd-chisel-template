# README.md

Single-cycle and multi-cycle matrix addition samples.

# File Structure

src/main/scala/madd/MatrixAddition1*.scala - Single-Cycle matrix addition sample in Chisel 3
src/main/scala/madd/MatrixAddition2*.scala - Multi-Cycle matrix addition sample in Chisel 3

# Usage

Step 1. Install Docker on your local machine.

Step 2. Run `./docker_build.sh` to build the Docker image.

Step 3. `./docker_run.sh` to run the Docker container.

Step 4. Use Visual Studio Code Docker Extension to attach to the Docker container created in Step 3. 

Step 5. Inside the container, run `./test.sh` to run sample test for single cycle and multi-cycle matrix addition samples.

Step 6. Implement single-cycle and multi-cycle matrix multiplication in Chisel 3.