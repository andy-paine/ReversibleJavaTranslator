class MethodClass{
	public Integer numberOfOnes(String string) {
		Integer return_var;
		Integer broke_here;
		return_label: {
			Integer num;
			num = 0;
			{
				int i;
				i = 0;
				while (i < string.length()) {
					if (string.charAt(i).equals('1')) {
						num++;
					}
					i++;
				}
			}
			return_var = num;
			broke_here = 0;
			break return_label;
		}
		return return_var;
	}
	
	public Double factorial(Integer i) {
		Double return_var;
		Integer broke_here;
		return_label: {
			if (i > 0) {
				Double factorial;
				factorial = factorial(i - 1);
				k = factorial * i;
			}
			return_var = k;
			broke_here = 1;
			break return_label;
		}
		return return_var;
	}
	
	public void memoFactorial(Integer i) {
		num = i;
		fac *= i;
	}
	
	public void reverseString() {
		String newStr;
		newStr = "";
		{
			Object length_var;
			length_var = str.length();
			int i;
			i = length_var - 1;
			while (i >= 0) {
				newStr += str.getCharAt(i);
				i--;
			}
		}
		str = newStr;
	}
}
