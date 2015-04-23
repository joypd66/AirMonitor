A couple quick updates to fix crashes.

Added a dummy on_MainScreen_refresh_button_Click(View v) method to the
MainActivity. The refresh button was trying to invoke this method,
which had been removed, and this was crashing the app. Ultimately the
refresh buttonneeds to be removed, but this will affect the layout of
the main screen and I don't want to get into that.

Also, the SensorDataGenerator was crashing when trying to parse an
empty string in the getData(String sensorData) method because there is
no data to process. Added a try/catch which just does nothing with an
empty String array. The method should still return an ArrayList, but it
will have zero elements. Ultimately we need to find a more graceful
way of handling the lack of an Arduino in the app.