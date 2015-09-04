class MethodClass{
	public void rev_numberOfOnes(String string) {
		Integer broke_here;
		Integer return_var;
		broke_here = RESTORE();
		return_var = RESTORE();
		return_label: {
			Integer num;
			num = RESTORE();
			broke_here = RESTORE();
			return_var = RESTORE();
			{
				int i;
				i = RESTORE();
				while_loop: {
					Integer while_counter_0;
					while_counter_0 = RESTORE();
					while (while_counter_0 > 0) {
						--i;
						if_statement: {
							Boolean if_outcome_0;
							if_outcome_0 = RESTORE();
							if (if_outcome_0) {
								--num;
							}
						}
						--while_counter_0;
					}
				}
				i = RESTORE();
			}
			num = RESTORE();
		}
	}
	
	public void rev_factorial(Integer i) {
		Integer broke_here;
		Double return_var;
		broke_here = RESTORE();
		return_var = RESTORE();
		return_label: {
			broke_here = RESTORE();
			return_var = RESTORE();
			if_statement: {
				Boolean if_outcome_1;
				if_outcome_1 = RESTORE();
				if (if_outcome_1) {
					Double factorial;
					factorial = RESTORE();
					k = RESTORE();
					factorial(i - 1);
					factorial = RESTORE();
				}
			}
		}
	}
	
	public void rev_memoFactorial(Integer i) {
		fac /= i;
		num = RESTORE();
	}
	
	public void rev_reverseString() {
		String newStr;
		newStr = RESTORE();
		str = RESTORE();
		{
			int i;
			Object length_var;
			i = RESTORE();
			length_var = RESTORE();
			while_loop: {
				Integer while_counter_1;
				while_counter_1 = RESTORE();
				while (while_counter_1 > 0) {
					++i;
					str.rev_getCharAt(i);
					--while_counter_1;
				}
			}
			i = RESTORE();
			str.length();
			length_var = RESTORE();
		}
		newStr = RESTORE();
	}
}
