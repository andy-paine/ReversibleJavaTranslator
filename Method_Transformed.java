class MethodClass{
	public Integer numberOfOnes(String string) {
		Integer broke_here;
		Integer return_var;
		return_label: {
			Integer num;
			SAVE(num);
			num = 0;
			{
				int i;
				SAVE(i);
				i = 0;
				while_loop: {
					Integer while_counter_0;
					while_counter_0 = 0;
					while (i < string.length()) {
						while_counter_0++;
						if_statement: {
							Boolean if_outcome_0;
							if_outcome_0 = !!(string.charAt(i).equals('1'));
							if (if_outcome_0) {
								num++;
							}
							SAVE(if_outcome_0);
						}
						i++;
					}
					SAVE(while_counter_0);
				}
				SAVE(i);
			}
			SAVE(return_var);
			return_var = num;
			SAVE(broke_here);
			broke_here = 0;
			SAVE(num);
			SAVE(num);
			break return_label;
		}
		SAVE(return_var);
		SAVE(broke_here);
		return return_var;
	}
	
	public Double factorial(Integer i) {
		Integer broke_here;
		Double return_var;
		return_label: {
			if_statement: {
				Boolean if_outcome_1;
				if_outcome_1 = !!(i > 0);
				if (if_outcome_1) {
					Double factorial;
					SAVE(factorial);
					factorial = factorial(i - 1);
					SAVE(k);
					k = factorial * i;
					SAVE(factorial);
				}
				SAVE(if_outcome_1);
			}
			SAVE(return_var);
			return_var = k;
			SAVE(broke_here);
			broke_here = 1;
			break return_label;
		}
		SAVE(return_var);
		SAVE(broke_here);
		return return_var;
	}
	
	public void memoFactorial(Integer i) {
		SAVE(num);
		num = i;
		fac *= i;
	}
	
	public void reverseString() {
		String newStr;
		SAVE(newStr);
		newStr = "";
		{
			int i;
			Object length_var;
			SAVE(length_var);
			length_var = str.length();
			SAVE(i);
			i = length_var - 1;
			while_loop: {
				Integer while_counter_1;
				while_counter_1 = 0;
				while (i >= 0) {
					while_counter_1++;
					newStr += str.getCharAt(i);
					i--;
				}
				SAVE(while_counter_1);
			}
			SAVE(length_var);
			SAVE(i);
		}
		SAVE(str);
		str = newStr;
		SAVE(newStr);
	}
}
