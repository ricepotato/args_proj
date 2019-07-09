import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {
    Args arg = null;

    @BeforeEach
    void setUp() {
        
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void testArgs() {
        String[] input = new String[5];
        input[0] = "-l";
        input[1] = "-d";
        input[2] = "3";
        input[3] = "-p";
        input[4] = "6";

        try {
            arg = new Args("l,d*,p#", input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        boolean logging = arg.getBoolean('l');
        assertTrue(logging);
        String value = arg.getString('d');
        assertEquals("3", value);
        int intVal = arg.getInt('p');
        assertEquals(intVal, 6);
    }

    @Test
    void testArgsParseException() {
        String[] input = new String[2];
        input[0] = "-p";
        input[1] = "3";

        Assertions.assertThrows(ParseException.class, () -> {
            arg = new Args("3#", input);
        });
    }
}