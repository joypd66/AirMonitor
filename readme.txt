Updates to generate, store, retrieve, and display location data.

A Location is now generated when a Snapshot is taken, and its provider,
latitude, longitude, altitude, and accuracy are stored in the database.
These are also used to generate and populate a new Location object when
the data is retrieved from the database, and the latitude and longitude
are displayed on the history screen.

NOTE: This update modifies the database structure. I'm not sure exactly
what will happen if you try to load data from a database predating this
update (I think it should be ignored, but not 100% sure). If you run
into problems, try clearing the app data on your phone.