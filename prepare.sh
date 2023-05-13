sudo apt-get update && sudo apt-get install curl vim openjdk-11-jdk -y

mkdir -p ~/working/ && \
    cd ~/working/ && \
    curl -L -o sbt-1.4.9.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.4.9.deb && \
    sudo dpkg -i sbt-1.4.9.deb && \
    rm sbt-1.4.9.deb && \
    sudo apt-get update && \
    sudo apt-get install sbt -y && \
    cd && \
    rm -r ~/working/ && \
    sbt sbtVersion