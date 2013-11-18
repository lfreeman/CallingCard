CallingCard
===========
In case dialed number starts with + symbol remove it. 
Add calling card access code and international prefix (like 011).
Insert the original number into CallLog. 

To compile and deploy, run:
    cd callingcard
    mvn clean install android:deploy
    
To run from eclipse
   Import as Existing maven project
   
Tests
======
Tests are implemented using the Appium
APK file should be located in callingcard_test/app

CallingCardTest - local Appium server
CallingCardTestSauceLab - running on Saucelabs

Enter correct credentials into config.properties file
APK file will be compressed and uploaded to Saucelabs storage as 
see https://saucelabs.com/appium/tutorial/3   (Appium for Android on Sauce Labs)

To compile and run all tests, run:
    cd callingcard_test
    mvn test
    
To run a single test, run:
    cd callingcard_test
    mvn -Dtest=org.lfreeman.callingcard.CallingCardTestSauceLab test
    
To run from eclipse
   Import as Existing maven project

