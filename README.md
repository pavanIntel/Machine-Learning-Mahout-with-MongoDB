Machine-Learning-Mahout-with-MongoDB
====================================
The goal of this article is to show how to setup a very simple recommendation engine on top of a MongoDB in combination with Apache’s open source machine learning library Mahout. The recommender engine / collaborative filtering code inside Mahout was formerly a separate project called Taste and has continued development inside Mahout alongside other Hadoop-based code. Our simple recommendation engine should implement a matrix factorization method, in order to calculate and add the missing ratings of users within a sparse matrix of given user ratings.



All implementations of the interface org.apache.mahout.cf.taste.recommender.Recommender are intended to give the user a number of recommended items, or simply to return the specific rating for a single item. The Recommender interface is based upon a specific implementation of the DataModel interface, which stands for the data repository that holds and persists the user ratings. Mahout gives you a predefined set of already supported DataModel implementations, containing file based repositories as well as Apache Hadoop or MySQL repositories. Since version 0.6 Mahout also added a MongoDB DataModel implementation (org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel). You can find the class MongoDBDataModel in a second Mahout package, which is called integration, it is not included in the core Mahout package.

Now we build an example of such a document in your MongoDB:MongoDBDataModel represents a DataModel backed by a MongoDB database. This class expects a collection in the database which contains a user ID (long or ObjectId), item ID (long or ObjectId), preference value (optional) and timestamps (“created_at”, “deleted_at”).

          { "_id" : ObjectId("4d7627bf6c7d47ade9fc7780"),
            "user_id" : ObjectId("4c2209fef3924d31102bd84b"),
            "item_id" : ObjectId(4c2209fef3924d31202bd853),
            "preference" : 0.5,
            "created_at" : "Tue Mar 23 2010 20:48:43 GMT-0400 (EDT)"
          }
As test dataset, consider a collection of books as items and a list of users with their ratings on specific books. The matrix U x I would look like this:



To get this tiny ratings matrix into your MongoDB, open the MongoDB console and type:

           use test;
           db.users.save({"name" : "Billy"});
           db.users.save({"name" : "Sarah"});
           db.users.save({"name" : "Klara"});
           db.users.save({"name" : "Joseph"});
           db.users.save({"name" : "Bob"});
           db.users.save({"name" : "Sue"});
           db.books.save({"title" : "Harry Potter"});
           db.books.save({"title" : "Sherlock Holmes"});
           db.books.save({"title" : "Alice in Wonderland"});
           db.books.save({"title" : "Game of Thrones"});
           db.books.save({"title" : "Pretty Little Liars"});
           db.books.save({"title" : "Twilight"});
           db.books.save({"title" : "The Innocent"});
           db.books.save({"title" : "Pride and Prejudice"});
           
           db.ratings.save({ "user_id" : db.users.findOne({name:"Billy"})._id,
           "item_id" : db.books.findOne({title:"Harry Potter"})._id,
           "preference" : 4,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Billy"})._id,
           "item_id" : db.books.findOne({title:"Twilight"})._id,
           "preference" : 1,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Billy"})._id,
           "item_id" : db.books.findOne({title:"The Innocent"})._id,
           "preference" : 3,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Sarah"})._id,
           "item_id" : db.books.findOne({title:"Game of Thrones"})._id,
           "preference" : 1,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Sarah"})._id,
           "item_id" : db.books.findOne({title:"Pretty Little Liars"})._id,
           "preference" : 5,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Sarah"})._id,
           "item_id" : db.books.findOne({title:"Twilight"})._id,
           "preference" : 4,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Sarah"})._id,
           "item_id" : db.books.findOne({title:"Pride and Prejudice"})._id,
           "preference" : 5,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Klara"})._id,
           "item_id" : db.books.findOne({title:"Alice in Wonderland"})._id,
           "preference" : 3,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Klara"})._id,
           "item_id" : db.books.findOne({title:"Pretty Little Liars"})._id,
           "preference" : 4,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Joseph"})._id,
           "item_id" : db.books.findOne({title:"Sherlock Holmes"})._id,
           "preference" : 3,
           "created_at" : 1339436655 });
           db.ratings.save({"user_id" : db.users.findOne({name:"Joseph"})._id,
           "item_id" : db.books.findOne({title:"Game of Thrones"})._id,
           "preference" : 5,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Bob"})._id,
           "item_id" : db.books.findOne({title:"Harry Potter"})._id,
           "preference" : 2,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Bob"})._id,
           "item_id" : db.books.findOne({title:"Sherlock Holmes"})._id,
           "preference" : 5,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Bob"})._id,
           "item_id" : db.books.findOne({title:"Game of Thrones"})._id,
           "preference" : 4,
           "created_at" : 1339436655 });
           db.ratings.save({ "user_id" : db.users.findOne({name:"Sue"})._id,
           "item_id" : db.books.findOne({title:"Twilight"})._id,
           "preference" : 5,
           "created_at" : 1339436655 });
           
If you are building and running a Java project dont forget to add following libraries:

MongoDB Java API
Uncommons Math
Mahout Math, Mahout Core, Mahout Integration, Mahout Collections
After you have created the MongoDB content above, you can create a new instance of the class MongoDBDataModel. This instance of DataModel is the connection to your MongoDB database. The model provides an access interface to all necessary information about items, users and their ratings for the recommendation engine:



                MongoDBDataModel dbm = new MongoDBDataModel("127.0.0.1",
                                            27017,
                                             "test",
                                          "ratings",
                                              false,
                                              false,
                                               null);
After successfully creating an instance of the datamodel, which is refering to the ratings table/document, you can start to build a simple recommender. In order to calculate the missing ratings we will use the matrix factorization approach, which is implemented within the Mahout SVDRecommender class (org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender). The SVDRecommender method demands for a specific factorizer implementation, where we will choose the “Alternating-Least-Squares with Weighted-λ-Regularization” factorization, which is implemented within the class ALSWRFactorizer.

MongoDBDataModel dbm = 
   new MongoDBDataModel("127.0.0.1", 27017, "test", "ratings", true, true, null);
SVDRecommender svd = 
   new SVDRecommender(dbm, new ALSWRFactorizer(dbm, 3, 0.05f, 50));
So after you run the code snipplet above you approximately get following estimated rating table:
