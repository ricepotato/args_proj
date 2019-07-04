import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {
    Args arg = null;

    @BeforeEach
    void setUp() {
        String[] input = new String[3];
        input[0] = "-l";
        input[1] = "-d";
        input[2] = "3";

        try {
            arg = new Args("l,d*", input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void testArgs() {
        boolean logging = arg.getBoolean('l');
        assertTrue(logging);
        String value = arg.getString('d');
        assertEquals("3", value);
    }
}