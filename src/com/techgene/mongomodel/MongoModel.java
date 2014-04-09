package com.techgene.mongomodel;

import java.net.UnknownHostException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;


public class MongoModel {
public static void main(String[] args) throws UnknownHostException, TasteException {
	
	MongoDBDataModel dbm = new MongoDBDataModel("localhost", 27017, "mahout", "ratings", true, true, null);
    SVDRecommender svd = new SVDRecommender(dbm, new ALSWRFactorizer(dbm, 3, 0.05f, 50));

   for(LongPrimitiveIterator users = dbm.getUserIDs(); users.hasNext();) {
	   long userId=users.nextLong();
	   //System.out.println(userId);
    for(LongPrimitiveIterator items = dbm.getItemIDs(); items.hasNext();) {
		 long itemId=items.nextLong();
		 //System.out.println(itemId);
		 //System.out.println("***************");
		 System.out.println(svd.estimatePreference(userId,itemId));
		 
		 
	}
   }
    
}
}
