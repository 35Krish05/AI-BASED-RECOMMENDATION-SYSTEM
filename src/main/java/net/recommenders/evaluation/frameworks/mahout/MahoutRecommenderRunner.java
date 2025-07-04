/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.frameworks.mahout;

import net.recommenders.evaluation.frameworks.AbstractRunner;
import net.recommenders.evaluation.frameworks.RecommendationRunner;
import net.recommenders.evaluation.frameworks.mahout.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.grouplens.lenskit.data.dao.PrefetchingItemDAO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author alejandr, alan
 */
public class MahoutRecommenderRunner extends AbstractRunner {

    public static final int DEFAULT_NEIGHBORHOOD_SIZE = 50;
    public static final int DEFAULT_ITERATIONS = 50;
    public MahoutRecommenderRunner(Properties _properties){
        super(_properties);
    }

    @Override
    public void run() throws IOException, TasteException {
        if(alreadyRecomended)
            return;
        DataModel trainModel = new FileDataModel(new File(properties.getProperty(RecommendationRunner.trainingSet)));
        DataModel testModel = new FileDataModel(new File(properties.getProperty(RecommendationRunner.testSet)));
        GenericRecommenderBuilder grb = new GenericRecommenderBuilder();

        if (properties.containsKey(RecommendationRunner.neighborhood) && properties.getProperty(RecommendationRunner.neighborhood).equals("-1"))
            properties.setProperty(RecommendationRunner.neighborhood, Math.round(Math.sqrt(trainModel.getNumItems())) + "");
        if (properties.containsKey(RecommendationRunner.factors) && properties.getProperty(RecommendationRunner.factors).equals("-1"))
            properties.setProperty(RecommendationRunner.factors, Math.round(Math.sqrt(trainModel.getNumItems())) + "");


        Recommender recommender = null;
        try{
            if(properties.getProperty(RecommendationRunner.factors) == null)
                recommender = grb.buildRecommender(trainModel, properties.getProperty(RecommendationRunner.recommender), properties.getProperty(RecommendationRunner.similarity), Integer.parseInt(properties.getProperty(RecommendationRunner.neighborhood)));
            if(properties.getProperty(RecommendationRunner.factors) != null)
//                recommender = grb.buildRecommender(trainModel, properties.getProperty(RecommendationRunner.recommender), properties.getProperty(RecommendationRunner.factorizer), Integer.parseInt(properties.getProperty(RecommendationRunner.iterations)), Integer.parseInt(properties.getProperty(RecommendationRunner.factors)));
                recommender = grb.buildRecommender(trainModel, properties.getProperty(RecommendationRunner.recommender), properties.getProperty(RecommendationRunner.factorizer), DEFAULT_ITERATIONS, Integer.parseInt(properties.getProperty(RecommendationRunner.factors)));
            //(dataModel, recommender, factorizer, default iterations, factors)
        }catch (RecommenderException e){
            e.printStackTrace();
        }

        LongPrimitiveIterator users = testModel.getUserIDs();
        while (users.hasNext()) {
            long u = users.nextLong();
            List<RecommendedItem> items = recommender.recommend(u, trainModel.getNumItems());
            writeData(u, items);
        }
    }


    public void setProperties(Properties parameters) {
        this.properties = parameters;
    }


}
