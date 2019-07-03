public class Main {

    public static void main(String[] args) {
        String[] input = new String[1];
        input[0] = "-l";
        Args arg = new Args("l", input);
        boolean logging = arg.getBoolean('l');
        System.out.println(logging);
    }
}
