package utils;

import java.util.Random;

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


    public static void main(String[] args) {
        System.out.println(getRandomIntNotEqual(1, 2));
        System.out.println(getRandomIntNotEqual(1, 2));
        System.out.println(getRandomIntNotEqual(1, 2));
        System.out.println(getRandomIntNotEqual(1, 2));
        System.out.println(getRandomIntNotEqual(1, 2));


    }


}
