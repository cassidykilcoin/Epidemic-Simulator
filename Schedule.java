// Schedule.java

import java.util.Scanner;

/** author Douglas W. Jones
 *  author Cassidy Kilcoin
 *  @version Apr. 10 2021
 *  Tuple of start and end times used for scheduling 
 *  people's visits to places
 */
class Schedule {
    // instance variables
    public final double startTime; // times are in seconds anno midnight
    public final double duration;  // duration of visit
    
    // TEST: NEW CODE
    public final double arriveProb;
    private static final MyRandom rando = MyRandom.stream();

    /** construct a new Schedule
     *  @param in -- the input stream
     *  @param context -- the context for error messages
     *  Syntax: (0.0-0.0)
     *  Meaning: (start-end) times given in hours from midnight
     *  The begin paren must just have been scanned from the input stream
     */
    public Schedule( MyScanner in, MyScanner.Message context ) {

	// get start time of schedule
	final double st = in.getNextFloat(
	    23.98F, ()-> context.myString() + "(: not followed by start time"
	);
	in.getNextLiteral(
	    MyScanner.dash, ()-> context.myString() + "(" + st
						    + ": not followed by -"
	);
	// get end time of schedule
	final double et = in.getNextFloat(
	    23.99F, ()-> context.myString() + "(" + st
					    + "-: not followed by end time"
	);

    //TEST: NEW CODE
    
    if(!in.tryNextLiteral( MyScanner.endParen )) { 
        arriveProb = in.getNextFloat( 0.0,
            ()-> context.myString() + "(" + st
                + et + ": arrival probability expected"
        );
        in.getNextLiteral(
            MyScanner.endParen,
            ()-> context.myString() + "(" + st + "-" + et
                        + ": not followed by )"
        );
    } else {
        arriveProb = 1.0;
    }
    // TEST: end of new code

	// check sanity constraints on start and end times
	if (st >= 24.00F) {
	    Error.warn(
		context.myString() + "(" + st + "-" + et
				   + "): start time is tomorrow"
	    );
	}
	Check.nonNeg( st, 0.0F,
	    ()-> context.myString() + "(" + st + "-" + et
				    + "): start time is yesterday"
	);
	if (st >= et) {
	    Error.warn(
		context.myString() + "(" + st + "-" + et
				   + "): times out of order"
	    );
	}

    //TEST: new code
    Check.positive( arriveProb, 0.0, ()-> context.myString() 
        + arriveProb + "non-positive arrival probability?"
        );

    if( arriveProb > 1.0 ) {
        Error.warn(
            context.myString() + " " + arriveProb
            + ": arrival probability more than 100%"
        );
    }

	startTime = st * Time.hour;
	duration = (et * Time.hour) - startTime;
    }

    /** compare two schedules to see if they overlap
     *  @return true if they overlap, false otherwise
     */
    public boolean overlap( Schedule s ) {
	if (s == null) return false;
	double thisEnd = this.startTime + this.duration;
	if (this.startTime <= s.startTime) {
	    if (s.startTime <= (this.startTime + this.duration)) return true;
	}
	double sEnd = s.startTime + s.duration;
	if (s.startTime <= this.startTime) {
	    if (this.startTime <= (s.startTime + s.duration)) return true;
	}
	return false;
    }

    /** commit a person to following a schedule regarding a place
     *  @param person
     *  @param place
     *  this starts the logical process of making a person follow this schedule
     */
    public void apply( Person person, Place place ) {
	Simulator.schedule( startTime, (double t)-> go( t, person, place ) );
    }

    /** keep a person on schedule
     *  @param person
     *  @param place
     *  this continues a logical process of moving a person on this schedule
     */
    private void go( double time, Person person, Place place ) {
	double tomorrow = time + Time.day;
    //TEST:NEW CODE
    float testRand = rando.nextFloat();
    if( testRand <= arriveProb ) {
	// first, ensure that we keep following this schedule
	Simulator.schedule( tomorrow, (double t)-> go( t, person, place ) );

	// second, make the person go there
	person.travelTo( time, place );

	// third, make sure we get home
	Simulator.schedule( time + duration, (double t)-> person.goHome( t ) );        
    }
    }

    /** convert a Schedule back to textual form
     *  @return the schedule as a string
     *  Syntax: (0.0-0.0)
     *  Meaning: (start-end) times given in hours from midnight
     */
    public String toString() {
	return "(" + startTime/Time.hour
	     + "-" + (startTime + duration) / Time.hour + ")";
    }
}