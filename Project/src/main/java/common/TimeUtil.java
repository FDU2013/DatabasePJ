package common;

import java.sql.Timestamp;

public class TimeUtil {

    public static long getTimeDifference(Timestamp endTime, Timestamp startTime) {
        long t1 = endTime.getTime();
        long t2 = startTime.getTime();
        return t1-t2;
    }


    public static String getTimeDifferenceString(Timestamp endTime, Timestamp startTime) {
        long t1 = endTime.getTime();
        long t2 = startTime.getTime();
        int days =  (int) ((t1 - t2)/(1000*60*60*24));
        int hours=(int) ((t1 - t2)/(1000*60*60)%24);
        return ""+days+"天"+hours+"小时";
    }

    public static String getTimeDifferenceString(long diff) {
        int days =  (int) (diff/(1000*60*60*24));
        int hours=(int) (diff/(1000*60*60)%24);
        return ""+days+"天"+hours+"小时";
    }
}
