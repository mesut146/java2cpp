public class ArrayTest{

    String str[][] = {{"test"}, {"asd"}};
    long mylong[];
    short myshort[][]=new short[10][20];
    int[][][][] arr4 = new int[1][2][3][str.length];
    
    public static void main(String[] args){
      System.out.println("str dims="+str.length+","+str[0].length);
    }
}
