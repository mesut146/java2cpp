//package base;

public class Try2 {

    static int func(){
      System.out.println("func()");
      return 5;
    }
    
    public static void main(String[] arg){
      System.out.println(with_finally());
    }

    //lambda
    static int with_finally() {
        try {
            if (1 == 1) {
                return func();
            }
            //return func();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        finally{
            System.out.println("finally");
            //return 6;
        }
        return 0;
    }

}
