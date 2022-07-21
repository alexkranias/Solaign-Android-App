package com.solaign;

/**
 * A class dedicated to finding the optimal positioning of a solar cell based on a variety of parameters.
 */
public class OptimizePosition {

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 *
	 * Structures defined for this module
	 *
	 *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	static Trigdata tdat = new Trigdata();
	static Posdata pdat = new Posdata();


	public static void main(String[] args) {

		/*
		TO OPTIMIZE THE SPEED AT WHICH WE CAN FIND THE OPTIMAL POSITION I THINK WE SHOULD DO SOMETHING WHERE WE FIND IT SUPER FAST (IN LESS THAN 1 SECOND MAX) BY MAKING THE
		MINUTE INTERVAL = 240, THIS GIVES RESULTS WITHIN +- 1 DEG, THEN AFTER WE CAN USER MULTITHREADING TO CALC IT AGAIN WITH MINUTE INTERVAL = 5, WHICH WILL CORRECT ITSELF
		SEEMLESSLY AFTER THE INITIAL POSITION. SO MAYBE LIKE THE INITIAL DOESNT SAY THE NUMBERS JUST DIRECTS USERS OF HOW TO POSITION IT GENERALLY THAN BY THE TIME IT DOES THE
		MORE PRECISE CALC IT SHOWS EXACT DEGREES
		 */

		//1 min = Tilt: 21.0	Azimuth: 180.0 | 5 min = Tilt: 21.0	Azimuth: 180.0 | 30 min = Tilt: 21.0	Azimuth: 181.0 | 240 min = Tilt: 21.0	Azimuth: 181.0
		double[] positions = calculateSimpleOptimalPosition(23.99838605131, -61.787266930902, -5, 1, 1, 2021, 1, 1, 2022, 1);
		System.out.println("Tilt: " + positions[0] + "\tAzimuth: " + positions[1]);//27.99838605131 -82.787266930902

	}


	static int  month_days[][] = { { 0,   0,  31,  59,  90, 120, 151,
			181, 212, 243, 273, 304, 334, 365 },//NOT LEAP
			{ 0,   0,  31,  60,  91, 121, 152,
					182, 213, 244, 274, 305, 335, 366 } };//IS LEAP
	/* cumulative number of days prior to beginning of month */

	static double degrad = 57.295779513; /* converts from radians to degrees */
	static double raddeg = 0.0174532925; /* converts from degrees to radians */

	/*============================================================================
	*    Local function prototypes
	============================================================================*/


	/*============================================================================
	 *    Long integer function S_solpos, adapted from the VAX solar libraries
	 *
	 *    This function calculates the apparent solar position and the
	 *    intensity of the sun (theoretical maximum solar energy) from
	 *    time and place on Earth.
	 *
	 *    Requires (from the struct posdata parameter):
	 *        Date and time:
	 *            year
	 *            daynum   (requirement depends on the S_DOY switch)
	 *            month    (requirement depends on the S_DOY switch)
	 *            day      (requirement depends on the S_DOY switch)
	 *            hour
	 *            minute
	 *            second
	 *            interval  DEFAULT 0
	 *        Location:
	 *            latitude
	 *            longitude
	 *        Location/time adjuster:
	 *            timezone
	 *        Atmospheric pressure and temperature:
	 *            press     DEFAULT 1013.0 mb
	 *            temp      DEFAULT 10.0 degrees C
	 *        Tilt of flat surface that receives solar energy:
	 *            aspect    DEFAULT 180 (South)
	 *            tilt      DEFAULT 0 (Horizontal)
	 *        Function Switch (codes defined in solpos.h)
	 *            function  DEFAULT S_ALL
	 *
	 *    Returns (via the struct posdata parameter):
	 *        everything defined in the struct posdata in solpos.h.
	 *----------------------------------------------------------------------------*/
	public static long S_solpos (Posdata pdat)
	{
		long retval;

		/* initialize the trig structure */
		tdat.sd = -999.0; /* flag to force calculation of trig data */
		tdat.cd =    1.0;
		tdat.ch =    1.0; /* set the rest of these to something safe */
		tdat.cl =    1.0;
		tdat.sl =    1.0;

		if ((retval = validate ( pdat )) != 0) /* validate the inputs */
			return retval;



		dom2doy( pdat );                /* convert input month-day to doy */

		geometry( pdat );               /* do basic geometry calculations */

		/* etr at non-refracted zenith angle */
		zen_no_ref( pdat, tdat );

		/* solar azimuth calculations */
		sazm( pdat, tdat );

		/* atmospheric refraction calculations */
		refrac( pdat );

		return 0;
	}


	/*============================================================================
	 *    Void function S_init
	 *
	 *    This function initiates all of the input parameters in the struct
	 *    posdata passed to S_solpos().  Initialization is either to nominal
	 *    values or to out of range values, which forces the calling program to
	 *    specify parameters.
	 *
	 *    NOTE: This function is optional if you initialize ALL input parameters
	 *          in your calling code.  Note that the required parameters of date
	 *          and location are deliberately initialized out of bounds to force
	 *          the user to enter real-world values.
	 *
	 *    Requires: Pointer to a posdata structure, members of which are
	 *           initialized.
	 *
	 *    Returns: Void
	 *----------------------------------------------------------------------------*/
	void S_init(Posdata pdat)
	{
		pdat.day       =    -99;   /* Day of month (May 27 = 27, etc.) */
		pdat.daynum    =   -999;   /* Day number (day of year; Feb 1 = 32 ) */
		pdat.hour      =    -99;   /* Hour of day, 0 - 23 */
		pdat.minute    =    -99;   /* Minute of hour, 0 - 59 */
		pdat.month     =    -99;   /* Month number (Jan = 1, Feb = 2, etc.) */
		pdat.second    =    -99;   /* Second of minute, 0 - 59 */
		pdat.year      =    -99;   /* 4-digit year */
		pdat.interval  =      0;   /* instantaneous measurement interval */
		pdat.aspect    =  180.0;   /* Azimuth of panel surface (direction it
	                                    faces) N=0, E=90, S=180, W=270 */
		pdat.latitude  =  -99.0;   /* Latitude, degrees north (south negative) */
		pdat.longitude = -999.0;   /* Longitude, degrees east (west negative) */
		pdat.press     = 1013.0;   /* Surface pressure, millibars */
		pdat.solcon    = 1367.0;   /* Solar constant, 1367 W/sq m */
		pdat.temp      =   15.0;   /* Ambient dry-bulb temperature, degrees C */
		pdat.tilt      =    0.0;   /* Degrees tilt from horizontal of panel */
		pdat.timezone  =  -99.0;   /* Time zone, east (west negative). */
		pdat.sbwid     =    7.6;   /* Eppley shadow band width */
		pdat.sbrad     =   31.7;   /* Eppley shadow band radius */
		pdat.sbsky     =   0.04;   /* Drummond factor for partly cloudy skies */
	}


	/*============================================================================
	 *    Local long int function validate
	 *
	 *    Validates the input parameters
	 *----------------------------------------------------------------------------*/
	static long validate ( Posdata pdat)
	{

		long retval = 0;  /* start with no errors */

		return retval;
	}


	/*============================================================================
	 *    Local Void function dom2doy
	 *
	 *    Converts day-of-month to day-of-year
	 *
	 *    Requires (from struct posdata parameter):
	 *            year
	 *            month
	 *            day
	 *
	 *    Returns (via the struct posdata parameter):
	 *            year
	 *            daynum
	 *----------------------------------------------------------------------------*/
	static void dom2doy( Posdata pdat )
	{
		pdat.daynum = pdat.day + month_days[0][pdat.month];

		/* (adjust for leap year) */
		if ( ((pdat.year % 4) == 0) &&
				( ((pdat.year % 100) != 0) || ((pdat.year % 400) == 0) ) &&
				(pdat.month > 2) )
			pdat.daynum += 1;
	}

	/*============================================================================
	 *    Local Void function geometry
	 *
	 *    Does the underlying geometry for a given time and location
	 *----------------------------------------------------------------------------*/
	static void geometry ( Posdata pdat )
	{
		double bottom;      /* denominator (bottom) of the fraction */
		double c2;          /* cosine of d2 */
		double cd;          /* cosine of the day angle or delination */
		double d2;          /* pdat.dayang times two */
		double delta;       /* difference between current year and 1949 */
		double s2;          /* sine of d2 */
		double sd;          /* sine of the day angle */
		double top;         /* numerator (top) of the fraction */
		int   leap;        /* leap year counter */

		/* Day angle */
	      /*  Iqbal, M.  1983.  An Introduction to Solar Radiation.
	            Academic Press, NY., page 3 */
		pdat.dayang = 360.0 * ( pdat.daynum - 1 ) / 365.0;

		/* Earth radius vector * solar constant = solar energy */
	        /*  Spencer, J. W.  1971.  Fourier series representation of the
	            position of the sun.  Search 2 (5), page 172 */
		sd     = Math.sin (raddeg * pdat.dayang);
		cd     = Math.cos (raddeg * pdat.dayang);
		d2     = 2.0 * pdat.dayang;
		c2     = Math.cos (raddeg * d2);
		s2     = Math.sin (raddeg * d2);

		pdat.erv  = 1.000110 + 0.034221 * cd + 0.001280 * sd;
		pdat.erv  += 0.000719 * c2 + 0.000077 * s2;

		/* Universal Coordinated (Greenwich standard) time */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.utime =
				pdat.hour * 3600.0 +
						pdat.minute * 60.0 +
						pdat.second -
						(double)pdat.interval / 2.0;
		pdat.utime = pdat.utime / 3600.0 - pdat.timezone;

		/* Julian Day minus 2,400,000 days (to eliminate roundoff errors) */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */

	    /* No adjustment for century non-leap years since this function is
	       bounded by 1950 - 2050 */
		delta    = pdat.year - 1949;
		leap     = (int) ( delta / 4.0 );
		pdat.julday =
				32916.5 + delta * 365.0 + leap + pdat.daynum + pdat.utime / 24.0;

		/* Time used in the calculation of ecliptic coordinates */
		/* Noon 1 JAN 2000 = 2,400,000 + 51,545 days Julian Date */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.ectime = pdat.julday - 51545.0;

		/* Mean longitude */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.mnlong  = 280.460 + 0.9856474 * pdat.ectime;

		/* (dump the multiples of 360, so the answer is between 0 and 360) */
		pdat.mnlong -= 360.0 * (int) ( pdat.mnlong / 360.0 );
		if ( pdat.mnlong < 0.0 )
			pdat.mnlong += 360.0;

		/* Mean anomaly */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.mnanom  = 357.528 + 0.9856003 * pdat.ectime;

		/* (dump the multiples of 360, so the answer is between 0 and 360) */
		pdat.mnanom -= 360.0 * (int) ( pdat.mnanom / 360.0 );
		if ( pdat.mnanom < 0.0 )
			pdat.mnanom += 360.0;

		/* Ecliptic longitude */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.eclong  = pdat.mnlong + 1.915 * Math.sin ( pdat.mnanom * raddeg ) +
				0.020 * Math.sin ( 2.0 * pdat.mnanom * raddeg );

		/* (dump the multiples of 360, so the answer is between 0 and 360) */
		pdat.eclong -= 360.0 * (int) ( pdat.eclong / 360.0 );
		if ( pdat.eclong < 0.0 )
			pdat.eclong += 360.0;

		/* Obliquity of the ecliptic */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */

		/* 02 Feb 2001 SMW corrected sign in the following line */
		/*  pdat.ecobli = 23.439 + 4.0e-07 * pdat.ectime;     */
		pdat.ecobli = 23.439 - 4.0e-07 * pdat.ectime;

		/* Declination */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.declin = degrad * Math.asin ( Math.sin (pdat.ecobli * raddeg) *
				Math.sin (pdat.eclong * raddeg) );

		/* Right ascension */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		top      =  Math.cos ( raddeg * pdat.ecobli ) * Math.sin ( raddeg * pdat.eclong );
		bottom   =  Math.cos ( raddeg * pdat.eclong );

		pdat.rascen =  degrad * Math.atan2 ( top, bottom );

		/* (make it a positive angle) */
		if ( pdat.rascen < 0.0 )
			pdat.rascen += 360.0;

		/* Greenwich mean sidereal time */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.gmst  = 6.697375 + 0.0657098242 * pdat.ectime + pdat.utime;

		/* (dump the multiples of 24, so the answer is between 0 and 24) */
		pdat.gmst -= 24.0 * (int) ( pdat.gmst / 24.0 );
		if ( pdat.gmst < 0.0 )
			pdat.gmst += 24.0;

		/* Local mean sidereal time */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.lmst  = pdat.gmst * 15.0 + pdat.longitude;

		/* (dump the multiples of 360, so the answer is between 0 and 360) */
		pdat.lmst -= 360.0 * (int) ( pdat.lmst / 360.0 );
		if ( pdat.lmst < 0.)
			pdat.lmst += 360.0;

		/* Hour angle */
	        /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
	            approximate solar position (1950-2050).  Solar Energy 40 (3),
	            pp. 227-235. */
		pdat.hrang = pdat.lmst - pdat.rascen;

		/* (force it between -180 and 180 degrees) */
		if ( pdat.hrang < -180.0 )
			pdat.hrang += 360.0;
		else if ( pdat.hrang > 180.0 )
			pdat.hrang -= 360.0;
	}


	/*============================================================================
	 *    Local Void function zen_no_ref
	 *
	 *    ETR solar zenith angle
	 *       Iqbal, M.  1983.  An Introduction to Solar Radiation.
	 *            Academic Press, NY., page 15
	 *----------------------------------------------------------------------------*/
	static void zen_no_ref ( Posdata pdat, Trigdata tdat )
	{
		double cz;          /* cosine of the solar zenith angle */

		localtrig( pdat, tdat );
		cz = tdat.sd * tdat.sl + tdat.cd * tdat.cl * tdat.ch;

		/* (watch out for the roundoff errors) */
		if ( Math.abs (cz) > 1.0 ) {
			if ( cz >= 0.0 )
				cz =  1.0;
			else
				cz = -1.0;
		}

		pdat.zenetr   = Math.acos ( cz ) * degrad;

		/* (limit the degrees below the horizon to 9 [+90 -> 99]) */
		if ( pdat.zenetr > 99.0 )
			pdat.zenetr = 99.0;

		pdat.elevetr = 90.0 - pdat.zenetr;
	}

	/*============================================================================
	 *    Local Void function sazm
	 *
	 *    Solar azimuth angle
	 *       Iqbal, M.  1983.  An Introduction to Solar Radiation.
	 *            Academic Press, NY., page 15
	 *----------------------------------------------------------------------------*/
	static void sazm( Posdata pdat, Trigdata tdat )
	{
		double ca;          /* cosine of the solar azimuth angle */
		double ce;          /* cosine of the solar elevation */
		double cecl;        /* ( ce * cl ) */
		double se;          /* sine of the solar elevation */

		localtrig( pdat, tdat );
		ce         = Math.cos ( raddeg * pdat.elevetr );
		se         = Math.sin ( raddeg * pdat.elevetr );

		pdat.azim     = 180.0;
		cecl       = ce * tdat.cl;
		if ( Math.abs ( cecl ) >= 0.001 ) {
			ca     = ( se * tdat.sl - tdat.sd ) / cecl;
			if ( ca > 1.0 )
				ca = 1.0;
			else if ( ca < -1.0 )
				ca = -1.0;

			pdat.azim = 180.0 - Math.acos ( ca ) * degrad;
			if ( pdat.hrang > 0 )
				pdat.azim  = 360.0 - pdat.azim;
		}
	}


	/*============================================================================
	 *    Local Int function refrac
	 *
	 *    Refraction correction, degrees
	 *        Zimmerman, John C.  1981.  Sun-pointing programs and their
	 *            accuracy.
	 *            SAND81-0761, Experimental Systems Operation Division 4721,
	 *            Sandia National Laboratories, Albuquerque, NM.
	 *----------------------------------------------------------------------------*/
	static void refrac( Posdata pdat )
	{
		double prestemp;    /* temporary pressure/temperature correction */
		double refcor;      /* temporary refraction correction */
		double tanelev;     /* tangent of the solar elevation angle */

		/* If the sun is near zenith, the algorithm bombs; refraction near 0 */
		if ( pdat.elevetr > 85.0 )
			refcor = 0.0;

			/* Otherwise, we have refraction */
		else {
			tanelev = Math.tan ( raddeg * pdat.elevetr );
			if ( pdat.elevetr >= 5.0 )
				refcor  = 58.1 / tanelev -
						0.07 / ( Math.pow (tanelev,3) ) +
						0.000086 / ( Math.pow (tanelev,5) );
			else if ( pdat.elevetr >= -0.575 )
				refcor  = 1735.0 +
						pdat.elevetr * ( -518.2 + pdat.elevetr * ( 103.4 +
								pdat.elevetr * ( -12.79 + pdat.elevetr * 0.711 ) ) );
			else
				refcor  = -20.774 / tanelev;

			prestemp    =
					( pdat.press * 283.0 ) / ( 1013.0 * ( 273.0 + pdat.temp ) );
			refcor     *= prestemp / 3600.0;
		}

		/* Refracted solar elevation angle */
		pdat.elevref = pdat.elevetr + refcor;

		/* (limit the degrees below the horizon to 9) */
		if ( pdat.elevref < -9.0 )
			pdat.elevref = -9.0;

		/* Refracted solar zenith angle */
		pdat.zenref  = 90.0 - pdat.elevref;
		pdat.coszen  = Math.cos( raddeg * pdat.zenref );
	}

	/*============================================================================
	 *    Local Void function localtrig
	 *
	 *    Does trig on internal variable used by several functions
	 *----------------------------------------------------------------------------*/
	static void localtrig( Posdata pdat, Trigdata tdat )
	{

		if ( tdat.sd < -900.0 )  /* sd was initialized -999 as flag */
		{
			tdat.sd = 1.0;  /* reflag as having completed calculations */

			tdat.cd = Math.cos ( raddeg * pdat.declin );

			tdat.ch = Math.cos ( raddeg * pdat.hrang );

			tdat.cl = Math.cos ( raddeg * pdat.latitude );

			tdat.sd = Math.sin ( raddeg * pdat.declin );

			tdat.sl = Math.sin ( raddeg * pdat.latitude );
		}
	}

	/**
	 * Calculates the airmass factor for the sun at a given elevation angle
	 * @param sun_elevation_angle_degrees The elevation angle of the sun in degrees.
	 * @return The airmass factor (unitless)
	 */
	private static double airmassFactor(double sun_elevation_angle_degrees) {
		return (1.0/(Math.sin(Math.toRadians(sun_elevation_angle_degrees)) + (0.50572*Math.pow(6.07995+sun_elevation_angle_degrees, -1.63644))));
	}

	static final int SOLAR_IRRADIANCE_NO_AIR = 1353;

	/**
	 * Calculates the approximate clear-sky direct solar irradiance for the sun at a given elevation angle.
	 * @param sun_elevation_angle_degrees The elevation angle of the sun in degrees.
	 * @return The approximate clear-sky direct solar irradiance of the sun; direct as in for a surface facing directly st the sun.
	 */
	private static double calculateDirectSolarIrradiance(double sun_elevation_angle_degrees) {
		return (SOLAR_IRRADIANCE_NO_AIR*Math.pow(0.7, Math.pow(airmassFactor(sun_elevation_angle_degrees), 0.678)));
	}

	private static double calculateSimpleRecievedSolarIrradiance(double panel_tilt_angle_degrees, double panel_azimuth_angle_degrees, double sun_elevation_angle_degrees, double sun_azimuth_angle_degrees) {
		return (calculateDirectSolarIrradiance(sun_elevation_angle_degrees)*(Math.cos(Math.toRadians(sun_elevation_angle_degrees))*Math.sin(Math.toRadians(panel_tilt_angle_degrees))*Math.cos(Math.toRadians(panel_azimuth_angle_degrees-sun_azimuth_angle_degrees))+(Math.sin(Math.toRadians(sun_elevation_angle_degrees))*Math.cos(Math.toRadians(panel_tilt_angle_degrees)))));
	}

	private static void print(int[] dateAndTime) {
		if (dateAndTime.length == 6) {
			System.out.println(	"Sec:\t" + dateAndTime[0] + "\t" +
					"Min:\t" + dateAndTime[1] + "\t" +
					"Hour:\t" + dateAndTime[2] + "\t" +
					"Day:\t" + dateAndTime[3] + "\t" +
					"Month:\t" + dateAndTime[4] + "\t" +
					"Yr:\t" + dateAndTime[5]);
		} else {
			System.out.println("INVALID DATE AND TIME INPUT");
		}
	}

	//comments specify which hemisphere each season's date starts, 2nd inner array is in day, month format.
	private static final int[][] SEASONS_START = {
			{1, 3},	//MARCH 1 		| NORTHERN SPRING START	| SOUTHERN FALL START
			{1, 6},	//JUNE 1		| NORTHERN SUMMER START	| SOUTHERN WINTER START
			{1, 9},	//SEPTEMBER 1	| NORTHERN FALL START 	| SOUTHERN SPRING START
			{1, 12}	//DECEMBER 1	| NORTHERN WINTER START	| SOUTHERN SUMMER START
	};

	private static int[] getCurrentDateAndTime(int start_day, int start_month, int start_year, int minutesAfterStart) {

		int year 		= 		start_year;
		int month 		= 		start_month;
		int day 		= 		start_day + (minutesAfterStart / (60*24));
		int hour      	=    	(minutesAfterStart % (60*24))/60;   		// Hour of day, 0 - 23
		int minute    	=    	(minutesAfterStart % (60));   				// Minute of hour, 0 - 59
		int second    	=    	0;   										// Second of minute, 0 - 59

		//int[] result_date_and_timet = {second, minute, hour, day, month, year};
		//print(result_date_and_timet);

		int isLeap = 0; //0 = false 	1 = true

		if ( 	((year % 4) == 0) &&
				( ((year % 100) != 0) || ((year % 400) == 0) ) ) {
			isLeap = 1;
		} else {
			isLeap = 0;
		}

		while (day > (month_days[isLeap][month+1]-month_days[isLeap][month])) {
			day -= (month_days[isLeap][month+1]-month_days[isLeap][month]);
			month++;
			if (month == 13) {
				year++;
				month = 1;
			}
			if ( 	((year % 4) == 0) &&
					( ((year % 100) != 0) || ((year % 400) == 0) ) ) {
				isLeap = 1;
			} else {
				isLeap = 0;
			}
			//int[] result_date_and_time = {second, minute, hour, day, month, year};
			//print(result_date_and_time);
		}

		int[] result_date_and_time = {second, minute, hour, day, month, year};

		return result_date_and_time;

	}

	private static int getMinutesBetweenDates(int start_day, int start_month, int start_year, int end_day, int end_month, int end_year) {

		int day = start_day;
		int month = start_month;
		int year = start_year;

		int day_counter = 0;
		int isLeap = 0; //0 = false 	1 = true

		if ( 	((year % 4) == 0) &&
				( ((year % 100) != 0) || ((year % 400) == 0) ) ) {
			isLeap = 1;
		} else {
			isLeap = 0;
		}

		if (day > end_day) {
			day_counter += ((month_days[isLeap][month+1]-month_days[isLeap][month]) - start_day + 1);
			month++;
			if (month == 13) {
				year++;
				month = 1;
			}
			day = 1;
		}

		if ( 	((year % 4) == 0) &&
				( ((year % 100) != 0) || ((year % 400) == 0) ) ) {
			isLeap = 1;
		} else {
			isLeap = 0;
		}

		while (month > end_month) {
			day_counter += (month_days[isLeap][month+1]-month_days[isLeap][month]);
			month++;
			if (month == 13) {
				year++;
				month = 1;
			}
		}

		while (year < end_year) {
			if ( 	((year % 4) == 0) &&
					( ((year % 100) != 0) || ((year % 400) == 0) ) ) {
				day_counter += 366;
				year++;
			} else {
				day_counter += 365;
				year++;
			}
		}

		while (month < end_month) {
			day_counter += (month_days[isLeap][month+1]-month_days[isLeap][month]);
			month++;
			if (month == 13) {
				year++;
				month = 1;
			}
		}

		day_counter += (end_day - day);

		int minutes_between = day_counter * 24 * 60;

		return minutes_between;

	}

	/**
	 * The following method calculates the total solar irradiance in Joules per square meter that falls on a solar cell at a given orientation over a specified time period.
	 * Prereq: Latitude and Longitude info already inputted before this method is called.
	 * @param panel_tilt_angle
	 * @param panel_azimuth_angle
	 * @param start_day
	 * @param start_month
	 * @param start_year
	 * @param end_day
	 * @param end_month
	 * @param end_year
	 * @param time_interval_minutes
	 * @return The total solar irradiance in Joules per square meter that falls on a solar cell at a given orientation over a specified time period.
	 */
	private static double calculateTotalEnergyGenerated(double panel_tilt_angle, double panel_azimuth_angle, int start_day, int start_month, int start_year, int end_day, int end_month, int end_year, int time_interval_minutes) {

		int minutes_current = 0;
		int minutes_total = getMinutesBetweenDates(start_day, start_month, start_year, end_day, end_month, end_year);

		int[] current_date_and_time;

		double sun_elevation_angle, sun_azimuth_angle;
		double irradiance, total_energy = 0;

		boolean SUN_RISES_ABOVE_HORIZON = false;

		//ANALYZE ENERGY PRODUCED WITH CURRENT PANEL ORIENTATION
		while (minutes_current < minutes_total) {
			current_date_and_time = getCurrentDateAndTime(start_day, start_month, start_year, minutes_current);

			Posdata.second 	=    	current_date_and_time[0];   	// Second of minute, 0 - 59
			Posdata.minute 	=   	current_date_and_time[1];   	// Minute of hour, 0 - 59
			Posdata.hour 	=    	current_date_and_time[2];   	// Hour of day, 0 - 23
			Posdata.day 	= 		current_date_and_time[3]; 		// Day of month (May 27 = 27, etc.)
			Posdata.month 	= 	 	current_date_and_time[4]; 		// Month number (Jan = 1, Feb = 2, etc.)
			Posdata.year 	= 		current_date_and_time[5]; 		// 4-digit year

			S_solpos(pdat);

			sun_elevation_angle = pdat.elevetr;
			sun_azimuth_angle = pdat.azim;

			if (sun_elevation_angle > 0) { //ONLY CALCULATE WHILE SUN IS ABOVE HORIZON
				SUN_RISES_ABOVE_HORIZON = true;
				irradiance = calculateSimpleRecievedSolarIrradiance(panel_tilt_angle, panel_azimuth_angle, sun_elevation_angle, sun_azimuth_angle);

				/* Irradiance is < 0 when the panel is being backlit by the sun,
				this does not cause negative power just 0 power (assumes
				power generation from diffuse light = 0, research this later) */
				if (irradiance < 0) {
					irradiance = 0;
				}

				total_energy += (irradiance*time_interval_minutes);
			}
			minutes_current += time_interval_minutes;
		}

		//HERE WE NEED TO PROMPT THE USER WITH A MESSAGE DISPLAYING THIS ERROR
		if (!SUN_RISES_ABOVE_HORIZON) throw new RuntimeException("Sun Never Rises at...\t Latitude: " + Posdata.latitude + "\tLongitude: " + Posdata.longitude + "\t...From " + start_month + "/" + start_day + "/" + start_year + " to "+ end_month + "/" + end_day + "/" + end_year + "\nPLEASE INPUT A DIFFERENT DATE RANGE THAT INCLUDES TIME DURING THE YEAR WHEN THERE IS DAYLIGHT.");

		return total_energy;
	}

	/**
	 * A simplified optimization method that calculates the optimal positioning of a solar panel at a given longitude and latitude to maximize the
	 * amount of energy produced in a given time period WITHOUT consideration of weather/climate (clear sky assumed).
	 * @param longitude The Earth longitude coordinate the panel is positioned at.
	 * @param latitude The Earth latitude coordinate the panel is positioned at.
	 * @param time_zone_from_gmt The number of hours the timezone of the panel instillation is from GMT. (e.g. EST is -5)
	 * @param start_day The day in the month the optimization period begins. (e.g. February 23, 2021; start_day = 23)
	 * @param start_month The month the optimization period begins. (e.g. February 23, 2021; start_month = 2)
	 * @param start_year The year the optimization period begins. (e.g. February 23, 2021; start_year = 2021)
	 * @param end_day The day in the month the optimization period ends. (e.g. February 23, 2021; start_day = 23)
	 * @param end_month The month the optimization period ends. (e.g. February 23, 2021; start_month = 2)
	 * @param end_year The year the optimization period ends. (e.g. February 23, 2021; start_year = 2021)
	 * @param time_interval_minutes The time interval between each calculated sun position in minutes. The smaller the number, the more precise the optimal position, but also more time and calculations are needed to find said answer.
	 * @return A two-element array containing the optimal positions; index 0 is the tilt angle and index 1 is the azimuth angle of the solar panel.
	 */
	private static double[] calculateSimpleOptimalPosition(double latitude, double longitude, int time_zone_from_gmt, int start_day, int start_month, int start_year, int end_day, int end_month, int end_year, int time_interval_minutes) {

		validate_optimization_inputs(latitude, longitude, time_zone_from_gmt, start_day, start_month, start_year, end_day, end_month, end_year, time_interval_minutes);

		Posdata.timezone  =  time_zone_from_gmt;   /* Time zone, east (west negative). */
		Posdata.interval  =      0;   /* instantaneous = 0 measurement interval */

		Posdata.latitude  = latitude;   /* Latitude, degrees north (south negative) */
		Posdata.longitude = longitude;   /* Longitude, degrees east (west negative) */

		int counter = 0;
		/*===========================================================================================================*/

		double panel_tilt_angle = 12, panel_azimuth_angle; //in degrees

		// AZIMUTH ALWAYS MOST OPTIMAL AT 0 OR 180 DEG
		if (latitude > 0) panel_azimuth_angle = 180; //in Northern Hemisphere
		else panel_azimuth_angle = 0; //in Southern Hemisphere

		double total_energy = 0; //THIS IS THE TOTAL IRRADIANCE THAT HITS THE PANEL PER SQ METER OVER THE COURSE OF THE TIME DURATION

		int minutes_current = 0;
		int minutes_total = getMinutesBetweenDates(start_day, start_month, start_year, end_day, end_month, end_year);

		double sun_elevation_angle, sun_azimuth_angle, irradiance;
		int[] current_date_and_time;

		double test_panel_tilt_angle = panel_tilt_angle, test_panel_azimuth_angle = panel_azimuth_angle; //in degrees
		double dPower_dTilt = 1, dPower_dAzimuth = 1;
		double tilt_step = 1, azimuth_step = 1; //changes each angle by 1 deg and sees change in energy, then either inc or dec value by 1 deg to increase energy produced

		double total_energy_p1 = 0, total_energy_p2 = 0; //TOTAL ENERGY POSITIONS 1 AND 2 (2 = final, 1 = init)

		boolean foundOptimalPosition = false;
		double max_energy = 0, max_tilt = 0, max_azimuth = 0;
		while (!foundOptimalPosition) {

			//CHANGE IN ENERGY FROM CHANGE IN TILT ANGLE
			total_energy_p2 = calculateTotalEnergyGenerated(panel_tilt_angle + tilt_step, panel_azimuth_angle, start_day, start_month, start_year, end_day, end_month, end_year, time_interval_minutes);
			total_energy_p1 = calculateTotalEnergyGenerated(panel_tilt_angle, panel_azimuth_angle, start_day, start_month, start_year, end_day, end_month, end_year, time_interval_minutes);

			//FIND TILT DIFFERENTIAL
			dPower_dTilt = (total_energy_p2 - total_energy_p1)/tilt_step;
			dPower_dTilt /= minutes_total;

			if (dPower_dTilt > 0) panel_tilt_angle += tilt_step;
			else if (dPower_dTilt < 0) panel_tilt_angle -= tilt_step;
			else panel_tilt_angle += 0;


			double current_energy = calculateTotalEnergyGenerated(panel_tilt_angle, panel_azimuth_angle, start_day, start_month, start_year, end_day, end_month, end_year, time_interval_minutes);
			if (current_energy >= max_energy) {
				max_energy = current_energy;
				max_tilt = panel_tilt_angle;
				max_azimuth = panel_azimuth_angle;
			} else {
				counter++;
			}

			if (counter >= 2) foundOptimalPosition = true; //double check that first instance of a max wasn't a fluke


		}

		//If optimal angle is behind solar panel
		if (max_tilt < 0) {
			max_tilt = -max_tilt;
			max_azimuth = (max_azimuth + 180) % 360;
		}

		//Double check the best optimal tilt position
		double temp_max = max_tilt;
		for (int i = -1; i <= 1; i += 2) {
			temp_max = max_tilt + (tilt_step * i);
			double energy = calculateTotalEnergyGenerated(temp_max, max_azimuth, start_day, start_month, start_year, end_day, end_month, end_year, time_interval_minutes);
			if (energy >= max_energy) {
				max_energy = energy;
				max_tilt = temp_max;
			}
		}

		//Double check the best optimal azimuth position
		for (int i = -1; i <= 1; i += 2) {
			temp_max = max_azimuth + (azimuth_step * i);
			double energy = calculateTotalEnergyGenerated(max_tilt, temp_max, start_day, start_month, start_year, end_day, end_month, end_year, time_interval_minutes);
			if (energy >= max_energy) {
				max_energy = energy;
				max_azimuth = temp_max;
			}
		}

		double[] optimal_position = {max_tilt, max_azimuth};

		return optimal_position;
	}

	private static void validate_optimization_inputs(double latitude, double longitude, int time_zone_from_gmt, int start_day, int start_month, int start_year, int end_day, int end_month, int end_year, int time_interval_minutes) {

		int STARTisLeap = 0; //0 = false 	1 = true
		int ENDisLeap = 0; //0 = false 	1 = true

		if ( 	((start_year % 4) == 0) &&
				( ((start_year % 100) != 0) || ((start_year % 400) == 0) ) ) {
			STARTisLeap = 1;
		} else {
			STARTisLeap = 0;
		}

		if ( 	((end_year % 4) == 0) &&
				( ((end_year % 100) != 0) || ((end_year % 400) == 0) ) ) {
			ENDisLeap = 1;
		} else {
			ENDisLeap = 0;
		}

		boolean startDay_IS_AFTER_endDay = (start_year > end_year) || (start_month > end_month) || (start_day > end_day && start_month == end_month);

		if (Math.abs(latitude) > 90 || Math.abs(longitude) > 90) throw new RuntimeException("INVALID COORDINATE POSITION: remember lat and long must be within -90 to 90 deg.");
		if (start_day <= 0 || start_month <= 0 || end_day <= 0 || end_month <= 0 || start_month > 12 || end_month > 12 || start_day > (month_days[STARTisLeap][start_month+1] - month_days[STARTisLeap][start_month]) || end_day > (month_days[ENDisLeap][end_month+1] - month_days[ENDisLeap][end_month])) throw new RuntimeException("INVALID DATE(S): outside domain");
		if (startDay_IS_AFTER_endDay) throw new RuntimeException("INVALID DATE(S): start date is after end date");
		if (start_day == end_day && start_month == end_month && start_year == end_year) throw new RuntimeException("INVALID DATE(S): start date is same as end date");
		if (Math.abs(time_zone_from_gmt) > 12) throw new RuntimeException("INVALID TIME ZONE"); //check what a valid timezone is idk if this is accurate
		if (time_interval_minutes > (4*60)) throw new RuntimeException("INVALID TIME INTERVAL: must be less than or equal to 4 hours in length (too inaccurate otherwise)");
		if (time_interval_minutes <= 0) throw new RuntimeException("INVALID TIME INTERVAL: outside of accepted domain");


	}

}