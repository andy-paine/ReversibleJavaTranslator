class MethodClass{
	public Integer numberOfOnes(String string) {
		Integer num = 0;
		for (int i=0; i<string.length(); i++) {
			if (string.charAt(i).equals('1')) {
				num++;
			}				
		}
		return num;
	}
	
	public Double factorial(Integer i) {
		
		if (i>0) {
			Double factorial = factorial(i-1);
			k = factorial*i;
		}
		
		return k;
	}
	
	public void memoFactorial(Integer i) {
		num = i;
		fac *= i;
	}
}
