package eu.monnetproject.clesa.core.utils;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 *  
 * @author kasooja 
 */

public class VectorUtils<X>{
	public double cosineProduct(Vector<X> vector1,Vector<X> vector2){
		if(vector1 == null || vector2 == null)
			return 0.0;
		
		double magnitudeVector1 = 0.0;
		double magnitudeVector2 = 0.0;
		double dotProduct = 0.0;
	
		
		Map<X, Double> map1 = vector1.getVectorAsMap();
		Map<X, Double> map2 = vector2.getVectorAsMap();

		for(Object unitVector : map1.keySet()){

			Double score1 = map1.get(unitVector);
			Double score2 = 0.0;

			if(map2.containsKey(unitVector))
				score2 = map2.get(unitVector);			

			dotProduct = dotProduct + score1*score2;

			magnitudeVector1 = magnitudeVector1 + score1*score1;

		}

		for(Object unitVector : map2.keySet()) {
			Double score2 = map2.get(unitVector);
			magnitudeVector2 = magnitudeVector2 + score2*score2;			
		}

		if((magnitudeVector1 > 0.0)&&(magnitudeVector2 > 0.0))
			dotProduct = dotProduct /((Math.sqrt(magnitudeVector1) * Math.sqrt(magnitudeVector2)));
		else
			dotProduct = 0;

		return dotProduct;
	}

	public Vector<X> add(List<Vector<X>> vectorsList) {
		Map<X, Double> additionMap = new HashMap<X, Double>();
		List<Vector<X>> updatedList = new ArrayList<Vector<X>>(vectorsList);
		for(Vector<X> vector : vectorsList) {
			Map<X, Double> map = vector.getVectorAsMap();
			updatedList.remove(vector);	
			for(X x : map.keySet()) {				
				double sum = map.get(x);				
				for(Vector<X> otherVector : updatedList) {		
					Map<X, Double> otherMap = otherVector.getVectorAsMap();
					if(otherMap.containsKey(x)) {
						sum = sum + otherMap.get(x).doubleValue();
					}
					otherMap.remove(x);
				}
				additionMap.put(x, new Double(sum));
			}
		}
		return new Vector<X>(additionMap);

	}

	public Vector<X>  vectorWithPrincipalDimensions(Vector<X> vector, int noOfPrincipalDimensions) {
		if(vector == null) 
			return null;
		Map<X, Double> sortedMap = new HashMap<X, Double>();
		Map<X, Double> vectorMap = vector.getVectorAsMap();
		List<X> keys = new ArrayList<X>(vectorMap.keySet());
		List<Double> values = new ArrayList<Double>(vectorMap.values());		
		Collections.sort(values);
		Collections.reverse(values);		
		for(Double value : values) {
			for(X key : keys) {
				if (vectorMap.get(key).doubleValue() == value.doubleValue()) {
					sortedMap.put(key, value);
					if((sortedMap.size() >= noOfPrincipalDimensions)) 
						return new Vector<X> (sortedMap);
				}
			}
		}
		return new Vector<X>(sortedMap);
	}
}
