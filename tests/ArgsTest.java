import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {
    Args arg = null;

    @BeforeEach
    void setUp() {
        String[] input = new String[1];
        input[0] = "-l";
        arg = new Args("l", input);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void isValid() {
    }

    @Test
    void cardianlity() {
    }

    @Test
    void useage() {
    }

    @Test
    void errorMessage() {
    }

    @Test
    void getBoolean() {
        boolean logging = arg.getBoolean('l');
        assertTrue(logging);
    }
}