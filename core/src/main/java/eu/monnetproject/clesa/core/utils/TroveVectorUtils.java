package eu.monnetproject.clesa.core.utils;

import gnu.trove.map.hash.TIntDoubleHashMap;

public class TroveVectorUtils {
	
	public static double cosineProduct(TIntDoubleHashMap map1, TIntDoubleHashMap map2){
		if(map1 == null || map2 == null)
			return 0.0;		
		double magnitudeVector1 = 0.0;
		double magnitudeVector2 = 0.0;
		double dotProduct = 0.0;					
		TIntDoubleHashMap iteratingMap = null;
		TIntDoubleHashMap secMap = null;		
		if(map1.size() >= map2.size()){
			iteratingMap = map1;
			secMap = map2;
		}
		else {
			iteratingMap = map2;
			secMap = map1;
		}		
		int[] secKeys = secMap.keys();
		int i = 0;		
		for(int unitVector : iteratingMap.keys()){
			double score1 =  iteratingMap.get(unitVector);
			double score2 = 0.0;
			if(secMap.containsKey(unitVector)){
				score2 = secMap.get(unitVector);
				dotProduct = dotProduct + score1*score2;
			}
			if(i < secKeys.length){
				score2 = secMap.get(secKeys[i++]);
				magnitudeVector2 = magnitudeVector2 + score2 * score2;			
			}		
			magnitudeVector1 = magnitudeVector1 + score1*score1;
		}

		if((magnitudeVector1 > 0.0)&&(magnitudeVector2 > 0.0))
			dotProduct = dotProduct /((Math.sqrt(magnitudeVector1) * Math.sqrt(magnitudeVector2)));
		else
			dotProduct = 0;

		return dotProduct;
	}


}
