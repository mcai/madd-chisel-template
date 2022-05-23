docker stop $(docker ps -a --filter name=madd-chisel-template -q )
docker container rm madd-chisel-template
docker container run -dt --privileged --name madd-chisel-template itecgo2021/madd-chisel-template:latest