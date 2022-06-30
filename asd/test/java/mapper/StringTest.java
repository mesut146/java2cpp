package mapper;

public class StringTest {

    public static void main(String[] aa) {
        byte[] buf = new byte[]{'h', 'e', 'l', 'l', 'o'};
        String str = new String(buf);
        String str2 = new String("world");
        String str3 = new String(new char[]{'f', 'o', 'o'});
        char c = str.charAt(0);
        String sub = str.substring(1, 2);
        int len = str.length();
        int idx = str.indexOf('h');
        int idx2 = str.indexOf("lo");
        int idx3 = str.indexOf(str);
        String con1 = str + " world";
        boolean start = str.startsWith("he");
        boolean end = str.endsWith("lo");
    }

}
