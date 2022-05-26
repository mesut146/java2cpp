#pragma once


namespace java{
namespace lang{

class Math
{
//fields
public:
    static bool $assertionsDisabled;
    static double E;
    static double PI;
    static long negativeZeroDoubleBits;
    static long negativeZeroFloatBits;
    static double twoToTheDoubleScaleDown;
    static double twoToTheDoubleScaleUp;

//methods
public:
    Math();

    static double IEEEremainder(double , double );

    static int abs(int );

    static long abs(long );

    static float abs(float );

    static double abs(double );

    static double acos(double );

    static int addExact(int , int );

    static long addExact(long , long );

    static double asin(double );

    static double atan(double );

    static double atan2(double , double );

    static double cbrt(double );

    static double ceil(double );

    static double copySign(double , double );

    static float copySign(float , float );

    static double cos(double );

    static double cosh(double );

    static int decrementExact(int );

    static long decrementExact(long );

    static double exp(double );

    static double expm1(double );

    static double floor(double );

    static int floorDiv(int , int );

    static long floorDiv(long , long );

    static int floorMod(int , int );

    static long floorMod(long , long );

    static int getExponent(float );

    static int getExponent(double );

    static double hypot(double , double );

    static int incrementExact(int );

    static long incrementExact(long );

    static double log(double );

    static double log10(double );

    static double log1p(double );

    static int max(int , int );

    static long max(long , long );

    static float max(float , float );

    static double max(double , double );

    static int min(int , int );

    static long min(long , long );

    static float min(float , float );

    static double min(double , double );

    static int multiplyExact(int , int );

    static long multiplyExact(long , long );

    static int negateExact(int );

    static long negateExact(long );

    static double nextAfter(double , double );

    static float nextAfter(float , double );

    static double nextDown(double );

    static float nextDown(float );

    static double nextUp(double );

    static float nextUp(float );

    static double pow(double , double );

    static double powerOfTwoD(int );

    static float powerOfTwoF(int );

    static double random();

    static double rint(double );

    static int round(float );

    static long round(double );

    static double scalb(double , int );

    static float scalb(float , int );

    static double signum(double );

    static float signum(float );

    static double sin(double );

    static double sinh(double );

    static double sqrt(double );

    static int subtractExact(int , int );

    static long subtractExact(long , long );

    static double tan(double );

    static double tanh(double );

    static double toDegrees(double );

    static int toIntExact(long );

    static double toRadians(double );

    static double ulp(double );

    static float ulp(float );


};//class Math

}//namespace java
}//namespace lang
