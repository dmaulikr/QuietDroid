package jid.quitedroid.Database;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import jid.quitedroid.ContextHandler;

/**
 * Created by JiYeon on 2016-07-12.
 */
//Associated with organising events
//Relate to GroupID (Overlapping Events +- 10 minutes)
public class EventOperations {
    //Initialize Database and two events required for timeline tracker algorithm
    private DatabaseOperations databaseOperations = new DatabaseOperations(ContextHandler.getContext());
    private Cursor cursor;
    private Event presentEvent = new Event();
    private Event futureEvent = new Event();
    private Event storePresentEvent = new Event();
    private Event storeFutureEvent = new Event();

    //NOTE: Init method 1 and 2 executed at the start of evaluation for each group
    public void organizeEventsToNewDbTable() {
        ArrayList<String> groupIDList;
        groupIDList = databaseOperations.getGroupIDList();

        //Iterate through groupID
        for (int i = 0; i < groupIDList.size(); i++) {
            cursor = databaseOperations.getNewCursor(databaseOperations, groupIDList.get(i));
            /****INIT************************************/
            if(!findEarliestEvent()){continue;} //If group did not have any ANY valid events (e.g. normal events only) please skip to next group
            findNextEarliestEvent(storePresentEvent.getEvent_start_time(), false);
            //Recursive method to sort all the events
            organizeEventsToDB();
            //Force insert normal mode at the end (trigger)
            forceInsertNormalMode();
            //Close cursor
            cursor.close();
        }
    }

    //Enter Recursive Stage
    //Step 4 *** of documentation
    private void organizeEventsToDB() {
        Boolean doesEventsOverlap, validFutureEventFound = false;
        doesEventsOverlap = validateAllFutureEvents(); //Store to Present Event to table 2 of database

        if (doesEventsOverlap) {
            //INIT
            if (findNextEarliestEvent(storePresentEvent.getEvent_start_time(), false)) {
                if (isFutureEventValid()) {
                    validFutureEventFound = true;
                } else {
                    //Note to self, keep present event until VALID future event has been found
                    while (findNextEarliestEvent(storeFutureEvent.getEvent_start_time(), false)) {
                        if (isFutureEventValid()) {
                            validFutureEventFound = true;
                            break;
                        }
                    }
                }
            }
        }

        if (validFutureEventFound) { //+OVERLAP FOUND
            //Set new Present Event ***
            setStorePresentEvent(storeFutureEvent.getEventID(), storeFutureEvent.getEvent_start_time(), storeFutureEvent.getEvent_end_time(), storeFutureEvent.getMode());
            //Find and validate new future event
            if (findNextEarliestEvent(storePresentEvent.getEvent_start_time(), false)) {
                //Go back to Step 4 *** (Specified above) to validate and store to table 2 of db
                organizeEventsToDB();
            } else {
                //Store present event to table 2 of db if not normal mode
                storePresentEventToDB();
                searchPreviousEvents();
                //Finish, EXIT***
            }

        } else {
            if (findNextEarliestEvent(storePresentEvent.getEvent_end_time() - 1, true)) {
                //SET NEW FUTURE EVENT
                if (findNextEarliestEvent(storePresentEvent.getEvent_start_time(), false)) {
                    //Go back to Step 4 *** (Specified above) to validate and store to table 2 of db
                    organizeEventsToDB();
                } else {
                    storePresentEventToDB();
                    searchPreviousEvents();
                }
            } else {
                //EXIT
            }
        }
    }

    private void searchPreviousEvents() {
        //Create cursor
//        Cursor cursor = databaseOperations.getCursor(databaseOperations, TableData.TableInfo.TABLE_NAME_RAW);
        //Move cursor to first row
        cursor.moveToFirst();
        //Initialize present event
        setPresentEvent(storePresentEvent.getEventID(), storePresentEvent.getEvent_start_time(), storePresentEvent.getEvent_end_time(), 1);
        //Initialize init flag
        Boolean validEventFound = false;
        //Continue while there is a next row to move to
        do {
            //Initialize Future Event
            setFutureEvent(cursor.getString(9), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3));

            //If the event that is being evaluated has Normal Mode, skip to next event
            if (futureEvent.getMode() == 0) {
                continue;
            }

            //If start time is later than the the current present event, store future event to present event
            if ((futureEvent.getEvent_end_time()) > (presentEvent.getEvent_end_time())) {
                if (!(futureEvent.getMode() < presentEvent.getMode())) {
                    setPresentEvent(futureEvent.getEventID(), futureEvent.getEvent_start_time(), futureEvent.getEvent_end_time(), futureEvent.getMode());
                    validEventFound = true;
                }
            }
        }
        while (cursor.moveToNext());

        setStorePresentEvent(presentEvent.getEventID(), databaseOperations.findLatestTime(databaseOperations), presentEvent.getEvent_end_time(), presentEvent.getMode());

        if (validEventFound && (storePresentEvent.getEvent_start_time() != storePresentEvent.getEvent_end_time())) {
            organizeEventsToDB();
        }
    }

    //Init method 2: Find earliest and next earliest 'valid' event in group
    //Valid Event: Highest mode and latest end time
    //Step 1,2 of Event Processing Documentation
    public Boolean findEarliestEvent() {
        Boolean validPresentEventFound = false;
        //Move cursor to first row
        cursor.moveToFirst();
        //Initialize present event
        setPresentEvent("XX", Long.MAX_VALUE, 0, 0);
        //Continue while there is a next row to move to
        do {
            //Initialize Future Event
            setFutureEvent(cursor.getString(9), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3));
            //If the event that is being evaluated has Normal Mode, skip to next event
            if (futureEvent.getMode() == 0) {
                continue;
            }

            validPresentEventFound = true;

            Log.d("TEST","ENTERED");

            searchForEarliestEvent();
        } while (cursor.moveToNext());
        //Store Present Event
        setStorePresentEvent(presentEvent.getEventID(), presentEvent.getEvent_start_time(), presentEvent.getEvent_end_time(), presentEvent.getMode());
        //Set earliest time (INIT for each group) ***
        databaseOperations.setEarliestTime(presentEvent.getEvent_start_time());

        return validPresentEventFound;
    }

    //This method finds the event that starts AFTER the input searchTime (int)
    //Step 4 of Event Processing Documentation
    public Boolean findNextEarliestEvent(long searchTime, Boolean isPresent) {
        //Move cursor to first row
        cursor.moveToFirst();
        //Initialize present event
        setPresentEvent("XX", searchTime, 0, 0);
        //Initialize init flag
        Boolean validEventFound = false;
        //Continue while there is a next row to move to
        do {
            //Initialize Future Event
            setFutureEvent(cursor.getString(9), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3));
            //If the event that is being evaluated has Normal Mode, skip to next event
            if (futureEvent.getMode() == 0) {
                continue;
            }
            if (!validEventFound) {
                //If start time is later than the the current present event, store future event to present event
                if ((futureEvent.getEvent_start_time()) > (presentEvent.getEvent_start_time())) {
                    setPresentEvent(futureEvent.getEventID(), futureEvent.getEvent_start_time(), futureEvent.getEvent_end_time(), futureEvent.getMode());
                    validEventFound = true;
                }
            } else {
                searchForEarliestEvent();
            }
        }
        while (cursor.moveToNext());

        if (isPresent) {
            //Store new event to Present Event
            setStorePresentEvent(presentEvent.getEventID(), presentEvent.getEvent_start_time(), presentEvent.getEvent_end_time(), presentEvent.getMode());
        } else {
            //Store new event to Future Event
            setStoreFutureEvent(presentEvent.getEventID(), presentEvent.getEvent_start_time(), presentEvent.getEvent_end_time(), presentEvent.getMode());
        }

        return validEventFound;
    }

    //Set earliest event as present event
    private void searchForEarliestEvent() {
        //If start time is earlier than the the current present event, store future event to present event
        if ((futureEvent.getEvent_start_time()) < (presentEvent.getEvent_start_time())) {
            setPresentEvent(futureEvent.getEventID(), futureEvent.getEvent_start_time(), futureEvent.getEvent_end_time(), futureEvent.getMode());
        } else if (futureEvent.getEvent_start_time() == presentEvent.getEvent_start_time()) {
            //Find out if the new event has a 'higher or same' mode, if the start time of both events are the same
            if (!(futureEvent.getMode() < presentEvent.getMode())) {
                //Find out if the new event has a same/later end time, store future event to present event
                if (futureEvent.getEvent_end_time() >= presentEvent.getEvent_end_time()) {
                    setPresentEvent(futureEvent.getEventID(), futureEvent.getEvent_start_time(), futureEvent.getEvent_end_time(), futureEvent.getMode());
                }
            }
        }
    }

    //Recursively invoke this method until all valid future events have been validated
    //Return TRUE: Found an overlap with present event and valid future event
    //Return FALSE: There are no overlaps between present event and valid future event
    private Boolean validateAllFutureEvents() {
        long startTime = 0; //Initialize start time
        //Recursive Stage
        if (doesFutureEventOverlap()) {
            if (presentEventHasHigherMode()) {
                //Check for all other future events that has either:
                //A higher mode, or start time earlier than present event's end time
                if (findNextEarliestEvent(storeFutureEvent.getEvent_start_time(), false)) {
                    validateAllFutureEvents();
                    return true;
                } else {
                    //Future Event has the same or lower mode
                    //Store to Table 2 of database
                    startTime = databaseOperations.findLatestTime(databaseOperations) > storePresentEvent.getEvent_start_time() ? databaseOperations.findLatestTime(databaseOperations) : storePresentEvent.getEvent_start_time();
                    databaseOperations.insert(databaseOperations, "calendar_info_organized", "NEW TEST", startTime, storePresentEvent.getEvent_end_time(), storePresentEvent.getMode(), 2, "1,1", "3,3", 1, 1, databaseOperations.setNewEventID(databaseOperations, storePresentEvent.getEventID()));
                    return true;
                }
            } else {
                //Store to Table 2
                startTime = databaseOperations.findLatestTime(databaseOperations) > storePresentEvent.getEvent_start_time() ? databaseOperations.findLatestTime(databaseOperations) : storePresentEvent.getEvent_start_time();
                databaseOperations.insert(databaseOperations, "calendar_info_organized", "NEW NEW TEST", startTime, storeFutureEvent.getEvent_start_time(), storePresentEvent.getMode(), 2, "1,1", "3,3", 1, 1, databaseOperations.setNewEventID(databaseOperations, storePresentEvent.getEventID()));
                return true;
            }
        } else {
            //Future Event does not overlap, therefore store straight into Table 2 of db
            //Store to Table 2 of database
            startTime = databaseOperations.findLatestTime(databaseOperations) > storePresentEvent.getEvent_start_time() ? databaseOperations.findLatestTime(databaseOperations) : storePresentEvent.getEvent_start_time();
            databaseOperations.insert(databaseOperations, "calendar_info_organized", "TEST", startTime, storePresentEvent.getEvent_end_time(), storePresentEvent.getMode(), 2, "1,1", "3,3", 1, 1, databaseOperations.setNewEventID(databaseOperations, storePresentEvent.getEventID()));
            return false;
        }
    }

    private void forceInsertNormalMode() {
        long startTime = databaseOperations.findLatestTime(databaseOperations);
        databaseOperations.insert(databaseOperations, "calendar_info_organized", "FINAL EVENT OF GROUP", startTime, startTime, 0, 2, "1,1", "3,3", 1, 1, databaseOperations.setNewEventID(databaseOperations, storePresentEvent.getEventID()));
    }

    //This method is invoked when only one event is left to be validated
    private void storePresentEventToDB() {
        long startTime = 0;
        if (storePresentEvent.getMode() != 0) {
            //COME BACK HERE
            startTime = databaseOperations.findLatestTime(databaseOperations) > storePresentEvent.getEvent_start_time() ? databaseOperations.findLatestTime(databaseOperations) : storePresentEvent.getEvent_start_time();
            databaseOperations.insert(databaseOperations, "calendar_info_organized", "FINAL TEST", startTime, storePresentEvent.getEvent_end_time(), storePresentEvent.getMode(), 2, "1,1", "3,3", 1, 1, databaseOperations.setNewEventID(databaseOperations, storePresentEvent.getEventID()));
        }
    }

    //Does the end time of present event come after future event's start time
    //Only start time of future event required
    //Yes: Return TRUE
    private Boolean doesFutureEventOverlap() {
        if (storePresentEvent.getEvent_end_time() > storeFutureEvent.getEvent_start_time()) {
            return true;
        }
        return false;
    }

    //Does the end time of present event come after future event's start time and
    //YES: Return TRUE
    private Boolean isFutureEventValid() {
        //databaseOperations.findLatestTime(databaseOperations) > storeFutureEvent.getEvent_start_time() &&
        if (databaseOperations.findLatestTime(databaseOperations) < storeFutureEvent.getEvent_end_time()) {
            return true;
        }
        return false;
    }

    //Does present event have a higher mode than the future event?
    //YES: Return TRUE
    private Boolean presentEventHasHigherMode() {
        if (storePresentEvent.getMode() > storeFutureEvent.getMode()) {
            return true;
        }
        return false;
    }

    private void setPresentEvent(String eventID, long startT, long endT, int mode) {
        presentEvent.setEvent_ID(eventID);
        presentEvent.setEvent_start_time(startT);
        presentEvent.setEvent_end_time(endT);
        presentEvent.setMode(mode);
    }

    private void setFutureEvent(String eventID, long startT, long endT, int mode) {
        futureEvent.setEvent_ID(eventID);
        futureEvent.setEvent_start_time(startT);
        futureEvent.setEvent_end_time(endT);
        futureEvent.setMode(mode);
    }

    private void setStorePresentEvent(String eventID, long startT, long endT, int mode) {
        storePresentEvent.setEvent_ID(eventID);
        storePresentEvent.setEvent_start_time(startT);
        storePresentEvent.setEvent_end_time(endT);
        storePresentEvent.setMode(mode);
    }

    private void setStoreFutureEvent(String eventID, long startT, long endT, int mode) {
        storeFutureEvent.setEvent_ID(eventID);
        storeFutureEvent.setEvent_start_time(startT);
        storeFutureEvent.setEvent_end_time(endT);
        storeFutureEvent.setMode(mode);
    }
}
