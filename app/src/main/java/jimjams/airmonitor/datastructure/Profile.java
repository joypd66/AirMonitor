package jimjams.airmonitor.datastructure;

import android.database.sqlite.SQLiteFullException;

import java.util.ArrayList;

import jimjams.airmonitor.database.DBAccess;

/**
 * User profile.
 */
public class Profile {
   /**
    * The user's ID number. Initially this is set to 0; when the profile is first uploaded to the
    * nonlocal database, a new, unique id is assigned.
    */
   private long id;

   /**
    * Existing conditions
    */
   private ArrayList<String> conditions;

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
      }
      catch(SQLiteFullException sqlfe) {
         id = 0;
         conditions = new ArrayList<>();
      }
      // Log.d(className, toString());
   }

   /**
    * Gets the current user profile.
    * @return The current user profile
    */
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

      if(!duplicate && !empty) {
         conditions.add(condition);

         // Update database
         DBAccess.getDBAccess().updateProfile();
      }
   }

   /**
    * Attempts to remove the specified condition from the Profile.
    * @param condition The condition to be removed
    */
   public void removeCondition(String condition) {
      if(conditions.remove(condition)) {
         DBAccess.getDBAccess().updateProfile();
      }
   }

   /**
    * Returns the List of existing conditions.
    * @return List of existing conditions
    */
   public ArrayList<String> getConditions() {
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