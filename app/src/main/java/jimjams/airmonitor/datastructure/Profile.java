package jimjams.airmonitor.datastructure;

import android.database.sqlite.SQLiteFullException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jimjams.airmonitor.database.DBAccess;

/**
 * User profile.
 */
public class Profile {

   /**
    * Used to identify source class for log
    */
   private String className = getClass().getSimpleName();

   /**
    * The user's ID number. Initially this is set to 0; when the profile is first uploaded to the
    * nonlocal database, a new, unique id is assigned.
    */
   private long id;

   /**
    * Existing conditions
    */
   private List<String> conditions;

   /**
    * Current instance of Profile
    */
   private static Profile profile = null;

   /**
    * Constructor. Will attempt to load a Profile from the database; otherwise an empty Profile with
    * id 0 will be generated.
    */
   private Profile() {
      DBAccess access = DBAccess.getDBAccess();
      try {
         id = access.getProfileId();
         conditions = access.getProfileConditions();
         // Log.d(className, "Constructor: database accessed.");
      }
      catch(SQLiteFullException sqlfe) {
         id = 0;
         conditions = new ArrayList<>();
         Log.w(className, "Too many Profiles in database; generating empty Profile.");
      }
      // Log.d(className, toString());
   }

   public static Profile getProfile() {
      if(profile == null) {
         profile = new Profile();
      }
      return profile;
   }

   /**
    * Attempts to add a new condition to the user's Profile. Will not add a duplicate condition.
    * @param condition The condition to be added
    */
   public void addCondition(String condition) {
      boolean duplicate = false;
      boolean empty = false;
      if(condition.trim().length() == 0) {
         empty = true;
      }
      for(String existing: conditions) {
         if(existing.equalsIgnoreCase(condition)) {
            duplicate = true;
         }
      }

      if(duplicate) {
         // Log.d(className, "Failed to add " + condition + " (duplicate).");
      }
      else if(empty) {
         // Log.d(className, "Failed to add " + condition + " (empty).");
      }
      else {
         conditions.add(condition);
         // Log.d(className, "Adding " + condition + ".");

         // Update database
         DBAccess.getDBAccess().updateProfile();
      }
      // Log.d(className, toString());
   }

   /**
    * Attempts to remove the specified condition from the Profile.
    * @param condition The condition to be removed
    */
   public void removeCondition(String condition) {
      if(conditions.remove(condition)) {
         // Log.d(className, "Removing " + condition + ".");
         DBAccess.getDBAccess().updateProfile();
      }
      else {
         // Log.d(className, "Failed to remove " + condition + ".");
      }
      // Log.d(className, toString());
   }

   /**
    * Returns the List of existing conditions.
    * @return List of existing conditions
    */
   public List<String> getConditions() {
      return conditions;
   }

   /**
    * Returns the profile's id.
    * @return The profile's id
    */
   public long getId() {
      return id;
   }

   /**
    * Returns a String representation of the Profile.
    * @return A String representation of the Profile
    */
   @Override
   public String toString() {
      String result = "id=" + id + "; conditions=";
      if(conditions.size() == 0) {
         result += "none";
      }
      else {
         for(int i = 0; i < conditions.size(); i++) {
            if(i != 0) {
               result += ", ";
            }
            result += conditions.get(i);
         }
      }
      return result;
   }
}