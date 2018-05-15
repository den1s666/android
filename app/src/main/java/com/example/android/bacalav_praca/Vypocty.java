package com.example.android.bacalav_praca;

import java.util.ArrayList;

/**
 * Created by Denys_d on 2/19/2018.
 */

public class Vypocty {

    private static final double K1= 0.13;
    private static final double K2= 0.175;
    private static final double K3= 0.07;
    private static final double E= 2.71828;

    public static double get_lambda1()
    {
        double lambda = -(K1 + K2 + K3) + Math.sqrt(Math.pow((K1+K2+K3),2) - 4*K2*K3);
        lambda /= 2;
        return lambda;
    }
    public static double get_lambda2()
    {
        double lambda = -(K1 + K2 + K3) - Math.sqrt(Math.pow((K1+K2+K3),2) - 4*K2*K3);
        lambda /= 2;
        return lambda;
    }
    public static double get_cK0(double cI, double t) {
        double lambda1 = get_lambda1();
        double lambda2 = get_lambda2();
        double cK = cI / (lambda1 - lambda2);
        cK *= ((lambda1 + K2) * Math.pow(E, lambda1 * t) - (lambda2 + K2) * Math.pow(E, lambda2 * t));
        return cK;
    }
    public static double get_cT0(double cI, double t) {
        double lambda1 = get_lambda1();
        double lambda2 = get_lambda2();
        double cT = cI / (K1 * (lambda1 - lambda2));
        cT *= (lambda1 + K2);
        cT *= (lambda2 + K2);
        cT *= (-Math.pow(E, lambda1 * t) + Math.pow(E, lambda2 * t));
        return cT;
    }
    public static double get_cK_podmienka(double cI, double t, double T,int n) {
        double lambda1 = get_lambda1();
        double lambda2 = get_lambda2();
        double cK = (lambda1+K2)*cI*(1 - Math.pow(E, -lambda1*(n+1)*T))*Math.pow(E, lambda1*t)/(1 - Math.pow(E, -lambda1*T))
                - (lambda2+K2)*cI*(1 - Math.pow(E, -lambda2*(n+1)*T))*Math.pow(E, lambda2*t)/(1 - Math.pow(E, -lambda2*T));
        cK /= (lambda1 - lambda2);
        return cK;
    }
    public static double get_cT_podmienka(double cI, double t, double T,int n) {
        double lambda1 = get_lambda1();
        double lambda2 = get_lambda2();
        double cT = (lambda1 + K2)*(lambda2 + K2)/(K1 * (lambda1 - lambda2));
        cT *= (-cI)*(1 - Math.pow(E, -lambda1*(n+1)*T))*Math.pow(E, lambda1*t)/(1 - Math.pow(E, -lambda1*T))
                + (cI*(1 - Math.pow(E, -lambda2*(n+1)*T))*Math.pow(E, lambda2*t))/(1 - Math.pow(E, -lambda2*T));
        return cT;
    }

    public static double strHodnotaTmp(double cI, int a, int b, double T) {
        double lambda1 = get_lambda1();
        double lambda2 = get_lambda2();
        double C1 = (lambda1 + K2)*(lambda2 + K2)*(-cI)*(1 - Math.pow(E, -lambda1*(a+1)*T))/(K1 * (lambda1 - lambda2)*lambda1*(1 - Math.pow(E, -lambda1*T))*T*(b-a));
        double C2 = (lambda1 + K2)*(lambda2 + K2)*(cI)*(1 - Math.pow(E, -lambda2*(a+1)*T))/(K1 * (lambda1 - lambda2)*lambda2*(1 - Math.pow(E, -lambda2*T))*T*(b-a));
        double res = C1*(Math.pow(E, lambda1*b*T)- Math.pow(E, lambda1*a*T)) + C2*(Math.pow(E, lambda2*b*T)- Math.pow(E, lambda2*a*T));
        return res;
    }

    //==============Vypocet strednej hodnoty funkcii CT pre periodu <aT;bT> ============
    public static double getStrHodnota(double cI, int a, int b, int perioda)
    {
        ArrayList<Double> max = new ArrayList<Double>();

        for(int j=a;j<b;j++)
        {

            max.add(strHodnotaTmp(cI, a, a+1, perioda));
        }

        double sum = 0;
        for(int j=0;j<max.size();j++){
            sum +=max.get(j);
        }
        sum/=(max.size());
        return sum;
    }

}
