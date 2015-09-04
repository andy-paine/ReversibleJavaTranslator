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
        double h = 52 + inlineInvoc();
        int x = 5;
        int y;
        y = 5;
        x++;
        y = x;
        y = x++;
        ++x;
        {
            int z = 12;
            z = ++x;
			z += x;
			z /= x;
        }

        for (int i=0; i<10 && i++<11 && ++i==x; i++) {
            i--;
        }

        do {
            y++;
            y = y;
            x = y;
        } while (y<10);
		
		label: {
        	int j = 0;
        	j++;
        	--j;
        }

        for (int i=0; i<20; i++) {
            i++;
            x = i;
        }
    }

    public int test() {
        int i=0;

		for (int j=0; j<2; j++) {
			if (i==1)
				return i;
			else
				return 0;
		}        

    }
	
	public int test3() {
		for (int j=0; j<10; j++) {
			double rand = Math.random();
			if (rand < 0.5) {
				int y = 5;
				return (int)(rand*100*y);
			}
		}
		return 0;
	}
	
	public int test2() {
		for (int i=0; i<12; i++) {
			if (i*i > 100)
				return i*i;
			else 
				i += 2;
		}
		
		return 0;
	}
	
	public void rev_random() {
		int rand;
		Object random_var;
		rand = RESTORE();
		random_var = RESTORE();
		{
			int j;
			j = RESTORE();
			while_loop: {
				int while_counter_0;
				while_counter_0 = RESTORE();
				while (while_counter_0 > 0) {
					--j;
					if_statement: {
						boolean if_outcome_0;
						if_outcome_0 = RESTORE();
						if (if_outcome_0) {
							i /= rand;
						}
					}
					--while_counter_0;
				}
			}
			j = RESTORE();
		}
		rand = RESTORE();
		Math.random();
		random_var = RESTORE();
	}
}