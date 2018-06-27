package com.fastfur.messaging.exercise.solution;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.Queries;
import com.fastfur.messaging.producer.twitter.TweetProducer;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;

import java.util.Arrays;
import java.util.Properties;



public class BranchTopologySolution {

    public static final String INPUT_TOPIC_NAME = "twitters";
    public static final String EN = "en";
    public static final String OUTPUTTOPIC1 = "outputtopic1";
    public static final String OUTPUTTOPIC2 = "outputtopic2";


    public static void main(String[] args) throws Exception {

        TweetProducer tp = new TweetProducer();
        tp.produceTweets(INPUT_TOPIC_NAME, Queries.getQueries());

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "branch-topology");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class.getName());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream(INPUT_TOPIC_NAME, Consumed.with(Serdes.String(), new TweetSerde()));

        //create two predicates for branch source of tweet
        //iPhone as a source
        Predicate<String, Tweet> iPhoneSource = (s, tweet) -> tweet.getSource().contains("iPhone");
        //android as a source
        Predicate<String, Tweet> androidSource = (s, tweet) -> tweet.getSource().contains("Android");

        Predicate<String, Tweet> notIPhoneOrAndroid = (s, tweet) -> !(tweet.getSource().contains("Android") & tweet.getSource().contains("iPhone"));

        //stream.print();
        //filter by language and create three branches based on the predefined predicates:
        KStream<String, Tweet>[] kStreams = stream.filter((k, v) -> (v.getLanguage().equals(EN)))
                .branch(iPhoneSource, androidSource, notIPhoneOrAndroid);

        //count transforms stream to KTable
        //we can turn KTable back to stream. This table - stream duality
        //in this case we get a stream where key is a word and value is counter ( type long )

        kStreams[0].
                mapValues(v-> v.getText()).
                    flatMapValues( v -> Arrays.asList(v.split("\\W+"))).
                         mapValues(v-> v.toLowerCase()).
                            groupBy((k,v)-> v, Serialized.with(Serdes.String(),Serdes.String())).count().
                               toStream().map(( word,count) -> org.apache.kafka.streams.KeyValue.pair(word,count)).
                                 to(OUTPUTTOPIC2,Produced.with(Serdes.String(), Serdes.Long()));
        

        //using peek not to stop stream
//        kStreams[0].peek((k, v) ->
//                System.out.println(v.getSource() + " from iPhone stream"));

//        kStreams[1].foreach((k, v) ->
//                System.out.println(v.getSource() + " from Android stream"));
//
//        kStreams[2].foreach((k, v) ->
//                System.out.println(v.getSource() + " from Other sources stream"));


         kStreams[0].to(OUTPUTTOPIC1, Produced.with(Serdes.String(), new TweetSerde()));


        KafkaStreams streams = new KafkaStreams(builder.build(), config);

        streams.start();
    }


}
