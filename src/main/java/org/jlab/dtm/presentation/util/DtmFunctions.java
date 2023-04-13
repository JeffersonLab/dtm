package org.jlab.dtm.presentation.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.view.User;

/**
 *
 * @author ryans
 */
public final class DtmFunctions {

    private static final Logger logger = Logger.getLogger(
            DtmFunctions.class.getName());

    private DtmFunctions() {
        // cannot instantiate publicly
    }

    public static Date getTimelineTickDate(double percent, long duration, Date start) {
        long offsetMillis = (long) ((percent / 100) * duration);
        Date tickDate = new Date(start.getTime() + offsetMillis);
        return tickDate;
    }

    public static String addS(int x) {
        if (x != 1) {
            return "s";
        } else {
            return "";
        }
    }

    public static String millisToHumanReadable(long milliseconds, boolean stacked) {
        String time;
        if (milliseconds < 60000) {
            int seconds = (int) Math.floor(milliseconds / 1000);
            time = seconds + " second" + addS(seconds);
        } else {
            int hours = (int) Math.floor((milliseconds) / 3600000),
                    remainingMilliseconds = (int) (milliseconds % 3600000),
                    minutes = (int) Math.floor(remainingMilliseconds / 60000);

            time = (hours > 0 ? hours + " hour"
                    + addS(hours) + " " + (stacked ? "\n" : "") : "") + minutes + " minute"
                    + addS(minutes);
        }

        return time;
    }

    public static String millisToAbbreviatedHumanReadable(long milliseconds) {
        int hours = (int) Math.floor((milliseconds) / 3600000),
                remainingMilliseconds = (int) (milliseconds % 3600000),
                minutes = (int) Math.floor(remainingMilliseconds / 60000);

        return (hours > 0 ? hours + "h" + " " : "") + minutes + "m";
    }

    public static String urlEncodeDateTime(Date value) {
        SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());
        String str = formatter.format(value);
        String encoded = "";

        try {
            encoded = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("UTF-8 is unsupported!");
        }
        return encoded;
    }

    public static String formatLogbookDate(Date date, int offsetHours) {
        String result;

        if (date == null) { // Just use current time if input is null
            date = new Date();
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, offsetHours);

        //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //result = formatter.format(cal.getTime());
        // URL Encode?
        result = String.valueOf(cal.getTimeInMillis() / 1000); // Logbook supports UNIX timestamp format

        return result;
    } 
    
    // Weekly Repair Report
    public static String formatSmartDate(Date date) {
        String format = "EEE, MMM d, HH:mm";
        //String format = "EEE dd-MMM-yyyy HH:mm";
        
        if(date == null) {
            date = new Date();
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        
        /*if(cal.get(Calendar.MINUTE) != 0 || cal.get(Calendar.HOUR_OF_DAY) != 0) {
            format = "EEE dd-MMM-yyyy HH:mm";
        } else {
            format = "EEE dd-MMM-yyyy";
        }*/
        
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        
        return formatter.format(date);
    }
    
    public static String formatConciseSmartDate(Date date) {
        String format = "EEE, MMM d";
        
        if(date == null) {
            date = new Date();
        }
        
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        
        return formatter.format(date);
    }    

    public static Date nDaysAgo(int n) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -n);
        return c.getTime();
    }

    public static String elapsedClasses(long elapsedMillis) {
        String classes = "";

        if (elapsedMillis > 14400000) { // 4 Hours
            classes = "time-elapsed-danger";
        } else if (elapsedMillis > 7200000) { // 2 minutes
            classes = "time-elapsed-warning";
        }

        return classes;
    }

    public static String getHostnameFromIp(String ip) {
        String hostname = ip;

        if (ip != null) {
            try {
                InetAddress address = InetAddress.getByName(ip);
                hostname = address.getHostName();

                if (!ip.equals(hostname)) {
                    hostname = hostname + " (" + ip + ")";
                }
            } catch (UnknownHostException e) {
                // Unable to resolve... oh well, just use ip
            }
        }

        return hostname;
    }

    public static User lookupUserByUsername(String username) {
        UserAuthorizationService auth = UserAuthorizationService.getInstance();

        return auth.getUserFromUsername(username);
    }

    // TODO: This should be moved to smoothness weblib
    public static String formatUsername(String username) {
        User user = lookupUserByUsername(username);

        if(user != null) {
            return formatUser(user);
        } else {
            return username;
        }
    }

    public static String formatUser(User user) {
        StringBuilder builder = new StringBuilder();

        if (user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
            if(user.getFirstname() == null || user.getLastname() == null ||
                    user.getFirstname().isEmpty() || user.getLastname().isEmpty()) {
                builder.append("(");
                builder.append(user.getUsername());
                builder.append(")");
            } else {
                builder.append(user.getLastname());
                builder.append(", ");
                builder.append(user.getFirstname());
                builder.append(" (");
                builder.append(user.getUsername());
                builder.append(")");
            }
        }

        return builder.toString();
    }

    public static String formatGroupList(String idCsv, List<Workgroup> groupList) {
        String groups = null;
        if (idCsv != null && !idCsv.trim().isEmpty()) {
            String[] tokens = idCsv.split(",");

            try {
                if (tokens.length > 0) {

                    BigInteger id = new BigInteger(tokens[0]);

                    for (Workgroup group : groupList) {
                        if (group.getWorkgroupId().equals(id)) {
                            groups = group.getName();
                            break;
                        }
                    }

                }

                if (tokens.length > 1) {
                    for (int i = 1; i < tokens.length; i++) {
                        groups = groups + ", ";

                        BigInteger id = new BigInteger(tokens[i]);

                        for (Workgroup group : groupList) {
                            if (group.getWorkgroupId().equals(id)) {
                                groups = groups + group.getName();
                                break;
                            }
                        }
                    }
                }
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Unable to parse integer in formatGroupList");
                return null;
            }

        }
        return groups;
    }

    /**
     * Returns the number of milliseconds since Jan 01 1970, but in local time, not UTC like usual.
     * This is useful because web browsers / JavaScript generally can't figure out daylight savings
     * or timezone offsets for varying points in time (they generally only know the fixed/constant
     * offset being applied on the client at present).
     *
     * @param date The date (milliseconds since Epoch in UTC)
     * @return milliseconds since Epoch in local time
     */
    public static long getLocalTime(Date date) {
        if(date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        long localOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);

        return cal.getTimeInMillis() + localOffset;
    }
}
