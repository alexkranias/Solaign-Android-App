package com.solaign;

public class Posdata {
	
	/***** ALPHABETICAL LIST OF COMMON VARIABLES *****/
    /* Each comment begins with a 1-column letter code:
       I:  INPUT variable
       O:  OUTPUT variable
       T:  TRANSITIONAL variable used in the algorithm,
           of public static interest only to the solar radiation
           modelers, and available to you because you
           may be one of them.

       The FUNCTION column indicates which sub-function
       within solpos must be switched on using the
       "function" parameter to calculate the desired
       output variable.  All function codes are
       defined in the solpos.h file.  The default
       S_ALL switch calculates all output variables.
       Multiple functions may be or'd to create a
       composite function switch.  For example,
       (S_TST | S_SBCF). Specifying only the functions
       for required output variables may allow solpos
       to execute more quickly.

       The S_DOY mask works as a toggle between the
       input date represented as a day number (daynum)
       or as month and day.  To set the switch (to
       use daynum input), the function is or'd; to
       clear the switch (to use month and day input),
       the function is inverted and and'd.

       For example:
           pdat->function |= S_DOY (sets daynum input)
           pdat->function &= ~S_DOY (sets month and day input)

       Whichever date form is used, S_solpos will
       calculate and return the variables(s) of the
       other form.  See the soltest.c program for
       other examples. */

/* VARIABLE        I/O  Function    Description */
/* -------------  ----  ----------  ---------------------------------------*/

public static int   day;       /* I/O: S_DOY      Day of month (May 27 = 27, etc.)
                 solpos will CALCULATE this by default,
                 or will optionally require it as input
                 depending on the setting of the S_DOY
                 function switch. */
public static int   daynum;    /* I/O: S_DOY      Day number (day of year; Feb 1 = 32 )
                 solpos REQUIRES this by default, but
                 will optionally calculate it from
                 month and day depending on the setting
                 of the S_DOY function switch. */
public static int   function;  /* I:              Switch to choose functions for desired
                 output. */
public static int   hour;      /* I:              Hour of day, 0 - 23, DEFAULT = 12 */
public static int   interval;  /* I:              public static interval of a measurement period in
                 seconds.  Forces solpos to use the
                 time and date from the public static interval
                 midpopublic static int. The INPUT time (hour,
                 minute, and second) is assumed to
                 be the END of the measurement
                 public static interval. */
public static int   minute;    /* I:              Minute of hour, 0 - 59, DEFAULT = 0 */
public static int   month;     /* I/O: S_DOY      Month number (Jan = 1, Feb = 2, etc.)
                 solpos will CALCULATE this by default,
                 or will optionally require it as input
                 depending on the setting of the S_DOY
                 function switch. */
public static int   second;    /* I:              Second of minute, 0 - 59, DEFAULT = 0 */
public static int   year;      /* I:              4-digit year (2-digit year is NOT
                allowed */

/***** public static doubleS *****/

public static double amass;      /* O:  S_AMASS    Relative optical airmass */
public static double ampress;    /* O:  S_AMASS    Pressure-corrected airmass */
public static double aspect;     /* I:             Azimuth of panel surface (direction it
                 faces) N=0, E=90, S=180, W=270,
                 DEFAULT = 180 */
public static double azim;       /* O:  S_SOLAZM   Solar azimuth angle:  N=0, E=90, S=180,
                 W=270 */
public static double cosinc;     /* O:  S_TILT     Cosine of solar incidence angle on
                 panel */
public static double coszen;     /* O:  S_REFRAC   Cosine of refraction corrected solar
                 zenith angle */
public static double dayang;     /* T:  S_GEOM     Day angle (daynum*360/year-length)
                 degrees */
public static double declin;     /* T:  S_GEOM     Declination--zenith angle of solar noon
                 at equator, degrees NORTH */
public static double eclong;     /* T:  S_GEOM     Ecliptic longitude, degrees */
public static double ecobli;     /* T:  S_GEOM     Obliquity of ecliptic */
public static double ectime;     /* T:  S_GEOM     Time of ecliptic calculations */
public static double elevetr;    /* O:  S_ZENETR   Solar elevation, no atmospheric
                 correction (= ETR) */
public static double elevref;    /* O:  S_REFRAC   Solar elevation angle,
                 deg. from horizon, refracted */
public static double eqntim;     /* T:  S_TST      Equation of time (TST - LMT), minutes */
public static double erv;        /* T:  S_GEOM     Earth radius vector
                 (multiplied to solar constant) */
public static double etr;        /* O:  S_ETR      Extraterrestrial (top-of-atmosphere)
                 W/sq m global horizontal solar
                 irradiance */
public static double etrn;       /* O:  S_ETR      Extraterrestrial (top-of-atmosphere)
                 W/sq m direct normal solar
                 irradiance */
public static double etrtilt;    /* O:  S_TILT     Extraterrestrial (top-of-atmosphere)
                 W/sq m global irradiance on a tilted
                 surface */
public static double gmst;       /* T:  S_GEOM     Greenwich mean sidereal time, hours */
public static double hrang;      /* T:  S_GEOM     Hour angle--hour of sun from solar noon,
                 degrees WEST */
public static double julday;     /* T:  S_GEOM     Julian Day of 1 JAN 2000 minus
                 2,400,000 days (in order to regain
                 single precision) */
public static double latitude;   /* I:             Latitude, degrees north (south negative) */
public static double longitude;  /* I:             Longitude, degrees east (west negative) */
public static double lmst;       /* T:  S_GEOM     Local mean sidereal time, degrees */
public static double mnanom;     /* T:  S_GEOM     Mean anomaly, degrees */
public static double mnlong;     /* T:  S_GEOM     Mean longitude, degrees */
public static double rascen;     /* T:  S_GEOM     Right ascension, degrees */
public static double press;      /* I:             Surface pressure, millibars, used for
                 refraction correction and ampress */
public static double prime;      /* O:  S_PRIME    Factor that normalizes Kt, Kn, etc. */
public static double sbcf;       /* O:  S_SBCF     Shadow-band correction factor */
public static double sbwid;      /* I:             Shadow-band width (cm) */
public static double sbrad;      /* I:             Shadow-band radius (cm) */
public static double sbsky;      /* I:             Shadow-band sky factor */
public static double solcon;     /* I:             Solar constant (NREL uses 1367 W/sq m) */
public static double ssha;       /* T:  S_SRHA     Sunset(/rise) hour angle, degrees */
public static double sretr;      /* O:  S_SRSS     Sunrise time, minutes from midnight,
                 local, WITHOUT refraction */
public static double ssetr;      /* O:  S_SRSS     Sunset time, minutes from midnight,
                 local, WITHOUT refraction */
public static double temp;       /* I:             Ambient dry-bulb temperature, degrees C,
                 used for refraction correction */
public static double tilt;       /* I:             Degrees tilt from horizontal of panel */
public static double timezone;   /* I:             Time zone, east (west negative).
               USA:  Mountain = -7, Central = -6, etc. */
public static double tst;        /* T:  S_TST      True solar time, minutes from midnight */
public static double tstfix;     /* T:  S_TST      True solar time - local standard time */
public static double unprime;    /* O:  S_PRIME    Factor that denormalizes Kt', Kn', etc. */
public static double utime;      /* T:  S_GEOM     Universal (Greenwich) standard time */
public static double zenetr;     /* T:  S_ZENETR   Solar zenith angle, no atmospheric
                 correction (= ETR) */
public static double zenref;     /* O:  S_REFRAC   Solar zenith angle, deg. from zenith,
                 refracted */

}
