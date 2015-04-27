Updates to allow for no location.

When attempting to load a Location for a Snapshot from the database,
the app was creating a Location with lat = long = 0, rather than simply
null. This resulted in invalid coordinates being displayed on the
history screen. The saveSnapshot(Snapshot snapshot) method now stores
"null" in the "provider" column for the Location if the Location is
null, and the getSnapshot(long id) method now creates a null Location
if this is the case.