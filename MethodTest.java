package com.company;

/**
 * Created by Andy on 11/08/2015.
 */
public class Test {

    public static void main(String[] args) {
        String hello = "hello";
        TestClass test = new Test();
        hello = methodInvoc(methodInvoc2(), methodInvoc3());
        randomMethodInvoc();        
    }

    public int test() {
        int i=0;

        if (i==1)
            return i;
        else
            return 0;

    }
	
	private static int staticTest(int arg1, double arg2) {
		doSomething(arg1);
		int z = doAnotherThing(arg2);
		return z;
	}
}