package sonar;

@FunctionalInterface
public interface ImportInstance {
    boolean imp(String phone);

    default String getInfo() {
        return "导入IssueInstance策略";
    }
}
