FROM ubuntu:bionic-20200311

ENV DEBIAN_FRONTEND=noninteractive

ENV SDK_VERSION=sdk-tools-linux-3859397 \
    ANDROID_BUILD_TOOLS_VERSION=30.0.3

#=============
# Set WORKDIR
#=============
WORKDIR /root

#==================
# General Packages
#------------------
# openjdk-8-jdk
#   Java
# ca-certificates
#   SSL client
# tzdata
#   Timezone
# zip
#   Make a zip file
# unzip
#   Unzip zip file
# curl
#   Transfer data from or to a server
# wget
#   Network downloader
#==================
RUN apt-get -qqy update && \
    apt-get -qqy --no-install-recommends install \
    openjdk-8-jdk \
    ca-certificates \
    tzdata \
    zip \
    unzip \
    curl \
    aapt \
    wget \
    xvfb \
  && rm -rf /var/lib/apt/lists/*

#===============
# Set JAVA_HOME
#===============
ENV JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/jre" \
    PATH=$PATH:$JAVA_HOME/bin

#=====================
# Install Android SDK
#=====================
ARG ANDROID_PLATFORM_VERSION="android-25"
ENV ANDROID_HOME=/root

RUN wget -O tools.zip https://dl.google.com/android/repository/${SDK_VERSION}.zip && \
    unzip tools.zip && rm tools.zip && \
    chmod a+x -R $ANDROID_HOME && \
    chown -R root:root $ANDROID_HOME

ENV PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin

# https://askubuntu.com/questions/885658/android-sdk-repositories-cfg-could-not-be-loaded
RUN mkdir -p ~/.android && \
    touch ~/.android/repositories.cfg && \
    echo y | sdkmanager "platform-tools" && \
    echo y | sdkmanager "build-tools;$ANDROID_BUILD_TOOLS_VERSION" && \
    echo y | sdkmanager "platforms;$ANDROID_PLATFORM_VERSION"

ENV PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools


#==================================
# Fix Issue with timezone mismatch
#==================================
ENV TZ="US/Pacific"
RUN echo "${TZ}" > /etc/timezone


COPY . /root/Navitas-Framework
RUN ls ./Navitas-Framework

RUN cd / && \
    cd ./root/Navitas-Framework && \
    chmod +x ./connect.sh && \
    chmod +x ./entry_point.sh && \
    chmod +x ./app.sh && \
    cd ./NaviProf && ./gradlew publishToMavenLocal && \
    cd ../NaviTests

CMD connect.sh && entry_point.sh

# If you want configure instructions manually:
# RUN cd / && \
  #    cd ./root/Navitas-Framework && \
  #    chmod +x ./get_devices.sh && \
  #    adb connect 192.168.0.103 && \
  #    adb tcpip 5555 && \
  #    cd ./NaviProf && ./gradlew publishToMavenLocal && \
  #    cd ../NaviTests && \
  #    ./gradlew navi_test:profileBuild

