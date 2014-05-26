

Java Client of User Profile Access Tool

1) Start ROLE SDK

2) Start Client
  
  java -cp umaccess.jar;gson-1.7.1.jar at.tugraz.kmi.role.parser.main.Main
  
3) Access Information of a User Profile (testuser in this case)

Enter the resources on the command line:

  http://127.0.0.1:8073/users/rest:info
  http://127.0.0.1:8073/users/testuser/rest:info