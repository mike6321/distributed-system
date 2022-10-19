# distributed-system
분산 시스템 및 클라우드 컴퓨팅 스터디를 위한 repository 입니다.

# zookeeper Install and run

https://downloads.apache.org/zookeeper/stable/

* cd /Users/nhn/IdeaProjects/repository/apache-zookeeper-3.8.0-bin/bin

* ./zkServer.sh

# 리더 선출 알고리즘 구현

### 노드 생성

* ./zkCli.sh
* create /election ""

### 메이븐 패키징 및 실행

* mvn clean package
* java -jar target/distributed-system-1.0-SNAPSHOT-jar-with-dependencies.jar

### 결과

<img width="2559" alt="image" src="https://user-images.githubusercontent.com/33277588/196673399-aae6fde3-1d21-44d3-ab38-f8dba6adfd0f.png">

