# Update system and install initial dependencies
sudo apt-get update
sudo apt-get install -y wget apt-transport-https gnupg curl vim

# Configure the Adoptium repository for JDK installation
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -sc) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt-get update
sudo apt-get install -y temurin-17-jdk

# Download and install sbt (Scala Build Tool)
mkdir -p ~/working/
cd ~/working/
curl -L -o sbt-1.9.7.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.9.7.deb
sudo dpkg -i sbt-1.9.7.deb
rm sbt-1.9.7.deb
sudo apt-get update
sudo apt-get install sbt -y
cd
rm -r ~/working/
sbt sbtVersion

# Install Homebrew package manager
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
echo 'eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"' >>~/.profile
eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"

# Install Verilator using Homebrew
brew install verilator
