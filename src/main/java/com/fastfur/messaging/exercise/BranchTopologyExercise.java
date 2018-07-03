package com.fastfur.messaging.exercise;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.Queries;
import com.fastfur.messaging.producer.twitter.TweetProducer;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;

import java.util.Arrays;
import java.util.Properties;

/**
 * In this Exercise you will have to Listens to tweet,
 * filter only english tweets, branch by predicates to different topics by device and print it
 */
public class BranchTopologyExercise {

    public static final String INPUT_TOPIC_NAME = "twitters";
    public static final String EN = "en";
    public static final String OUTPUTTOPIC1 = "outputtopic1";
    public static final String OUTPUTTOPIC2 = "outputtopic2";


    public static void main(String[] args) throws Exception {

        TweetProducer tp = new TweetProducer();
        tp.produceTweets(INPUT_TOPIC_NAME, Queries.getQueries());

        Properties config = new Properties();
        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "branch-topology");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class.getName());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream(INPUT_TOPIC_NAME, Consumed.with(Serdes.String(), new TweetSerde()));
        /**
        //create two predicates for branch source of tweet
        //iPhone as a source
        Predicate<String, Tweet> iPhoneSource = (s, tweet) -> tweet.getSource().contains("iPhone");
        //android as a source
        Predicate<String, Tweet> androidSource = (s, tweet) -> tweet.getSource().contains("Android");

        Predicate<String, Tweet> notIPhoneOrAndroid = (s, tweet) -> !(tweet.getSource().contains("Android") & tweet.getSource().contains("iPhone"));
        */

        KafkaStreams streams = new KafkaStreams(builder.build(), config);

        streams.start();
    }
}
