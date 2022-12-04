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

    public static CASE_TYPE String2CaseType(String s){
        switch (s){
            case "BUG":return CASE_TYPE.BUG;
            case "VULN":return CASE_TYPE.VULN;
            case "SMELL":return CASE_TYPE.SMELL;
            case "SECHOT": return CASE_TYPE.SECHOT;
            default: return null;
        }
    }

    public static CASE_STATUS String2CaseStatus(String s){
        switch (s){
            case "SOLVED":return CASE_STATUS.SOLVED;
            case "UNSOLVED":return CASE_STATUS.UNSOLVED;
            default: return null;
        }
    }

    public static INSTANCE_STATUS String2InstanceStatus(String s){
        switch (s){
            case "APPEAR":return INSTANCE_STATUS.APPEAR;
            case "UPDATE":return INSTANCE_STATUS.UPDATE;
            case "DISAPPEAR":return INSTANCE_STATUS.DISAPPEAR;
            default: return null;
        }
    }
}
