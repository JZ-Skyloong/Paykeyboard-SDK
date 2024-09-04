package geekmaker.com.paykeyboardsdk;

import android.util.Log;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void testCalc(){
        double result = 0.0D;
        double num1  = Double.parseDouble( "12176.38");
        double num2 =  Double.parseDouble("46.46");

        result = num1 + num2;
        String ret = (BigDecimal.valueOf(result)).toPlainString();
        Log.i("Calc",ret);

       // System.out.println((BigDecimal.valueOf(Double.parseDouble(num1) + Double.parseDouble(num2))).toPlainString());
       // assertEquals(2176.39,Float.parseFloat(num1));
       // System.out.println(Double.parseDouble(num1));
    }
}