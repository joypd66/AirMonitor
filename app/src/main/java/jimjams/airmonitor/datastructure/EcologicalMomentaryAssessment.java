package jimjams.airmonitor.datastructure;

import java.util.List;

/**
 * An Ecological Momentary Assessment, representing the user's personal assessment of the
 * environment at the time when a Snapshot is taken.
 */
public class EcologicalMomentaryAssessment {

   /**
    * true if the user indicates that s/he is indoors; false otherwise
    */
   private boolean indoors;

   /**
    * The user's self-reported location. Not to be confused with the Snapshot's location field.
    */
   private String reportedLocation;

   /**
    * The user's self-reported activity at the time of the Snapshot
    */
   private String activity;

   /**
    * The user's report of who s/he is with at the time of the Snapshot
    */
   private List<String> companions;

   /**
    * The user's subjective report of the current air quality, on a scale of 1 to 10
    */
   private int airQuality;

   /**
    * The user's belief that the current environment will hurt his/her health, on a scale of 1 to 10
    */
   private int belief;

   /**
    * Likelihood the user will relocate for cleaner air, on a scale of 1 to 10
    */
   private int intention;

   /**
    * true if the user has changed location for better air since the last report
    */
   private boolean behavior;

   /**
    * The user's report of what prevented him/her from relocating.
    */
   private String barrier;

   /**
    * Constructor.
    * @param indoors          true if the user indicates that s/he is indoors
    * @param reportedLocation The user's self-reported location
    * @param activity         The user's self-reported activity at the time of the Snapshot
    * @param companions       The user's report of who s/he is with at the time of the Snapshot
    * @param airQuality       The user's subjective report of the current air quality, on a scale of
    *                         1 to 10
    * @param belief           The user's belief that the current environment will hurt his/her
    *                         health
    * @param intention        Likelihood the user will relocate for cleaner air
    * @param behavior         true if the user has changed location for better air since the last
    *                         report
    * @param barrier          The user's report of what prevented him/her from relocating.
    */
   public EcologicalMomentaryAssessment(boolean indoors, String reportedLocation, String activity,
         List<String> companions, int airQuality, int belief, int intention, boolean behavior,
         String barrier) {
      this.indoors = indoors;
      this.reportedLocation = reportedLocation;
      this.activity = activity;
      this.companions = companions;
      this.airQuality = airQuality;
      this.belief = belief;
      this.intention = intention;
      this.behavior = behavior;
      this.barrier = barrier;
   }

   /**
    * Returns true if the user indicates that s/he is indoors.
    * @return true if the user indicates that s/he is indoors
    */
   public boolean getIndoors() {
      return indoors;
   }

   /**
    * Returns the user's self-reported location.
    * @return The user's self-reported location
    */
   public String getReportedLocation() {
      return reportedLocation;
   }

   /**
    * Returns the user's self-reported activity at the time of the Snapshot.
    * @return The user's self-reported activity at the time of the Snapshot
    */
   public String getActivity() {
      return activity;
   }

   /**
    * Returns the user's report of who s/he is with at the time of the Snapshot.
    * @return The user's report of who s/he is with at the time of the Snapshot
    */
   public List<String> getCompanions() {
      return companions;
   }

   /**
    * Returns the user's subjective report of the current air quality, on a scale of 1 to 10.
    * @return The user's subjective report of the current air quality
    */
   public int getAirQuality() {
      return airQuality;
   }

   /**
    * Returns the user's belief that the current environment will hurt his/her health
    * @return The user's belief that the current environment will hurt his/her health
    */
   public int getBelief() {
      return belief;
   }

   /**
    * Returns the likelihood the user will relocate for cleaner air, on a scale of 1 to 10.
    * @return The likelihood the user will relocate for cleaner air
    */
   public int getIntention() {
      return intention;
   }

   /**
    * Returns true if the user has changed location for better air since the last report.
    * @return true if the user has changed location for better air
    */
   public boolean getBehavior() {
      return behavior;
   }

   /**
    * Returns the user's report of what prevented him/her from relocating.
    * @return The user's report of what prevented him/her from relocating
    */
   public String getBarrier() {
      return barrier;
   }

   /**
    * Returns a String representation of the EMA.
    * @return A String representation of the EMA
    */
   public String toString() {
      String result = "reportedLocation: " + reportedLocation + " (" +
            (indoors ? "in" : "out") + "doors); ";
      result += "activity: " + activity + "; ";
      result += "companions: ";
      if(companions.size() == 0) {
         result += "none";
      }
      for(int i = 0; i < companions.size(); i++) {
         if(i != 0) {
            result += ", ";
         }
         result += companions.get(i);
      }
      result += "; airQuality: " + airQuality + "; ";
      result += "belief: " + belief + "; ";
      result += "intention: " + intention + "; ";
      result += "behavior: " + behavior + "; ";
      result += "barrier: " + barrier + "; ";
      return result;
   }
}