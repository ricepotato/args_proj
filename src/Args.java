import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;


public class Args {
    private String schema;
    private String[] args;
    private boolean valid;
    private Set<Character> unexpectedArguments = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaler> booleanArgs = new HashMap<Character, ArgumentMarshaler>();
    private Map<Character, ArgumentMarshaler> stringArgs = new HashMap<Character, ArgumentMarshaler>();
    private Map<Character, ArgumentMarshaler> intArgs = new HashMap<Character, ArgumentMarshaler>();
    private Set<Character> argsFound = new HashSet<Character>();
    private int currentArgument;
    private char errorArgumentId = '\0';
    private String errorParameter = "TILT";
    private ErrorCode errorCode = ErrorCode.OK;

    enum ErrorCode {
        OK, MISSING_STRING, MISSING_INTEGER, INVALID_INTEGER, UNEXPECTED_ARGUMENT
    }

    public Args(String schema, String[] args) throws ParseException {
        this.schema = schema;
        this.args = args;
        valid = parse();
    }

    private boolean parse() throws ParseException {
        if (schema.length() == 0 && args.length == 0){
            return true;
        }
        parseSchema();
        try {
            parseArguments();
        }catch(ArgsException e){

        }
        return valid;
    }

    private boolean parseSchema() throws ParseException {
        for (String element : schema.split(",")){
            if (element.length() > 0) {
                String trimmedElement = element.trim();
                parseSchemaElement(trimmedElement);
            }
        }
        return true;
    }

    private void parseSchemaElement(String element) throws ParseException{
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if (isBooleanSchemaElement(elementTail))
            parseBooleanSchemaElement(elementId);
        else if (isStringSchemaElement(elementTail))
            parseStringSchemaElement(elementId);
        else if (isIntegerSchemaElement(elementTail))
            parseIntegerSchemaElement(elementId);
        else{
            throw new ParseException(String.format("Argument: %c has invalid format: %s",
                    elementId, elementTail), 0);
        }
    }

    private void validateSchemaElementId(char elementId) throws ParseException {
        if (!Character.isLetter(elementId)){
            throw new ParseException("Bad character:" + elementId + "in Args format: " + schema, 0);
        }
    }
    private void parseBooleanSchemaElement(char elementId) {
        booleanArgs.put(elementId, new BooleanArgumentMarshaler());
    }

    private void parseStringSchemaElement(char elementId) {
        stringArgs.put(elementId, new StringArgumentMarshaler());
    }

    private void parseIntegerSchemaElement(char elementId) {
        intArgs.put(elementId, new IntegerArgumentMarshaler());
    }

    private boolean isIntegerSchemaElement(String elementTail) {
        return elementTail.equals("#");
    }

    private boolean isStringSchemaElement(String elementTail) {
        return elementTail.equals("*");
    }

    private boolean isBooleanSchemaElement(String elementTail) {
        return elementTail.length() == 0;
    }

    private boolean parseArguments() throws ArgsException {
        for (currentArgument = 0 ; currentArgument < args.length; currentArgument++ ){
            String arg = args[currentArgument];
            parseArgument(arg);
        }
        return true;
    }

    private void parseArgument(String arg) throws ArgsException {
        if (arg.startsWith("-")){
            parseElements(arg);
        }
        
    }

    private void parseElements(String arg) throws ArgsException {
        for (int i = 1 ; i < arg.length(); i++){
            parseElement(arg.charAt(i));
        }
    }

    private void parseElement(char argChar) throws ArgsException {
        if (setArgument(argChar))
            argsFound.add(argChar);
        else{
            unexpectedArguments.add(argChar);
            errorCode = ErrorCode.UNEXPECTED_ARGUMENT;
            valid = false;
        }
    }

    private boolean setArgument(char argChar) throws ArgsException {
        boolean set = true;
        if (isBooleanArg(argChar))
            setBooleanArg(argChar, true);
        else if (isStringArg(argChar))
            setStringArg(argChar, "");
        else if (isIntArg(argChar))
            setIntArg(argChar);
        else
            set = false;

        return set;
    }

    private boolean isIntArg(char argChar) {
        return intArgs.containsKey(argChar);
    }

    private void setIntArg(char argChar) throws ArgsException {
        currentArgument++;
        String parameter = null;
        try{
            parameter = args[currentArgument];
            intArgs.get(argChar).setInteger(Integer.parseInt(parameter));
        }catch (ArrayIndexOutOfBoundsException e){
            valid = false;
            errorArgumentId = argChar;
            errorCode = ErrorCode.MISSING_INTEGER;
            throw new ArgsException();
        }catch (NumberFormatException e){
            valid = false;
            errorArgumentId = argChar;
            errorParameter = parameter;
            errorCode = ErrorCode.INVALID_INTEGER;
            throw new ArgsException();
        }
    }

    private void setStringArg(char argChar, String s) throws ArgsException {
        currentArgument++;
        try{
            stringArgs.get(argChar).setString(args[currentArgument]);
        }catch(ArrayIndexOutOfBoundsException e){
            valid = false;
            errorArgumentId = argChar;
            errorCode = ErrorCode.MISSING_STRING;
            throw new ArgsException();
        }
    }

    private boolean isStringArg(char argChar){
        return stringArgs.containsKey(argChar);
    }

    private void setBooleanArg(char argChar, boolean value) {
        booleanArgs.get(argChar).set("true");
    }

    private boolean isBooleanArg(char argChar) {
        return booleanArgs.containsKey(argChar);
    }

    public int cardianlity(){
        return argsFound.size();
    }

    public String useage(){
        if (schema.length() > 0)
            return "-[" + schema + "]";
        else
            return "";
    }

    public String errorMessage() throws Exception{
        switch (errorCode){
            case OK:
                throw new Exception("TILT: Should not get here.");
            case UNEXPECTED_ARGUMENT:
                return unexpectedArgumentMessage();
            case MISSING_STRING:
                return String.format("Could not find string parameter for -%c.",
                        errorArgumentId);
            case INVALID_INTEGER:
                return String.format("Argument -%c expects an integer but was '%s'.",
                        errorArgumentId, errorParameter);
            case MISSING_INTEGER:
                return String.format("Could not find integer parameter for -%c.",
                        errorArgumentId);
        }
        return "";
    }

    private String unexpectedArgumentMessage() {
        StringBuffer message = new StringBuffer("Argument(s) -");
        for (char c : unexpectedArguments){
            message.append(c);
        }
        message.append(" unexpeced");

        return message.toString();
    }

    public boolean getBoolean(char arg){
        Args.ArgumentMarshaler am = booleanArgs.get(arg);
        return am != null && (Boolean)am.get();
    }

    private boolean falseIfNull(Boolean b){
        return b == null ? false : b;
    }

    private int zeroIfNull(Integer i){
        return i == null ? 0 : i;
    }

    public String getString(char arg){
        Args.ArgumentMarshaler am = stringArgs.get(arg);
        return am == null ? "" : am.getString();
    }

    private String blankIfNull(String s){
        return s == null ? "" : s;
    }

    public int getInt(char arg){
        Args.ArgumentMarshaler am = intArgs.get(arg);
        return am == null ? 0 : am.getInteger();
    }

    public boolean has(char arg){
        return argsFound.contains(arg);
    }

    public boolean isValid(){
        return valid;
    }

    private class ArgsException extends Exception{

    }

    private abstract class ArgumentMarshaler{
        private String stringValue;
        private int integerValue;

        public void setString(String s){
            stringValue = s;
        }

        public String getString(){
            return stringValue == null ? "" : stringValue;
        }

        public void setInteger(int i){
            integerValue = i;
        }

        public int getInteger(){
            return integerValue;
        }

        public abstract void set(String s);
        public abstract Object get();
    }

    private class BooleanArgumentMarshaler extends ArgumentMarshaler{
        private boolean booleanValue = false;

        @Override
        public void set(String s) {
            booleanValue = true;
        }

        public Object get(){
            return booleanValue;
        }
    }

    private class StringArgumentMarshaler extends ArgumentMarshaler{
        @Override
        public void set(String s) {
        }

        @Override
        public Object get() {
            return null;
        }
    }

    private class IntegerArgumentMarshaler extends ArgumentMarshaler{
        @Override
        public void set(String s) {
        }

        @Override
        public Object get() {
            return null;
        }
    }
}

