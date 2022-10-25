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

# 워처와 트리거를 활용한 시스템 장애 감지

### 노드 생성

* ./zkCli.sh
* create /target_znode "test new data"

### 노드 데이터 변경

* set /target_znode "test update data"

### 자식 노드 생성

* create /target_znode/children_znode "children node data"

### 노드삭제

*  deleteall /target_znode

# 리더 재선출 알고리즘 구현

**<img width="1920" alt="image" src="https://user-images.githubusercontent.com/33277588/196939113-dbe14d0f-68e2-4d9f-9ff1-c7eeef8b395d.png">**

* 첫 번째 노드 삭제 시 두 번째 노드가 리더
* 중간 노드 삭제 시 삭제된 뒤의 노드가 삭제된 앞의 노드와 연결

# Zookeeper를 이용한 클러스터 오토 힐러

* java -jar [path] [numberOfWorkers] [pathToWorkerProgram]

### example

java -jar /Users/nhn/IdeaProjects/repository/distributed-system/autohealer/target/autohealer-1.0-SNAPSHOT-jar-with-dependencies.jar 5 /Users/nhn/IdeaProjects/repository/distributed-system/flakyworker/target/flakyworker-1.0-SNAPSHOT-jar-with-dependencies.jar

# 서비스 레지스트리 및 서비스 디스커버리 구현

![image](https://user-images.githubusercontent.com/33277588/197769620-d489a7f2-6bee-4a58-967f-0ce2fe09fc48.png)

![image](https://user-images.githubusercontent.com/33277588/197769700-56dee9aa-fe44-40d0-9ac3-0c96dc0956d5.png)

![image](https://user-images.githubusercontent.com/33277588/197769746-64837de8-13b4-4f72-8c60-efa65531ceba.png)

