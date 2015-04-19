Updates to store current sensor data to database.

Sensor data is now stored in a database table and can be retrieved
using the DBAccess.getCurrentData() method. I had to make some changes
to the MainActivity. I duplicated a couple methods, so now there is a
beginListenForRandomData() method which starts a thread to get
randomly-generated data rather than Arduino data. There is also a
zero-argument version of SensorDataGenerator.getData() which generates
random data as well. It was also necessary to tweak this thread a
little... my screen was going blank, and it seemed to be caused by the
thread monopolizing the app's resources (I think... not really sure).
Anyway, I got rid of the Runable used to construct the workerThread and
added a sleep(1500), which both gives the app time to update the screen
and keeps the data from being refreshed more often than is useful. So
far this change is only in the beginListenForRandomData() method.