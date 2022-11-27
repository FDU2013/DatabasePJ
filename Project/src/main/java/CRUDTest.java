import crud.RepositoryCRUD;
import entity.Repository;

public class CRUDTest {
    public static void allTest(){
        RepositoryTest();
    }

    public static void RepositoryTest(){
        Repository repository = new Repository(0,"repo1","this is repo1","/C:");
        try {
            RepositoryCRUD.insertRepository(repository);
            System.out.println("add repo1 succ");
        }catch (Exception e){
            System.out.println("fail");
        }
    }


}
