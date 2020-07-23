package base;

public class InnerTest {

    int x = 123456;
    String str = "test";

    class Inner {

        void innerMeth() {
            //inner can access parent instance
            System.out.println(x + " = " + str);
        }
    }
}