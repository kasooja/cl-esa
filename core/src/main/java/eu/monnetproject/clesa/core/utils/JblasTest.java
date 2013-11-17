package eu.monnetproject.clesa.core.utils;

import org.jblas.DoubleMatrix;

public class JblasTest {
public static void main(String[] args) {
	double [][] vectors = new double[100][4200000];
	for(int count = 0; count < 100; count++){
		vectors[count][0] = count +3.0;
		vectors[count][5002] = count +5.0;
		vectors[count][200000] = count +5.0;
		vectors[count][300000] = count +5.0;
	}
	
	DoubleMatrix matrix = new DoubleMatrix(vectors);
	System.out.println(matrix);
	
	DoubleMatrix mul2 = matrix.mul(matrix);
	System.out.println(mul2);
}
}
