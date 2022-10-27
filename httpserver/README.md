# CURL

* curl --request GET -v localhost:8080/status

![image](https://user-images.githubusercontent.com/33277588/198174654-17285747-9345-4991-93dd-e377e51f8745.png)

* curl --request POST -v --data '50,100' localhost:8080/task

<img width="1181" alt="image" src="https://user-images.githubusercontent.com/33277588/198173066-2ba35359-b74d-45e2-a75f-db00138ea775.png">

* curl --request POST -v --header "X-Test: true" --data '50,100' localhost:8080/task

<img width="1405" alt="image" src="https://user-images.githubusercontent.com/33277588/198173301-a85a79cc-2e72-49aa-983a-0596dbe67d5b.png">

* curl --request POST -v --header "X-Debug: true" --data '50,100' localhost:8080/tas

![image](https://user-images.githubusercontent.com/33277588/198174547-36732180-a12e-4dbc-beeb-528faf37f2c5.png)

