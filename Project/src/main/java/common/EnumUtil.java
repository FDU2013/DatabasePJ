package common;

public class EnumUtil {
    public static String Enum2String(CASE_TYPE type){
        switch (type){
            case BUG:return "BUG";
            case VULN:return "VULN";
            case SMELL:return "SMELL";
            case SECHOT:return "SECHOT";
            default: return "";
        }
    }
    public static String Enum2String(CASE_STATUS status){
        switch (status){
            case SOLVED:return "SOLVED";
            case UNSOLVED:return "UNSOLVED";
            default: return "";
        }
    }

    public static String Enum2String(INSTANCE_STATUS status){
        switch (status){
            case APPEAR:return "APPEAR";
            case UPDATE:return "UPDATE";
            case DISAPPEAR:return "DISAPPEAR";
            default: return "";
        }
    }
}
