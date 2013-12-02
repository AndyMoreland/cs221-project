package EmailAnalysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

/* This classifier takes in several features and makes a prediction. */
public class AggregateClassifier implements TrainableClassifier {
    public static final double DEFAULT_THRESHOLD = 0.5;
    private final Map<Feature, Float> weights;
    private Set<Feature> features;

    public AggregateClassifier(Set<Feature> features) {
        this(features, DEFAULT_THRESHOLD);
    }

    public AggregateClassifier(Set<Feature> features, double threshold) {
        this.features = features;
        this.weights = Maps.newHashMap();
    }

    @Override
    public EmailClass classify(Email email) {
        List<Float> featureValues = processEmail(email);
        System.err.println("Attempting to use an aggregate classifier without any meat in it.");
        return EmailClass.SHOULD_RESPOND_TO;
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<Email> emails) {
        Map<Email, EmailClass> classes = Maps.newHashMap();
        for (Email email : emails) {
            classes.put(email, classify(email));
        }

        return classes;
    }

    @Override
    public void train(List<Email> trainingData, Oracle trueClassifier) {
        System.err.println("Currently unimplemented");
    }

    private List<Float> processEmail(Email email) {
        List<Float> featureValues = Lists.newArrayList();
        for (Feature feature : features) {
            featureValues.add(feature.getValue(email));
        }
        return featureValues;
    }
}
