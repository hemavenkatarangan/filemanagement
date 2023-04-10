FROM maven:3.6.0-jdk-11-slim
RUN mkdir /home/filemanagement
COPY . /home/filemanagement
RUN cd /home/filemanagement
WORKDIR /home/filemanagement
RUN ls -l
RUN mvn clean
RUN mvn install
EXPOSE 8080
ENTRYPOINT ["mvn","spring-boot:run"]



