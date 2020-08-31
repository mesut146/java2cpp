package base;

public class InnerTest {

    int x = 123456;
    String str = "test";

    class Inner {

        Inner(InnerTest parent) {
          parent.toString();
        }
        
        void innerMeth() {
            //inner can access parent instance
            //anony class can access parent instance
            System.out.println(x + " = " + str + " " + InnerTest.this.x);
        }
    }
}
