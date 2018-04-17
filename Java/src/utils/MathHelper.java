package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class MathHelper {


    public static int getRandomIntNotEqual(int first, int max) {

        int second;
        if(first == 0) {
            second = (int) Math.floor(Math.random()) * (max-1) + 1;
        } else if(first == max-1) {
            second = (int) Math.floor(Math.random()) * (max-1);
        } else {
            second = (Math.random() < (first + 0.0)/max ) ? (int)Math.floor(Math.random() * first) : (int)Math.floor(Math.random()*(max - first-1 ))+(first+1);
        }
        return second;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int getRandomGreater(int first, int max){
        int second;
        second = (int) Math.floor(Math.random() * (max - first - 1)) + first + 1;
        return second;
    }





}
